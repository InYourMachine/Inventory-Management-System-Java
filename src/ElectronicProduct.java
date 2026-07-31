// Electronic product with warranty information (inheritance + polymorphism).

public class ElectronicProduct extends Product {

    private int warrantyMonths;

    public ElectronicProduct() {
        super();
        setCategory("Electronic");
    }

    public ElectronicProduct(int productId, String productName, double price,
                             int quantity, int warrantyMonths) {
        super(productId, productName, price, quantity, "Electronic");
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getStockStatus() {
        String baseStatus = super.getStockStatus();
        return baseStatus + " | Warranty: " + warrantyMonths + " months";
    }

    @Override
    public String toString() {
        return super.toString() + " | Warranty: " + warrantyMonths + " months";
    }
}
