package concertreports;

import java.sql.*;

public class ConcertReports {

    private Connection connection;

    // Constructor to initialize the connection
    public ConcertReports(Connection connection) {
        this.connection = connection;
    }
    
    // Method to generate the customer engagement report
    public String customerEngagement(int year) {
        StringBuilder result = new StringBuilder();

        String query = """
            SELECT 
                Customers.customer_code,
                Customers.first_name,
                Customers.last_name,
                COUNT(DISTINCT Transactions.transaction_code) AS total_transactions,
                SUM(CASE 
                        WHEN Transactions.transaction_type = 'buy' AND Refunds.ticket_code IS NULL THEN Transactions.total_amount
                        ELSE 0
                    END) AS ticket_sales,
                SUM(CASE 
                        WHEN Transactions.transaction_type != 'buy' THEN Transactions.total_amount
                        ELSE 0
                    END) AS other_sales,
                SUM(CASE 
                        WHEN Transactions.transaction_type = 'buy' AND Refunds.ticket_code IS NULL THEN Transactions.total_amount
                        ELSE 0
                    END) + SUM(CASE 
                        WHEN Transactions.transaction_type != 'buy' THEN Transactions.total_amount
                        ELSE 0
                    END) AS total_sales
            FROM Customers
            JOIN Transactions ON Customers.customer_code = Transactions.customer_code
            LEFT JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
            LEFT JOIN Refunds ON Tickets.ticket_code = Refunds.ticket_code
            WHERE YEAR(Transactions.transaction_date) = ?
            GROUP BY Customers.customer_code, Customers.first_name, Customers.last_name
            ORDER BY total_sales DESC;
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                result.append("<h2>Customer Engagement Report for ").append(year).append("</h2>");
                result.append("<table border='1'>")
                      .append("<thead><tr><th>Customer ID</th><th>First Name</th><th>Last Name</th>")
                      .append("<th>Total Transactions</th><th>Total Sales</th><th>Ticket Sales</th><th>Misc Sales</th></tr></thead><tbody>");

                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    result.append("<tr>")
                          .append("<td>").append(rs.getInt("customer_code")).append("</td>")
                          .append("<td>").append(rs.getString("first_name")).append("</td>")
                          .append("<td>").append(rs.getString("last_name")).append("</td>")
                          .append("<td>").append(rs.getInt("total_transactions")).append("</td>")
                          .append("<td>").append("₱").append(String.format("%.2f", rs.getDouble("total_sales"))).append("</td>")
                          .append("<td>").append("₱").append(String.format("%.2f", rs.getDouble("ticket_sales"))).append("</td>")
                          .append("<td>").append("₱").append(String.format("%.2f", rs.getDouble("other_sales"))).append("</td>")
                          .append("</tr>");
                }
                if (!hasResults) {
                    result.append("<tr><td colspan='7'>No transactions found for the given year.</td></tr>");
                }
                result.append("</tbody></table>");
            }
        } catch (SQLException e) {
            result.append("<p>Error generating Customer Engagement Report: ").append(e.getMessage()).append("</p>");
        }
        return result.toString();
    }
    
    // Method to generate the annual sales report
public String annualSales(int year) {
    String query = """
    SELECT 
        MONTH(transaction_date) AS month,
        SUM(total_amount) AS total_sales,
        COUNT(transaction_code) AS tickets_sold
    FROM Transactions
    WHERE transaction_type = 'buy'
      AND YEAR(transaction_date) = ?
    GROUP BY MONTH(transaction_date)
    ORDER BY MONTH(transaction_date);
    """;

    StringBuilder result = new StringBuilder();
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, year); // Set the year parameter

        try (ResultSet rs = stmt.executeQuery()) {
            result.append("<h2>Annual Sales Report for ").append(year).append("</h2>");
            result.append("<table border='1' style='width: 100%; border-collapse: collapse;'>")
                  .append("<thead><tr><th>Month</th><th>Total Sales</th><th>Tickets Sold</th></tr></thead>")
                  .append("<tbody>");

            double yearlyTotal = 0;
            int yearlyTickets = 0;

            while (rs.next()) {
                int month = rs.getInt("month");
                double totalSales = rs.getDouble("total_sales");
                int ticketsSold = rs.getInt("tickets_sold");

                yearlyTotal += totalSales;
                yearlyTickets += ticketsSold;

                result.append("<tr>")
                      .append("<td>").append(getMonthName(month)).append("</td>")
                      .append("<td>").append("₱").append(String.format("%.2f", totalSales)).append("</td>")
                      .append("<td>").append(ticketsSold).append("</td>")
                      .append("</tr>");
            }

            result.append("<tr style='font-weight: bold; background-color: #f2f2f2;'>")
                  .append("<td>Total</td>")
                  .append("<td>").append("₱").append(String.format("%.2f", yearlyTotal)).append("</td>")
                  .append("<td>").append(yearlyTickets).append("</td>")
                  .append("</tr>");
            result.append("</tbody></table>");
        }
    } catch (SQLException e) {
        result.append("<p>Error fetching sales data: ").append(e.getMessage()).append("</p>");
    }
    return result.toString();
}

    // Helper method to convert month number to month name
    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Unknown";
        };
    }

public String concertAnalysis(int year) {
    StringBuilder result = new StringBuilder();

    // SQL query to get the concert analysis data for the given year
    String query = """
    SELECT 
        Concerts.concert_code,
        Concerts.concert_title,
        Concerts.performer_name,
        COUNT(Tickets.ticket_code) AS total_tickets_sold
    FROM Concerts
    INNER JOIN Tickets ON Concerts.concert_code = Tickets.concert_code
    INNER JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
    WHERE YEAR(Concerts.concert_date) = ?
    AND Transactions.transaction_type = 'buy'  -- Only consider 'buy' transactions
    GROUP BY Concerts.concert_code, Concerts.concert_title, Concerts.performer_name
    ORDER BY total_tickets_sold DESC;
    """;

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, year); // Set the year parameter

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Append each concert's details to the result
                result.append(String.format("%-15d %-30s %-25s %-15d\n",
                        rs.getInt("concert_code"),
                        rs.getString("concert_title"),
                        rs.getString("performer_name"),
                        rs.getInt("total_tickets_sold")));
            }
        }
    } catch (SQLException e) {
        result.append("Error: ").append(e.getMessage());
    }
    return result.toString();
}

    // Method to generate the top sales report
public String topSales(int year) {
    StringBuilder result = new StringBuilder();
    result.append("\n--- Top Selling Concerts Report ---\n");

    String query = """
        SELECT 
            Concerts.concert_code,
            Concerts.performer_name,
            Concerts.concert_title,  -- Added the title column
            SUM(CASE 
                    WHEN Transactions.transaction_type = 'buy' THEN Transactions.total_amount 
                    ELSE 0 
                END) AS total_sales
        FROM Concerts
        LEFT JOIN Tickets ON Concerts.concert_code = Tickets.concert_code
        LEFT JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
        WHERE YEAR(Transactions.transaction_date) = ? OR Transactions.transaction_date IS NULL
        GROUP BY Concerts.concert_code, Concerts.performer_name, Concerts.concert_title
        ORDER BY total_sales DESC;
    """;

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, year);

        try (ResultSet rs = stmt.executeQuery()) {
            result.append(String.format("\n%-15s %-25s %-30s %-15s\n", "Concert Code", "Performer", "Concert Title", "Total Sales"));
            result.append("---------------------------------------------------------------------\n");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                result.append(String.format("%-15d %-25s %-30s ₱%-15.2f\n",
                        rs.getInt("concert_code"),
                        rs.getString("performer_name"),
                        rs.getString("concert_title"),  // Display concert title
                        rs.getDouble("total_sales")));
            }

            if (!hasResults) {
                result.append("No sales data found for the given year.\n");
            }
        }
    } catch (SQLException e) {
        result.append("Error generating top sales report: ").append(e.getMessage()).append("\n");
    }

    return result.toString();  // Return the result as a string
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