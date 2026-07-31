import java.util.List;

//Interface defining all inventory CRUD and stock operations.

public interface StockOperations {

    void addProduct(Product product) throws ProductException;

    List<Product> viewAllProducts() throws ProductException;

    void updateProduct(Product product) throws ProductException;

    void deleteProduct(int productId) throws ProductException;

    Product searchById(int productId) throws ProductException;

    List<Product> searchByName(String name) throws ProductException;

    void reduceStock(int productId, int amount) throws ProductException;

    void restock(int productId, int amount) throws ProductException;

    List<Product> getLowStockProducts() throws ProductException;
}
