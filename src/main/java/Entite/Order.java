package Entite;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private Customer customer;
    private double total;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private String address;
    private String phone;
    private String status = "PENDING";
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}
    public Order(Long id, Customer customer, double total, LocalDateTime orderDate,
                 String paymentMethod, String address, String phone) {
        this.id = id;
        this.customer = customer;
        this.total = total;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.phone = phone;
    }
    public Order(Long id, Customer customer, double total, LocalDateTime orderDate,
                 String paymentMethod, String address, String phone, String status) {
        this.id = id;
        this.customer = customer;
        this.total = total;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.phone = phone;
        this.status = status != null ? status : "PENDING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getCustomerName() {
        return customer != null ? (customer.getPrenom() + " " + customer.getNom()).trim() : "";
    }

    public String getOrderDateFormatted() {
        return orderDate != null ? orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
}
