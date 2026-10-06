import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class FeedMillManager {
    private static final String DB_URL = "jdbc:sqlite:../feed_mill.db";
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        FeedMillManager app = new FeedMillManager();
        app.ensureDatabase();
        app.run();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void ensureDatabase() {
        try (Connection conn = connect()) {
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("Connected to feed_mill.db");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    private void run() {
        while (true) {
            System.out.println("\n=== Feed Mill Management Console ===");
            System.out.println("1. View inventory");
            System.out.println("2. Add raw material");
            System.out.println("3. Record purchase");
            System.out.println("4. Record production batch");
            System.out.println("5. Record sale");
            System.out.println("6. Show low-stock materials");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewInventory();
                case "2" -> addMaterial();
                case "3" -> recordPurchase();
                case "4" -> recordProduction();
                case "5" -> recordSale();
                case "6" -> showLowStock();
                case "7" -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewInventory() {
        String sql = "SELECT material_id, name, unit, current_stock, reorder_level, unit_cost FROM raw_materials ORDER BY name";
        try (Connection conn = connect(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-4s %-22s %-7s %-12s %-12s %-10s%n", "ID", "Material", "Unit", "Stock", "Reorder", "Cost");
            while (rs.next()) {
                System.out.printf("%-4d %-22s %-7s %-12.2f %-12.2f %-10.2f%n",
                        rs.getInt("material_id"), rs.getString("name"), rs.getString("unit"),
                        rs.getDouble("current_stock"), rs.getDouble("reorder_level"), rs.getDouble("unit_cost"));
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    private void addMaterial() {
        System.out.print("Material name: "); String name = scanner.nextLine();
        System.out.print("Unit (kg/bag/etc.): "); String unit = scanner.nextLine();
        double stock = readDouble("Starting stock: ");
        double reorder = readDouble("Reorder level: ");
        double cost = readDouble("Unit cost: ");
        String sql = "INSERT INTO raw_materials(name, unit, current_stock, reorder_level, unit_cost) VALUES(?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, unit); ps.setDouble(3, stock); ps.setDouble(4, reorder); ps.setDouble(5, cost);
            ps.executeUpdate();
            System.out.println("Material added.");
        } catch (SQLException e) { System.out.println("Could not add material: " + e.getMessage()); }
    }

    private void recordPurchase() {
        int materialId = readInt("Material ID: ");
        int supplierId = readInt("Supplier ID: ");
        double qty = readDouble("Quantity purchased: ");
        double unitCost = readDouble("Unit cost: ");
        String date = readDate("Purchase date (YYYY-MM-DD, blank=today): ");

        String purchaseSql = "INSERT INTO purchases(material_id, supplier_id, quantity, unit_cost, purchase_date) VALUES(?,?,?,?,?)";
        String stockSql = "UPDATE raw_materials SET current_stock = current_stock + ?, unit_cost = ? WHERE material_id = ?";
        String moveSql = "INSERT INTO inventory_movements(material_id, movement_type, quantity, reference_note, movement_date) VALUES(?, 'IN', ?, 'Purchase', ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(purchaseSql); PreparedStatement p2 = conn.prepareStatement(stockSql); PreparedStatement p3 = conn.prepareStatement(moveSql)) {
                p1.setInt(1, materialId); p1.setInt(2, supplierId); p1.setDouble(3, qty); p1.setDouble(4, unitCost); p1.setString(5, date); p1.executeUpdate();
                p2.setDouble(1, qty); p2.setDouble(2, unitCost); p2.setInt(3, materialId); p2.executeUpdate();
                p3.setInt(1, materialId); p3.setDouble(2, qty); p3.setString(3, date); p3.executeUpdate();
                conn.commit();
                System.out.println("Purchase recorded and inventory updated.");
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { System.out.println("Purchase failed: " + e.getMessage()); }
    }

    private void recordProduction() {
        System.out.print("Feed type: "); String feedType = scanner.nextLine();
        double qty = readDouble("Quantity produced (kg): ");
        double cost = readDouble("Estimated production cost: ");
        String date = readDate("Production date (YYYY-MM-DD, blank=today): ");
        String sql = "INSERT INTO production_batches(feed_type, quantity_produced, production_date, estimated_cost, status) VALUES(?,?,?,?, 'Completed')";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedType); ps.setDouble(2, qty); ps.setString(3, date); ps.setDouble(4, cost); ps.executeUpdate();
            System.out.println("Production batch recorded.");
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    private void recordSale() {
        System.out.print("Customer name: "); String customer = scanner.nextLine();
        System.out.print("Feed type: "); String feedType = scanner.nextLine();
        double qty = readDouble("Quantity sold (kg): ");
        double price = readDouble("Unit price: ");
        String date = readDate("Sale date (YYYY-MM-DD, blank=today): ");
        String sql = "INSERT INTO sales(customer_name, feed_type, quantity, unit_price, sale_date) VALUES(?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer); ps.setString(2, feedType); ps.setDouble(3, qty); ps.setDouble(4, price); ps.setString(5, date); ps.executeUpdate();
            System.out.println("Sale recorded.");
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    private void showLowStock() {
        String sql = "SELECT name, current_stock, reorder_level, unit FROM raw_materials WHERE current_stock <= reorder_level ORDER BY current_stock";
        try (Connection conn = connect(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nLow-stock materials:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("- %s: %.2f %s (reorder at %.2f)%n", rs.getString("name"), rs.getDouble("current_stock"), rs.getString("unit"), rs.getDouble("reorder_level"));
            }
            if (!any) System.out.println("None.");
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid whole number."); }
        }
    }

    private String readDate(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? LocalDate.now().toString() : value;
    }
}
