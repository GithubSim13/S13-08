<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concerttransactions.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ticket Transferring</title>
    </head>
    <body>
        <h1>Ticket Transferring</h1>

        <!-- Form to get old and new customer IDs, ticket IDs, and payment method -->
        <form method="post" action="tickettransferring.jsp">
            <label for="oldCustomerCode">Enter Current Customer's ID:</label>
            <input type="text" id="oldCustomerCode" name="oldCustomerCode" required /><br/>

            <label for="newCustomerCode">Enter New Customer's ID:</label>
            <input type="text" id="newCustomerCode" name="newCustomerCode" required /><br/>

            <label for="ticketCodes">Enter Ticket Codes (comma-separated):</label>
            <input type="text" id="ticketCodes" name="ticketCodes" required /><br/>

            <!-- Dropdown for Payment Method -->
            <label for="paymentMethod">Select Payment Method:</label>
            <select id="paymentMethod" name="paymentMethod" required>
                <option value="cash">Cash</option>
                <option value="bank_transfer">Bank Transfer</option>
                <option value="card">Card</option>
            </select><br/>

            <input type="submit" value="Transfer Tickets" />
        </form>

        <%
            // Check if the form was submitted and process the input
            String oldCustomerCodeParam = request.getParameter("oldCustomerCode");
            String newCustomerCodeParam = request.getParameter("newCustomerCode");
            String ticketCodesParam = request.getParameter("ticketCodes");
            String paymentMethod = request.getParameter("paymentMethod");

            if (oldCustomerCodeParam != null && newCustomerCodeParam != null && ticketCodesParam != null && paymentMethod != null) {
                try {
                    // Parse the input values
                    int oldCustomerCode = Integer.parseInt(oldCustomerCodeParam);
                    int newCustomerCode = Integer.parseInt(newCustomerCodeParam);

                    // Parse the ticket codes into an array
                    String[] ticketStrings = ticketCodesParam.split(",");
                    int[] ticketIds = new int[ticketStrings.length];
                    for (int i = 0; i < ticketStrings.length; i++) {
                        ticketIds[i] = Integer.parseInt(ticketStrings[i].trim());
                    }

                    // Establish the database connection
                    Connection connection = ConcertTransactions.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertTransactions
                        ConcertTransactions transactions = new ConcertTransactions(connection);

                        // Call the transferTickets method to perform the transfer
                        transactions.transferTickets(oldCustomerCode, newCustomerCode, ticketIds, paymentMethod);

                        // Display a success message
                        out.println("<h2>Ticket Transfer Completed Successfully!</h2>");
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid input. Please ensure the customer IDs and ticket codes are numeric values.</h2>");
                } catch (Exception e) {
                    out.println("<h2>An error occurred: " + e.getMessage() + "</h2>");
                }
            }
        %>
    </body>
</html>
