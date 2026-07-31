// Grocery product with expiry date (inheritance + polymorphism).

public class GroceryProduct extends Product {

    private String expiryDate;

    public GroceryProduct() {
        super();
        setCategory("Grocery");
    }

    public GroceryProduct(int productId, String productName, double price,
                          int quantity, String expiryDate) {
        super(productId, productName, price, quantity, "Grocery");
        this.expiryDate = expiryDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String getStockStatus() {
        String baseStatus = super.getStockStatus();
        return baseStatus + " | Expires: " + expiryDate;
    }

    @Override
    public String toString() {
        return super.toString() + " | Expiry: " + expiryDate;
    }
}
