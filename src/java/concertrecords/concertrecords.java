package concertrecords;

import java.sql.*;

public class ConcertRecords {

    private Connection connection;

    // Constructor to initialize the connection
    public ConcertRecords(Connection connection) {
        this.connection = connection;
    }

    // Method to view the ticket record and return it as a String for JSP rendering
    public String ticketRecord(int ticketCode) {
        String query = """
        SELECT
            Tickets.ticket_code, Tickets.ticket_price, Tickets.seat_number, Tickets.ticket_type,
            Concerts.concert_title, Concerts.performer_name, Concerts.concert_date,
            Venues.venue_name, Transactions.transaction_date, Transactions.transaction_type,
            Customers.first_name AS customer_first_name,
            Customers.last_name AS customer_last_name, Customers.email AS customer_email,
            Customers.contact_number AS customer_contact
        FROM Tickets
        JOIN Concerts ON Tickets.concert_code = Concerts.concert_code
        JOIN Artists ON Concerts.artist_code = Artists.artist_code
        JOIN Venues ON Concerts.venue_code = Venues.venue_code
        JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
        JOIN Customers ON Transactions.customer_code = Customers.customer_code
        WHERE Tickets.ticket_code = ?;
       """;

        StringBuilder result = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ticketCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.append("<h2>Ticket Details</h2>")
                          .append("<p>Ticket Code: ").append(rs.getInt("ticket_code")).append("</p>")
                          .append("<p>Ticket Price: ₱").append(rs.getBigDecimal("ticket_price")).append("</p>")
                          .append("<p>Seat Type: ").append(rs.getString("ticket_type")).append("</p>")
                          .append("<p>Seat Number: ").append(rs.getString("seat_number")).append("</p>")
                          .append("<p>Transacted: ").append(rs.getTimestamp("transaction_date")).append("</p>")
                          .append("<h2>Concert Details</h2>")
                          .append("<p>Event Name: ").append(rs.getString("concert_title")).append("</p>")
                          .append("<p>Performer: ").append(rs.getString("performer_name")).append("</p>")
                          .append("<p>Concert Date: ").append(rs.getDate("concert_date")).append("</p>")
                          .append("<p>Venue: ").append(rs.getString("venue_name")).append("</p>")
                          .append("<h2>Customer Details</h2>")
                          .append("<p>Name: ").append(rs.getString("customer_first_name"))
                          .append(" ").append(rs.getString("customer_last_name")).append("</p>")
                          .append("<p>Email: ").append(rs.getString("customer_email")).append("</p>")
                          .append("<p>Contact Number: ").append(rs.getString("customer_contact")).append("</p>");

                    String transactionType = rs.getString("transaction_type");
                    if ("transfer".equalsIgnoreCase(transactionType)) {
                        result.append("<p>Original Buyer: Transferred ticket</p>");
                    } else {
                        result.append("<p>Original Buyer: Yes</p>");
                    }
                } else {
                    result.append("<p>No ticket record found for Ticket Code: ").append(ticketCode).append("</p>");
                }
            }
        } catch (SQLException e) {
            result.append("<p>Error fetching ticket record: ").append(e.getMessage()).append("</p>");
        }
        return result.toString();
    }

    // Method to view the concert record
    public String concertRecord(int concertId) {
        String query = """
        SELECT
            Concerts.concert_code,
            Concerts.performer_name,
            Concerts.concert_title,
            Concerts.entry_restrictions,
            Venues.venue_name,
            Concerts.concert_date,
            Concerts.tickets_available
        FROM Concerts
        JOIN Venues ON Concerts.venue_code = Venues.venue_code
        WHERE Concerts.concert_code = ?;
        """;

        StringBuilder result = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, concertId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.append("<h2>Concert Details</h2>")
                          .append("<p>Concert Code: ").append(rs.getInt("concert_code")).append("</p>")
                          .append("<p>Event Name: ").append(rs.getString("concert_title")).append("</p>")
                          .append("<p>Performer: ").append(rs.getString("performer_name")).append("</p>")
                          .append("<p>Entry Restrictions: ").append(rs.getString("entry_restrictions")).append("</p>")
                          .append("<p>Venue: ").append(rs.getString("venue_name")).append("</p>")
                          .append("<p>Concert Date: ").append(rs.getDate("concert_date")).append("</p>")
                          .append("<p>Tickets Available: ").append(rs.getInt("tickets_available")).append("</p>");
                } else {
                    result.append("<p>No concert record found for Concert ID: ").append(concertId).append("</p>");
                }
            }
        } catch (SQLException e) {
            result.append("<p>Error fetching concert record: ").append(e.getMessage()).append("</p>");
        }
        return result.toString();
    }

    // Method to view the customer record
    public String customerRecord(int customerId) {
        String query = """
        SELECT 
            Customers.customer_code, Customers.first_name, Customers.last_name, Customers.email, Customers.contact_number,
            Tickets.ticket_code, Tickets.seat_number, Transactions.transaction_type
        FROM Customers
        LEFT JOIN Transactions ON Customers.customer_code = Transactions.customer_code
        LEFT JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
        WHERE Customers.customer_code = ?;
        """;

        StringBuilder result = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.append("<h2>Customer Details</h2>")
                          .append("<p>Customer Code: ").append(rs.getInt("customer_code")).append("</p>")
                          .append("<p>Name: ").append(rs.getString("first_name")).append(" ")
                          .append(rs.getString("last_name")).append("</p>")
                          .append("<p>Email: ").append(rs.getString("email")).append("</p>")
                          .append("<p>Contact Number: ").append(rs.getString("contact_number")).append("</p>");

                    result.append("<h3>Tickets Owned</h3>");
                    do {
                        result.append("<p>Ticket Code: ").append(rs.getInt("ticket_code"))
                              .append(" | Seat: ").append(rs.getString("seat_number"))
                              .append(" | Transaction Type: ").append(rs.getString("transaction_type")).append("</p>");
                    } while (rs.next());
                } else {
                    result.append("<p>No customer record found for Customer ID: ").append(customerId).append("</p>");
                }
            }
        } catch (SQLException e) {
            result.append("<p>Error fetching customer record: ").append(e.getMessage()).append("</p>");
        }
        return result.toString();
    }

    // Method to view the transaction record
    public String transactionRecord(int transactionCode) {
        String query = """
        SELECT 
            Transactions.transaction_code,
            Transactions.transaction_type,
            Transactions.transaction_date,
            Transactions.total_amount,
            Transactions.payment_method,
            Customers.first_name, Customers.last_name
        FROM Transactions
        JOIN Customers ON Transactions.customer_code = Customers.customer_code
        WHERE Transactions.transaction_code = ?;
        """;

        StringBuilder result = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, transactionCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.append("<h2>Transaction Details</h2>")
                          .append("<p>Transaction Code: ").append(rs.getInt("transaction_code")).append("</p>")
                          .append("<p>Type: ").append(rs.getString("transaction_type")).append("</p>")
                          .append("<p>Date: ").append(rs.getTimestamp("transaction_date")).append("</p>")
                          .append("<p>Total Amount: ₱").append(rs.getBigDecimal("total_amount")).append("</p>")
                          .append("<p>Payment Method: ").append(rs.getString("payment_method")).append("</p>")
                          .append("<h3>Customer Details</h3>")
                          .append("<p>Name: ").append(rs.getString("first_name")).append(" ")
                          .append(rs.getString("last_name")).append("</p>");
                } else {
                    result.append("<p>No transaction record found for Transaction Code: ").append(transactionCode).append("</p>");
                }
            }
        } catch (SQLException e) {
            result.append("<p>Error fetching transaction record: ").append(e.getMessage()).append("</p>");
        }
        return result.toString();
    }

    // Static method to establish a connection to the database
    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/concerttix";
        String user = "root";
        String password = "Is!ma2friend3";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}
