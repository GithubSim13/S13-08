<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertrecords.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ticket Transfer</title>
    </head>
    <body>
        <h1>Transfer Tickets</h1>

        <!-- Form to input customer codes and ticket IDs -->
        <form method="post" action="tickettransferring.jsp">
            <label for="oldCustomerCode">Enter Old Customer ID:</label>
            <input type="text" id="oldCustomerCode" name="oldCustomerCode" required/><br/>

            <label for="newCustomerCode">Enter New Customer ID:</label>
            <input type="text" id="newCustomerCode" name="newCustomerCode" required/><br/>

            <label for="ticketIds">Enter Ticket IDs (comma-separated):</label>
            <input type="text" id="ticketIds" name="ticketIds" required/><br/>

            <label for="paymentMethod">Payment Method:</label>
            <select id="paymentMethod" name="paymentMethod" required>
                <option value="cash">Cash</option>
                <option value="bank_transfer">Bank Transfer</option>
                <option value="card">Card</option>
            </select><br/>

            <input type="submit" value="Transfer Tickets"/>
        </form>

        <%
            // Get form parameters from the request
            String oldCustomerCodeParam = request.getParameter("oldCustomerCode");
            String newCustomerCodeParam = request.getParameter("newCustomerCode");
            String ticketIdsParam = request.getParameter("ticketIds");
            String paymentMethod = request.getParameter("paymentMethod");

            if (oldCustomerCodeParam != null && newCustomerCodeParam != null && ticketIdsParam != null && paymentMethod != null) {
                try {
                    // Parse the customer codes and ticket IDs
                    int oldCustomerCode = Integer.parseInt(oldCustomerCodeParam);
                    int newCustomerCode = Integer.parseInt(newCustomerCodeParam);

                    // Split and parse ticket IDs
                    String[] ticketStrings = ticketIdsParam.split(",");
                    int[] ticketIds = new int[ticketStrings.length];
                    for (int i = 0; i < ticketStrings.length; i++) {
                        ticketIds[i] = Integer.parseInt(ticketStrings[i].trim());
                    }

                    // Establish the database connection
                    Connection connection = ConcertRecords.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertTransactions
                        ConcertTransactions transactions = new ConcertTransactions(connection);

                        // Perform the ticket transfer
                        boolean success = transactions.transferTickets(oldCustomerCode, newCustomerCode, ticketIds, paymentMethod);

                        // Display success or failure message
                        if (success) {
                            out.println("<h2>Ticket transfer was successful!</h2>");
                        } else {
                            out.println("<h2>There was an error during the ticket transfer. Please try again.</h2>");
                        }
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid input. Please enter valid numeric values for customer IDs and ticket IDs.</h2>");
                } catch (Exception e) {
                    out.println("<h2>Error: " + e.getMessage() + "</h2>");
                }
            } else {
                out.println("<h2>Please fill in all the fields to transfer tickets.</h2>");
            }
        %>
    </body>
</html>
