package Services;

import Entite.Customer;
import Entite.Order;
import Entite.OrderItem;
import Entite.Product;
import Utlis.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrder {
    private final Connection con = DataSource.getInstance().getCon();
    private final ServiceCustomer serviceCustomer = new ServiceCustomer();
    private final ServiceProduct serviceProduct = new ServiceProduct();

    public Order createOrderFromCart(Customer customer, String firstName, String lastName, String phone, String address, String paymentMethod) throws SQLException {
        if (customer == null) return null;
        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;
        try {
            String cartSql = "SELECT c.product_id, c.quantity, p.price FROM carts c JOIN products p ON p.id = c.product_id WHERE c.customer_id=?";
            PreparedStatement cartStmt = con.prepareStatement(cartSql);
            cartStmt.setLong(1, customer.getId());
            ResultSet rs = cartStmt.executeQuery();
            while (rs.next()) {
                long productId = rs.getLong("product_id");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                total += qty * price;
                Product product = serviceProduct.findById((int) productId);
                items.add(new OrderItem(null, null, product, qty, price, qty * price));
            }
            if (items.isEmpty()) {
                con.setAutoCommit(previousAutoCommit);
                return null;
            }
            String insertSql = "INSERT INTO orders(customer_id, total, order_date, payment_method, address, phone, status) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement orderStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setLong(1, customer.getId());
                orderStmt.setDouble(2, total);
                orderStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                orderStmt.setString(4, paymentMethod);
                orderStmt.setString(5, address);
                orderStmt.setString(6, phone);
                orderStmt.setString(7, "PENDING");
                orderStmt.executeUpdate();
                ResultSet keys = orderStmt.getGeneratedKeys();
                if (!keys.next()) {
                    con.rollback();
                    con.setAutoCommit(previousAutoCommit);
                    return null;
                }
                long orderId = keys.getLong(1);
                try (PreparedStatement itemStmt = con.prepareStatement("INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) VALUES (?,?,?,?,?)")) {
                    for (OrderItem item : items) {
                        itemStmt.setLong(1, orderId);
                        itemStmt.setLong(2, item.getProduct().getId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.setDouble(5, item.getLineTotal());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }
                try (PreparedStatement clearCart = con.prepareStatement("DELETE FROM carts WHERE customer_id=?")) {
                    clearCart.setLong(1, customer.getId());
                    clearCart.executeUpdate();
                }
                con.commit();
                con.setAutoCommit(previousAutoCommit);
                Order order = new Order(orderId, customer, total, LocalDateTime.now(), paymentMethod, address, phone, "PENDING");
                order.setItems(items);
                return order;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("status")) {
                try (PreparedStatement orderStmt = con.prepareStatement("INSERT INTO orders(customer_id, total, order_date, payment_method, address, phone) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    orderStmt.setLong(1, customer.getId());
                    orderStmt.setDouble(2, total);
                    orderStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    orderStmt.setString(4, paymentMethod);
                    orderStmt.setString(5, address);
                    orderStmt.setString(6, phone);
                    orderStmt.executeUpdate();
                    ResultSet keys = orderStmt.getGeneratedKeys();
                    if (keys.next()) {
                        long orderId = keys.getLong(1);
                        try (PreparedStatement itemStmt = con.prepareStatement("INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) VALUES (?,?,?,?,?)")) {
                            for (OrderItem item : items) {
                                itemStmt.setLong(1, orderId);
                                itemStmt.setLong(2, item.getProduct().getId());
                                itemStmt.setInt(3, item.getQuantity());
                                itemStmt.setDouble(4, item.getUnitPrice());
                                itemStmt.setDouble(5, item.getLineTotal());
                                itemStmt.addBatch();
                            }
                            itemStmt.executeBatch();
                        }
                        try (PreparedStatement clearCart = con.prepareStatement("DELETE FROM carts WHERE customer_id=?")) {
                            clearCart.setLong(1, customer.getId());
                            clearCart.executeUpdate();
                        }
                        con.commit();
                        con.setAutoCommit(previousAutoCommit);
                        Order order = new Order(orderId, customer, total, LocalDateTime.now(), paymentMethod, address, phone, "PENDING");
                        order.setItems(items);
                        return order;
                    }
                }
            }
            con.rollback();
            con.setAutoCommit(previousAutoCommit);
            throw e;
        }
    }

    /** Liste toutes les commandes avec le client (pour l'admin). */
    public List<Order> findAllWithCustomer() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sqlWithStatus = "SELECT o.id, o.customer_id, o.total, o.order_date, o.payment_method, o.address, o.phone, o.status, " +
                "c.prenom, c.nom, c.email FROM orders o JOIN customers c ON o.customer_id = c.id ORDER BY o.order_date DESC";
        String sqlNoStatus = "SELECT o.id, o.customer_id, o.total, o.order_date, o.payment_method, o.address, o.phone, " +
                "c.prenom, c.nom, c.email FROM orders o JOIN customers c ON o.customer_id = c.id ORDER BY o.order_date DESC";
        String sql = sqlWithStatus;
        try {
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrderRow(rs, true));
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("status")) {
                try (PreparedStatement ps = con.prepareStatement(sqlNoStatus); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapOrderRow(rs, false));
                }
            } else throw e;
        }
        return list;
    }

    private Order mapOrderRow(ResultSet rs, boolean hasStatus) throws SQLException {
        long custId = rs.getLong("customer_id");
        Customer cust = serviceCustomer.findById((int) custId);
        if (cust == null) cust = new Customer(custId, rs.getString("prenom"), rs.getString("nom"), rs.getString("email"));
        String status = "PENDING";
        if (hasStatus) try { if (rs.getString("status") != null) status = rs.getString("status"); } catch (SQLException ignored) {}
        return new Order(rs.getLong("id"), cust, rs.getDouble("total"),
                rs.getTimestamp("order_date").toLocalDateTime(), rs.getString("payment_method"),
                rs.getString("address"), rs.getString("phone"), status);
    }

    /** Charge une commande avec ses lignes (order_items + products) pour facture. */
    public Order findByIdWithItems(long orderId) throws SQLException {
        List<Order> all = findAllWithCustomer();
        for (Order o : all) {
            if (o.getId() != null && o.getId() == orderId) {
                String itemsSql = "SELECT oi.product_id, oi.quantity, oi.unit_price, oi.line_total FROM order_items oi WHERE oi.order_id=?";
                try (PreparedStatement ps = con.prepareStatement(itemsSql)) {
                    ps.setLong(1, orderId);
                    ResultSet rs = ps.executeQuery();
                    List<OrderItem> items = new ArrayList<>();
                    while (rs.next()) {
                        Product p = serviceProduct.findById(rs.getInt("product_id"));
                        items.add(new OrderItem(null, o, p, rs.getInt("quantity"), rs.getDouble("unit_price"), rs.getDouble("line_total")));
                    }
                    o.setItems(items);
                }
                return o;
            }
        }
        return null;
    }

    public boolean updateStatus(long orderId, String status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE orders SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Nombre total de commandes. */
    public int getTotalOrdersCount() throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /** Nombre de commandes en attente (PENDING). */
    public int getPendingOrdersCount() throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM orders WHERE status = ?")) {
            ps.setString(1, "PENDING");
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("status")) return 0;
            throw e;
        }
    }
}
