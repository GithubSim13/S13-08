<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concerttransactions.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ticket Refunding</title>
    </head>
    <body>
        <h1>Ticket Refund</h1>

        <!-- Form to get Customer ID and Ticket IDs -->
        <form method="post" action="ticketrefunding.jsp">
            <label for="customerId">Customer ID:</label>
            <input type="text" id="customerId" name="customerId" required /><br/>

            <label for="ticketIds">Ticket IDs (comma-separated):</label>
            <input type="text" id="ticketIds" name="ticketIds" required /><br/>

            <label for="paymentMethod">Payment Method for Refund Fee:</label>
            <select id="paymentMethod" name="paymentMethod" required>
                <option value="cash">Cash</option>
                <option value="bank_transfer">Bank Transfer</option>
                <option value="card">Card</option>
            </select><br/><br/>

            <input type="submit" value="Process Refund" />
        </form>

        <% 
            // Check if the form was submitted
            String customerIdParam = request.getParameter("customerId");
            String ticketIdsParam = request.getParameter("ticketIds");
            String paymentMethodParam = request.getParameter("paymentMethod");

            if (customerIdParam != null && ticketIdsParam != null && paymentMethodParam != null) {
                try {
                    int customerId = Integer.parseInt(customerIdParam);
                    String[] ticketIdsStr = ticketIdsParam.split(",");
                    int[] ticketIds = new int[ticketIdsStr.length];
                    for (int i = 0; i < ticketIdsStr.length; i++) {
                        ticketIds[i] = Integer.parseInt(ticketIdsStr[i].trim());
                    }

                    // Establish the database connection
                    Connection connection = ConcertTransactions.getConnection();
                    if (connection != null) {
                        ConcertTransactions transactions = new ConcertTransactions(connection);

                        double totalRefundFee = 0.0;
                        StringBuilder refundResult = new StringBuilder("<h2>Refund Results:</h2>");

                        // Process each ticket refund
                        for (int ticketId : ticketIds) {
                            transactions.insertRefundRecord(customerId, ticketId, paymentMethodParam);
                            double ticketPrice = transactions.getTicketPrice(ticketId);
                            double refundFee = ticketPrice * 0.10; // 10% refund fee
                            totalRefundFee += refundFee;

                            refundResult.append("<p>Ticket ").append(ticketId)
                                        .append(" refunded successfully. Refund fee: ₱")
                                        .append(String.format("%.2f", refundFee)).append("</p>");
                        }

                        refundResult.append("<p>Total refund fee charged: ₱")
                                    .append(String.format("%.2f", totalRefundFee)).append("</p>");
                        out.println(refundResult.toString());
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid input. Please ensure all fields contain valid values.</h2>");
                } catch (SQLException e) {
                    out.println("<h2>Error processing refund: " + e.getMessage() + "</h2>");
                }
            }
        %>
    </body>
</html>
