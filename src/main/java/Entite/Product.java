package Entite;

public class Product {
    private Long id;
    private String name;
    private double price;
    private int quantity;
    private Category category;
    private SubCategory subCategory;
    private Provider provider;

    public Product() {}
    public Product(Long id, String name, double price, int quantity, Category category, Provider provider) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.provider = provider;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public SubCategory getSubCategory() { return subCategory; }
    public void setSubCategory(SubCategory subCategory) { this.subCategory = subCategory; }
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public String getCategoryName() {
        return category != null && category.getName() != null ? category.getName() : "";
    }
    public String getSubCategoryName() {
        return subCategory != null && subCategory.getName() != null ? subCategory.getName() : "";
    }
}
