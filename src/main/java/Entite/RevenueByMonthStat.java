package Entite;

public class RevenueByMonthStat {
    private String periodLabel;
    private double totalRevenue;

    public RevenueByMonthStat(String periodLabel, double totalRevenue) {
        this.periodLabel = periodLabel;
        this.totalRevenue = totalRevenue;
    }
    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}
