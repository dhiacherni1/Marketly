package Entite;

public class TopCustomerStat {
    private String customerName;
    private long totalOrders;
    private double totalSpent;

    public TopCustomerStat(String customerName, long totalOrders, double totalSpent) {
        this.customerName = customerName;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
    }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
}
