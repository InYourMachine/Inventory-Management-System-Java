// Base product class demonstrating encapsulation.
// General products use this class directly.

public class Product {

    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private String category;

    public Product() {
    }

    public Product(int productId, String productName, double price,
                   int quantity, String category) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    //Returns stock status based on quantity (low stock threshold = 5).

    public String getStockStatus() {
        if (quantity <= 0) {
            return "OUT OF STOCK";
        } else if (quantity < 5) {
            return "LOW STOCK";
        }
        return "IN STOCK";
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | $%.2f | Qty: %d | %s | %s",
                productId, productName, price, quantity, category, getStockStatus());
    }
}
