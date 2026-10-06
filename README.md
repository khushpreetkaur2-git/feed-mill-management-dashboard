# Feed Mill Management Dashboard

A portfolio project inspired by real feed-mill operations. It demonstrates how a traditional manufacturing business can use software to organize inventory, suppliers, purchasing, feed production, sales, and operational reporting.

## Tech Stack

- **Java 17** — command-line management application
- **SQL / SQLite** — relational database and business records
- **Python** — analytics and dashboard
- **Streamlit + Pandas** — interactive reporting interface
- **Maven** — Java dependency/build management

## Features

- Track raw-material stock and reorder levels
- Store supplier information
- Record purchases and automatically update inventory
- Record feed-production batches
- Record customer sales
- Detect low-stock materials
- View inventory value, production totals and sales revenue
- Visualize production, costs, inventory and revenue over time

## Project Structure

```text
feed_mill_dashboard/
├── java/
│   ├── pom.xml
│   └── src/main/java/FeedMillManager.java
├── python/
│   ├── dashboard.py
│   ├── setup_database.py
│   └── requirements.txt
├── sql/
│   ├── schema.sql
│   └── sample_data.sql
├── feed_mill.db                # created after setup
└── README.md
```

## 1. Create the database

From the project root:

```bash
python3 python/setup_database.py
```

This creates `feed_mill.db` and loads sample feed-mill data.

## 2. Run the Python dashboard

Create and activate a virtual environment if desired, then install dependencies:

```bash
pip install -r python/requirements.txt
streamlit run python/dashboard.py
```

The browser dashboard displays inventory, production, sales and low-stock alerts.

## 3. Run the Java application

Requirements: Java 17+ and Maven.

```bash
cd java
mvn compile exec:java
```

The Java console application connects to the same `feed_mill.db` file and lets you view inventory, add materials, record purchases, record production, record sales and check low-stock items.

## Database Design

The database contains six main tables:

- `suppliers`
- `raw_materials`
- `purchases`
- `production_batches`
- `sales`
- `inventory_movements`

Relationships use primary and foreign keys, and common date fields are indexed for reporting queries.

## Portfolio / Resume Description

**Feed Mill Management Dashboard — Java, Python, SQL, SQLite, Streamlit**

- Developed a database-driven management system for tracking feed-mill inventory, suppliers, purchases, production batches and sales.
- Designed a relational SQL database with primary/foreign-key relationships and queries for operational reporting.
- Built a Java application for business-record management and inventory updates using JDBC.
- Created a Python/Streamlit analytics dashboard to visualize inventory levels, production activity, costs and sales revenue.
- Implemented low-stock monitoring based on reorder thresholds to support inventory planning.

## Important Note

This is a portfolio project inspired by feed-mill operations. Do not claim that Skylark Poultry Feed Mill Pvt. Ltd. deployed or used this software unless that actually happened.
