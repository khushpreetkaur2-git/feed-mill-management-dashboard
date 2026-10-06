import sqlite3
from pathlib import Path
import pandas as pd
import streamlit as st

BASE_DIR = Path(__file__).resolve().parents[1]
DB_PATH = BASE_DIR / "feed_mill.db"

st.set_page_config(page_title="Feed Mill Management Dashboard", page_icon="🌾", layout="wide")

@st.cache_data(ttl=10)
def query(sql: str, params=()):
    with sqlite3.connect(DB_PATH) as conn:
        return pd.read_sql_query(sql, conn, params=params)


def money(value):
    return f"₹{value:,.0f}"

st.title("Feed Mill Management Dashboard")
st.caption("Inventory, production, purchasing and sales overview")

if not DB_PATH.exists():
    st.error("Database not found. Run `python setup_database.py` first.")
    st.stop()

inventory = query("SELECT * FROM raw_materials ORDER BY name")
production = query("SELECT * FROM production_batches ORDER BY production_date")
sales = query("SELECT *, quantity * unit_price AS revenue FROM sales ORDER BY sale_date")
purchases = query("SELECT *, quantity * unit_cost AS purchase_value FROM purchases ORDER BY purchase_date")

inventory_value = float((inventory["current_stock"] * inventory["unit_cost"]).sum()) if not inventory.empty else 0
production_total = float(production["quantity_produced"].sum()) if not production.empty else 0
sales_revenue = float(sales["revenue"].sum()) if not sales.empty else 0
low_stock_count = int((inventory["current_stock"] <= inventory["reorder_level"]).sum()) if not inventory.empty else 0

c1, c2, c3, c4 = st.columns(4)
c1.metric("Inventory Value", money(inventory_value))
c2.metric("Feed Produced", f"{production_total:,.0f} kg")
c3.metric("Sales Revenue", money(sales_revenue))
c4.metric("Low-Stock Items", low_stock_count)

st.divider()

left, right = st.columns(2)
with left:
    st.subheader("Current Raw-Material Stock")
    if inventory.empty:
        st.info("No inventory data available.")
    else:
        chart_df = inventory.set_index("name")[["current_stock", "reorder_level"]]
        st.bar_chart(chart_df)

with right:
    st.subheader("Production by Feed Type")
    if production.empty:
        st.info("No production data available.")
    else:
        prod_by_type = production.groupby("feed_type", as_index=False)["quantity_produced"].sum().set_index("feed_type")
        st.bar_chart(prod_by_type)

left, right = st.columns(2)
with left:
    st.subheader("Sales Revenue Over Time")
    if sales.empty:
        st.info("No sales data available.")
    else:
        sales_time = sales.copy()
        sales_time["sale_date"] = pd.to_datetime(sales_time["sale_date"])
        sales_time = sales_time.groupby("sale_date", as_index=False)["revenue"].sum().set_index("sale_date")
        st.line_chart(sales_time)

with right:
    st.subheader("Production Cost Over Time")
    if production.empty:
        st.info("No production data available.")
    else:
        cost_time = production.copy()
        cost_time["production_date"] = pd.to_datetime(cost_time["production_date"])
        cost_time = cost_time.groupby("production_date", as_index=False)["estimated_cost"].sum().set_index("production_date")
        st.line_chart(cost_time)

st.subheader("Low-Stock Alerts")
low_stock = inventory[inventory["current_stock"] <= inventory["reorder_level"]].copy()
if low_stock.empty:
    st.success("All raw materials are above their reorder levels.")
else:
    low_stock["shortage_to_reorder"] = low_stock["reorder_level"] - low_stock["current_stock"]
    st.dataframe(
        low_stock[["name", "current_stock", "reorder_level", "unit", "shortage_to_reorder"]],
        use_container_width=True,
        hide_index=True,
    )

st.subheader("Operational Records")
tab1, tab2, tab3, tab4 = st.tabs(["Inventory", "Production", "Sales", "Purchases"])
with tab1:
    st.dataframe(inventory, use_container_width=True, hide_index=True)
with tab2:
    st.dataframe(production, use_container_width=True, hide_index=True)
with tab3:
    st.dataframe(sales, use_container_width=True, hide_index=True)
with tab4:
    st.dataframe(purchases, use_container_width=True, hide_index=True)

st.caption("Portfolio project inspired by feed-mill operations. Built with Python, Java and SQL.")
