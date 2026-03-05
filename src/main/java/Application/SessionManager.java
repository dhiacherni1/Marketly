package Application;

import Entite.Customer;
import Entite.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Customer currentCustomer;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentCustomer(Customer customer) { this.currentCustomer = customer; }
    public Customer getCurrentCustomer() { return currentCustomer; }
    public boolean isAdmin() { return currentUser != null && "ADMIN".equals(currentUser.getRole()); }
    public boolean isClient() { return currentUser != null && "CLIENT".equals(currentUser.getRole()); }
    public void logout() { this.currentUser = null; this.currentCustomer = null; }
}
