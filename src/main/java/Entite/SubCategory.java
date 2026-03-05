package Entite;

public class SubCategory {
    private Long id;
    private String name;
    private Category category;
    private String imagePath;

    public SubCategory() {}
    public SubCategory(Long id, String name, Category category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imagePath = null;
    }
    public SubCategory(Long id, String name, Category category, String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCategoryName() {
        return category != null && category.getName() != null ? category.getName() : "";
    }
}
