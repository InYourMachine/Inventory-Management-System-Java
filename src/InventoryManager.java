import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


// Implements StockOperations with JDBC and PreparedStatement.

public class InventoryManager implements StockOperations {

    private static final int LOW_STOCK_THRESHOLD = 5;

    @Override
    public void addProduct(Product product) throws ProductException {
        String sql = "INSERT INTO products (product_name, price, quantity, category, "
                + "expiry_date, warranty_months) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.setString(4, product.getCategory());
            ps.setString(5, getExpiryDate(product));
            ps.setObject(6, getWarrantyMonths(product));

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new ProductException("Failed to add product.");
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while adding product.", e);
        }
    }

    @Override
    public List<Product> viewAllProducts() throws ProductException {
        String sql = "SELECT * FROM products ORDER BY product_id";
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while viewing products.", e);
        }
        return list;
    }

    @Override
    public void updateProduct(Product product) throws ProductException {
        String sql = "UPDATE products SET product_name=?, price=?, quantity=?, category=?, "
                + "expiry_date=?, warranty_months=? WHERE product_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.setString(4, product.getCategory());
            ps.setString(5, getExpiryDate(product));
            ps.setObject(6, getWarrantyMonths(product));
            ps.setInt(7, product.getProductId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new ProductException("No product found with ID: " + product.getProductId());
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while updating product.", e);
        }
    }

    @Override
    public void deleteProduct(int productId) throws ProductException {
        String sql = "DELETE FROM products WHERE product_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new ProductException("No product found with ID: " + productId);
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while deleting product.", e);
        }
    }

    @Override
    public Product searchById(int productId) throws ProductException {
        String sql = "SELECT * FROM products WHERE product_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
                throw new ProductException("No product found with ID: " + productId);
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while searching by ID.", e);
        }
    }

    @Override
    public List<Product> searchByName(String name) throws ProductException {
        String sql = "SELECT * FROM products WHERE product_name LIKE ?";
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
            if (list.isEmpty()) {
                throw new ProductException("No products found matching name: " + name);
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while searching by name.", e);
        }
        return list;
    }

    @Override
    public void reduceStock(int productId, int amount) throws ProductException {
        if (amount <= 0) {
            throw new ProductException("Sell amount must be greater than zero.");
        }

        Product product = searchById(productId);
        if (product.getQuantity() < amount) {
            throw new ProductException("Insufficient stock. Available: " + product.getQuantity());
        }

        String sql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ProductException("Database error while reducing stock.", e);
        }

        Product updated = searchById(productId);
        if (updated.getQuantity() < LOW_STOCK_THRESHOLD) {
            throw new ProductException("Sale completed. LOW STOCK ALERT: "
                    + updated.getProductName() + " has only "
                    + updated.getQuantity() + " unit(s) left.");
        }
    }

    @Override
    public void restock(int productId, int amount) throws ProductException {
        if (amount <= 0) {
            throw new ProductException("Restock amount must be greater than zero.");
        }

        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, productId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new ProductException("No product found with ID: " + productId);
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while restocking.", e);
        }
    }

    @Override
    public List<Product> getLowStockProducts() throws ProductException {
        String sql = "SELECT * FROM products WHERE quantity < ? ORDER BY quantity";
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, LOW_STOCK_THRESHOLD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new ProductException("Database error while fetching low stock items.", e);
        }
        return list;
    }

    /** Polymorphic factory: creates the correct Product subtype from a DB row. */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("product_id");
        String name = rs.getString("product_name");
        double price = rs.getDouble("price");
        int quantity = rs.getInt("quantity");
        String category = rs.getString("category");

        switch (category) {
            case "Grocery":
                String expiry = rs.getString("expiry_date");
                return new GroceryProduct(id, name, price, quantity,
                        expiry != null ? expiry : "N/A");
            case "Electronic":
                int warranty = rs.getInt("warranty_months");
                if (rs.wasNull()) {
                    warranty = 0;
                }
                return new ElectronicProduct(id, name, price, quantity, warranty);
            default:
                return new Product(id, name, price, quantity, "General");
        }
    }

    private String getExpiryDate(Product product) {
        if (product instanceof GroceryProduct) {
            return ((GroceryProduct) product).getExpiryDate();
        }
        return null;
    }

    private Integer getWarrantyMonths(Product product) {
        if (product instanceof ElectronicProduct) {
            return ((ElectronicProduct) product).getWarrantyMonths();
        }
        return null;
    }
}
