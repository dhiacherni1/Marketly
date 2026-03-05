package Entite;

public class Customer {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private Long userId;

    public Customer() {}
    public Customer(Long id, String prenom, String nom, String email) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.userId = null;
    }
    public Customer(Long id, String prenom, String nom, String email, Long userId) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
