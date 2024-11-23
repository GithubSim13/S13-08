package concerttransactions;

import java.util.Scanner;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ConcertTransactions {

    private Connection connection;

    // Constructor to initialize the connection
    public ConcertTransactions(Connection connection) {
        this.connection = connection;
    }
    
    // Method to check if customer is banned
    public boolean isCustomerBanned(int customerId) throws SQLException {
        String query = "SELECT 1 FROM Bans WHERE customer_code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // Returns true if customer is banned
            }
        }
    }

    // Method to check if customer meets age restrictions for a concert
    public boolean checkCustomerAge(int customerId, int concertId) throws SQLException {
        String query = """
            SELECT 1
            FROM Customers cm
            JOIN Concerts cr ON cr.concert_code = ? 
            WHERE cm.customer_code = ? 
              AND (cr.entry_restrictions != '18+' 
                   OR (cr.entry_restrictions = '18+' 
                       AND TIMESTAMPDIFF(YEAR, cm.birth_date, CURDATE()) >= 18));
        """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, concertId);
            stmt.setInt(2, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // Returns true if customer meets age restrictions
            }
        }
    }

    // Method to retrieve available seat types for a concert
    public List<String> getSeatTypes(int concertId) throws SQLException {
        String query = "SELECT ticket_type FROM Prices WHERE concert_code = ?";
        List<String> seatTypes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, concertId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seatTypes.add(rs.getString("ticket_type"));
                }
            }
        }
        return seatTypes;
    }

public double getTicketPrice(int concertId, String seatType) throws SQLException {
    String query = "SELECT price FROM TicketPrices WHERE concert_id = ? AND seat_type = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, concertId);
        stmt.setString(2, seatType);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("price");
            } else {
                throw new SQLException("No price found for the specified concert and seat type.");
            }
        }
    }
}



    // Method to check seat availability
    public boolean isSeatAvailable(int concertId, String seatNumber) throws SQLException {
        String query = "SELECT 1 FROM Tickets WHERE concert_code = ? AND seat_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, concertId);
            stmt.setString(2, seatNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();  // Returns true if seat is available
            }
        }
    }

// Method to get the latest transaction code for a customer and concert
public int getLatestTransactionCode(int customerId, int concertId) throws SQLException {
    String query = """
        SELECT MAX(t.transaction_code) 
        FROM Transactions t
        JOIN Tickets tk ON t.transaction_code = tk.transaction_code
        WHERE t.customer_code = ? AND tk.concert_code = ?
    """;
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, customerId);
        stmt.setInt(2, concertId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }
    return -1; // Return -1 if no transaction code is found
}

public void sellTicket(int customerId, int concertId, String seatType, String seatNumber, double ticketPrice, String paymentMethod) throws SQLException {
    // Insert the transaction into the Transactions table
    String insertTransactionQuery = "INSERT INTO Transactions (customer_id, concert_id, payment_method, amount) VALUES (?, ?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, customerId);
        stmt.setInt(2, concertId);
        stmt.setString(3, paymentMethod);
        stmt.setDouble(4, ticketPrice);
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating transaction failed, no rows affected.");
        }

        // Get the generated transaction ID
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                int transactionId = generatedKeys.getInt(1);

                // Insert the ticket into the Tickets table
                String insertTicketQuery = "INSERT INTO Tickets (concert_id, seat_number, seat_type, transaction_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ticketStmt = connection.prepareStatement(insertTicketQuery)) {
                    ticketStmt.setInt(1, concertId);
                    ticketStmt.setString(2, seatNumber);
                    ticketStmt.setString(3, seatType);
                    ticketStmt.setInt(4, transactionId);
                    ticketStmt.executeUpdate();
                }
            } else {
                throw new SQLException("Creating transaction failed, no ID obtained.");
            }
        }
    }
}

    
public void insertRefundRecord(int customerId, int ticketId, String paymentMethod) throws SQLException {
    final double REFUND_FEE_PERCENTAGE = 0.10;

    // Step 1: Fetch the ticket price to calculate the refund fee
    String fetchTicketPriceQuery = """
        SELECT ticket_price
        FROM Tickets
        WHERE ticket_code = ?;
    """;

    double ticketPrice;
    try (PreparedStatement stmt = connection.prepareStatement(fetchTicketPriceQuery)) {
        stmt.setInt(1, ticketId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                ticketPrice = rs.getDouble("ticket_price");
            } else {
                throw new SQLException("Ticket not found for ID: " + ticketId);
            }
        }
    }

    double refundFee = ticketPrice * REFUND_FEE_PERCENTAGE;

    // Step 2: Insert the refund transaction into Transactions
    String insertTransactionQuery = """
        INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount, payment_method)
        VALUES (?, 'refund', CURRENT_TIMESTAMP, ?, ?);
    """;

    int transactionId;
    try (PreparedStatement stmt = connection.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, customerId);
        stmt.setDouble(2, refundFee); // Refund fee
        stmt.setString(3, paymentMethod);
        stmt.executeUpdate();

        // Retrieve the generated transaction ID
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                transactionId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve transaction ID for refund.");
            }
        }
    }

    // Step 3: Insert the ticket refund record into Refunds
    String insertRefundQuery = """
        INSERT INTO Refunds (transaction_code, ticket_code)
        VALUES (?, ?);
    """;
    try (PreparedStatement stmt = connection.prepareStatement(insertRefundQuery)) {
        stmt.setInt(1, transactionId); // Link to the refund transaction
        stmt.setInt(2, ticketId); // Ticket being refunded
        stmt.executeUpdate();
    }

    System.out.printf("Refund processed for Ticket ID: %d. Refund fee: ₱%.2f\n", ticketId, refundFee);
}

    
    private void refundTickets() {
    System.out.println("\n--- Refund Tickets ---");

    // Using Scanner for input instead of MyJDBC
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Customer ID: ");
    int customerId = scanner.nextInt();

    System.out.print("Enter Ticket IDs to refund (comma-separated): ");
    scanner.nextLine(); // Consume newline left by nextInt
    String ticketIdsInput = scanner.nextLine();
    String[] ticketIds = ticketIdsInput.split(",");

    final double REFUND_FEE_PERCENTAGE = 0.10; // 10% fee
    double totalRefundFee = 0.0; // To track total refund fee

    System.out.print("Enter payment method for the refund fee (cash, bank_transfer, card): ");
    String paymentMethod = scanner.nextLine();

    // Using StringBuilder for result formatting
    StringBuilder result = new StringBuilder();
    result.append("\nProcessing Refund for Customer ID: ").append(customerId).append("\n");

    try {
        connection.setAutoCommit(false);

        // Step 1: Check if the customer is in the Bans table
        String banCheckQuery = "SELECT * FROM Bans WHERE customer_code = ?";
        try (PreparedStatement banCheckStmt = connection.prepareStatement(banCheckQuery)) {
            banCheckStmt.setInt(1, customerId);
            try (ResultSet banCheckRs = banCheckStmt.executeQuery()) {
                if (banCheckRs.next()) {
                    result.append("Customer is banned. Refund cannot be processed.");
                    connection.rollback();
                    return;
                }
            }
        }

        // Step 2: Validate ticket ownership by checking if the ticket was originally bought by the customer
        String validateTicketQuery = """
            SELECT t.ticket_price, tr.transaction_code, t.ticket_code
            FROM Tickets t
            JOIN Transactions tr ON t.transaction_code = tr.transaction_code
            WHERE t.ticket_code = ? AND tr.customer_code = ? AND tr.transaction_type = 'buy';
        """;
        try (PreparedStatement validateStmt = connection.prepareStatement(validateTicketQuery)) {
            for (String ticketId : ticketIds) {
                validateStmt.setInt(1, Integer.parseInt(ticketId.trim()));
                validateStmt.setInt(2, customerId);
                try (ResultSet rs = validateStmt.executeQuery()) {
                    if (rs.next()) {
                        double ticketPrice = rs.getDouble("ticket_price");
                        int originalTransactionCode = rs.getInt("transaction_code");
                        int ticketCode = rs.getInt("ticket_code");

                        // Calculate refund fee
                        double refundFee = ticketPrice * REFUND_FEE_PERCENTAGE;
                        totalRefundFee += refundFee;

                        // Step 3: Insert into Refunds table
                        String insertRefundQuery = "INSERT INTO Refunds (transaction_code, ticket_code) VALUES (?, ?)";
                        try (PreparedStatement refundStmt = connection.prepareStatement(insertRefundQuery)) {
                            refundStmt.setInt(1, originalTransactionCode); // Original transaction code
                            refundStmt.setInt(2, ticketCode); // Ticket code
                            refundStmt.executeUpdate();
                        }

                    } else {
                        result.append(String.format("Ticket ID %s is either not owned by you or has already been refunded. Aborting transaction.\n", ticketId));
                        connection.rollback();
                        return;
                    }
                }
            }
        }

        // Step 4: Record the refund transaction in the Transactions table
        String recordTransactionQuery = """
            INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount, payment_method)
            VALUES (?, 'refund', CURRENT_TIMESTAMP, ?, ?);
        """;
        try (PreparedStatement recordStmt = connection.prepareStatement(recordTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
            recordStmt.setInt(1, customerId);
            recordStmt.setDouble(2, totalRefundFee); // Only the refund fee
            recordStmt.setString(3, paymentMethod);
            recordStmt.executeUpdate();
        }

        connection.commit();
        result.append(String.format("Refund successful. Total refund fee charged: ₱%.2f.\n", totalRefundFee));
    } catch (SQLException e) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            result.append("Rollback failed: ").append(rollbackEx.getMessage()).append("\n");
        }
        result.append("Error processing refund: ").append(e.getMessage()).append("\n");
    } finally {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            result.append("Error resetting auto-commit: ").append(e.getMessage()).append("\n");
        }
    }

    // Output result to the console
    System.out.println(result.toString());
}
    
    public void cancelConcert(int concertCode) {
        final double REFUND_FEE_PERCENTAGE = 0.10; // Refund fee of 10%

        try {
            connection.setAutoCommit(false); // Disable auto-commit for transaction management

            // Step 1: Check if concert code is valid
            String validateConcertQuery = "SELECT COUNT(*) AS concert_count FROM Concerts WHERE concert_code = ?";
            try (PreparedStatement ps = connection.prepareStatement(validateConcertQuery)) {
                ps.setInt(1, concertCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("concert_count") == 0) {
                        System.err.println("No concert found with the provided Concert Code.");
                        return; // Exit if the concert does not exist
                    }
                }
            }

            // Step 2: Update concert status to 'cancelled'
            String updateConcertQuery = "UPDATE Concerts SET status = 'cancelled' WHERE concert_code = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateConcertQuery)) {
                ps.setInt(1, concertCode);
                ps.executeUpdate();
                System.out.println("Concert status updated to 'cancelled'.");
            }

            // Step 3: Process refunds for all tickets associated with the cancelled concert
            String selectTicketsQuery = """
            SELECT Tickets.ticket_code, Tickets.ticket_price, Transactions.customer_code
            FROM Tickets
            JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
            WHERE Tickets.concert_code = ?;
            """;
            try (PreparedStatement ps = connection.prepareStatement(selectTicketsQuery)) {
                ps.setInt(1, concertCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.isBeforeFirst()) { // Check if no tickets exist
                        System.out.println("No tickets found for the specified concert.");
                    }

                    while (rs.next()) {
                        double ticketPrice = rs.getDouble("ticket_price");
                        int customerCode = rs.getInt("customer_code");

                        // Calculate refund amount after applying the refund fee
                        double refundAmount = ticketPrice * (1 - REFUND_FEE_PERCENTAGE);

                        // Step 4: Record the refund transaction
                        String recordTransactionQuery = """
                        INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount)
                        VALUES (?, 'refund', CURRENT_TIMESTAMP, ?);
                        """;
                        try (PreparedStatement transactionPs = connection.prepareStatement(recordTransactionQuery)) {
                            transactionPs.setInt(1, customerCode); // Set customer_code parameter
                            transactionPs.setDouble(2, refundAmount); // Set refundAmount parameter
                            transactionPs.executeUpdate(); // Execute insert query for refund
                        }
                    }
                }
            }

            // Step 5: Update the venue's availability to 'available'
            String updateVenueAvailabilityQuery = """
            UPDATE AvailableVenues
            SET availability = 'available'
            WHERE venue_code = (SELECT venue_code FROM Concerts WHERE concert_code = ?);
            """;
            try (PreparedStatement ps = connection.prepareStatement(updateVenueAvailabilityQuery)) {
                ps.setInt(1, concertCode);
                ps.executeUpdate();
                System.out.println("Venue availability updated to 'available'.");
            }

            // Step 6: Commit all changes
            connection.commit();
            System.out.println("Concert cancelled, refund transactions recorded, and venue set to available.");

        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction if any error occurs
                System.err.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
    
    // Method to handle the menu for transferring tickets
    private void transferTicketsMenu() {
        try {
            Scanner scanner = new Scanner(System.in);

            // Get user input for customer codes and ticket IDs
            System.out.print("Enter current customer's ID: ");
            int oldCustomerCode = scanner.nextInt();
            System.out.print("Enter new customer's ID: ");
            int newCustomerCode = scanner.nextInt();

            System.out.println("Enter ticket IDs separated by commas (e.g., 1,2,3): ");
            String ticketInput = scanner.next();
            String[] ticketStrings = ticketInput.split(",");
            int[] ticketIds = new int[ticketStrings.length];
            for (int i = 0; i < ticketStrings.length; i++) {
                ticketIds[i] = Integer.parseInt(ticketStrings[i].trim());
            }

            System.out.print("Enter payment method (cash, bank_transfer, card): ");
            String paymentMethod = scanner.next();

            // Call the transferTickets method
            transferTickets(oldCustomerCode, newCustomerCode, ticketIds, paymentMethod);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ticket IDs. Please enter numbers separated by commas.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    // Method to perform the actual ticket transfer
    public void transferTickets(int oldCustomerCode, int newCustomerCode, int[] ticketCodes, String paymentMethod) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // Step 1: Verify the old customer owns the tickets and check the last transaction status
            String checkOwnershipQuery = """
            SELECT T.ticket_code, Tr.transaction_code, Tr.transaction_type
            FROM Tickets T
            JOIN Transactions Tr ON T.transaction_code = Tr.transaction_code
            WHERE Tr.customer_code = ? AND T.ticket_code = ?;
            """;

            // Step 2: Insert the new transaction record
            String insertTransactionQuery = """
            INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount, payment_method)
            VALUES (?, 'transfer', CURRENT_TIMESTAMP, 100.00, ?);
            """;

            // Step 3: Update the ticket record with the new customer and transaction
            String updateTicketQuery = """
            UPDATE Tickets
            SET transaction_code = ?
            WHERE ticket_code = ? AND transaction_code = ?;
            """;

            int newTransactionCode;

            for (int ticketCode : ticketCodes) {
                int oldTransactionCode;

                // Step 1: Verify ownership and check if the ticket has already been transferred
                try (PreparedStatement checkStmt = connection.prepareStatement(checkOwnershipQuery)) {
                    checkStmt.setInt(1, oldCustomerCode);
                    checkStmt.setInt(2, ticketCode);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("Ticket code " + ticketCode + " not found for customer " + oldCustomerCode);
                            connection.rollback();
                            return;
                        }

                        // Fetch transaction details
                        oldTransactionCode = rs.getInt("transaction_code");
                        String transactionType = rs.getString("transaction_type");

                        if ("transfer".equalsIgnoreCase(transactionType)) {
                            System.out.println("Ticket code " + ticketCode + " has already been transferred and cannot be transferred again.");
                            connection.rollback();
                            return;
                        }

                        System.out.println("Ownership verified for Ticket Code: " + ticketCode);
                    }
                }

                // Step 2: Create a new transaction for the transfer
                try (PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
                    transactionStmt.setInt(1, newCustomerCode);
                    transactionStmt.setString(2, paymentMethod);
                    transactionStmt.executeUpdate();

                    try (ResultSet generatedKeys = transactionStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newTransactionCode = generatedKeys.getInt(1);
                            System.out.println("New transaction created with transaction code: " + newTransactionCode);
                        } else {
                            System.out.println("Failed to create transaction record.");
                            connection.rollback();
                            return;
                        }
                    }
                }

                // Step 3: Update the ticket record with the new transaction code
                try (PreparedStatement updateTicketStmt = connection.prepareStatement(updateTicketQuery)) {
                    updateTicketStmt.setInt(1, newTransactionCode);
                    updateTicketStmt.setInt(2, ticketCode);
                    updateTicketStmt.setInt(3, oldTransactionCode);

                    int affectedRows = updateTicketStmt.executeUpdate();
                    if (affectedRows == 0) {
                        System.out.println("Failed to transfer Ticket Code: " + ticketCode);
                        connection.rollback();
                        return;
                    }
                    System.out.println("Ticket Code " + ticketCode + " successfully transferred.");
                }
            }

            connection.commit(); // Commit transaction
            System.out.println("Ticket transfer successful!");

        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error transferring tickets: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Restore default auto-commit
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    // Example main method to invoke transferTicketsMenu
    public static void main(String[] args) {
        Connection connection = ConcertTransactions.getConnection();
        if (connection != null) {
            ConcertTransactions transactions = new ConcertTransactions(connection);
            transactions.transferTicketsMenu(); // Initiate the transfer process
        } else {
            System.out.println("Could not connect to the database.");
        }
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