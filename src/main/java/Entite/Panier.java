package Entite;

public class Panier {
    private Long id;
    private Customer customer;
    private Product product;
    private int quantity;

    public Panier() {}
    public Panier(Long id, Customer customer, Product product, int quantity) {
        this.id = id;
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getProductName() {
        return product != null && product.getName() != null ? product.getName() : "";
    }
    public Double getProductPrice() {
        return product != null ? product.getPrice() : 0.0;
    }
}
