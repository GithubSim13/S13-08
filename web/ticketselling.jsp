<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concerttransactions.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ticket Selling</title>
    </head>
    <body>
        <h1>Ticket Selling</h1>

        <!-- Form to get Customer ID, Concert ID, Seat Type, and Seat Number -->
        <form method="post" action="ticketSelling.jsp">
            <label for="customerId">Customer ID:</label>
            <input type="text" id="customerId" name="customerId" required /><br/>

            <label for="concertId">Concert ID:</label>
            <input type="text" id="concertId" name="concertId" required /><br/>

            <label for="seatType">Seat Type:</label>
            <select id="seatType" name="seatType" required>
                <option value="">Select a concert first</option>
                <% 
                    String concertIdParam = request.getParameter("concertId");
                    if (concertIdParam != null && !concertIdParam.isEmpty()) {
                        int concertId = Integer.parseInt(concertIdParam);
                        Connection connection = ConcertTransactions.getConnection();
                        if (connection != null) {
                            ConcertTransactions transactions = new ConcertTransactions(connection);
                            try {
                                // Get available seat types and prices for the concert
                                List<Map<String, Object>> seatTypes = transactions.getSeatTypes(concertId);
                                if (seatTypes != null && !seatTypes.isEmpty()) {
                                    for (Map<String, Object> seat : seatTypes) {
                                        String seatType = (String) seat.get("seatType");
                                        double price = (Double) seat.get("price");
                                        out.println("<option value=\"" + seatType + "\">" + seatType + " - ₱" + price + "</option>");
                                    }
                                }
                            } catch (SQLException e) {
                                out.println("<option value=\"\">Error fetching seat types</option>");
                            }
                        }
                    }
                %>
            </select><br/>

            <label for="seatNumber">Seat Number:</label>
            <input type="text" id="seatNumber" name="seatNumber" required /><br/>

            <label for="paymentMethod">Payment Method:</label>
            <select id="paymentMethod" name="paymentMethod" required>
                <option value="cash">Cash</option>
                <option value="bank_transfer">Bank Transfer</option>
                <option value="card">Card</option>
            </select><br/><br/>

            <input type="submit" value="Sell Ticket" />
        </form>

        <% 
            // Handle form submission
            String customerIdParam = request.getParameter("customerId");
            String seatTypeParam = request.getParameter("seatType");
            String seatNumberParam = request.getParameter("seatNumber");
            String paymentMethodParam = request.getParameter("paymentMethod");

            if (customerIdParam != null && seatTypeParam != null && seatNumberParam != null && paymentMethodParam != null) {
                try {
                    int customerId = Integer.parseInt(customerIdParam);
                    int concertId = Integer.parseInt(request.getParameter("concertId"));

                    // Establish the database connection
                    Connection connection = ConcertTransactions.getConnection();
                    if (connection != null) {
                        ConcertTransactions transactions = new ConcertTransactions(connection);

                        // Step 1: Check if the seat is available
                        boolean isSeatAvailable = transactions.isSeatAvailable(concertId, seatNumberParam);
                        if (!isSeatAvailable) {
                            out.println("<h2>Error: Seat is already taken.</h2>");
                            return;
                        }

                        // Step 2: Check if the customer is banned
                        boolean isBanned = transactions.isCustomerBanned(customerId);
                        if (isBanned) {
                            out.println("<h2>Error: Customer is banned and cannot purchase a ticket.</h2>");
                            return;
                        }

                        // Step 3: Check if customer meets age requirements
                        boolean meetsAgeRequirement = transactions.checkCustomerAge(customerId, concertId);
                        if (!meetsAgeRequirement) {
                            out.println("<h2>Error: Customer does not meet the age requirements for this concert.</h2>");
                            return;
                        }

                        // Step 4: Retrieve ticket price for the selected seat type
                        double ticketPrice = transactions.getTicketPrice(concertId, seatTypeParam);

                        // Step 5: Process the ticket sale (insert the transaction and ticket records)
                        transactions.sellTicket(customerId, concertId, seatTypeParam, seatNumberParam, ticketPrice, paymentMethodParam);

                        out.println("<h2>Ticket sold successfully!</h2>");
                        out.println("<p>Concert ID: " + concertId + "</p>");
                        out.println("<p>Seat Number: " + seatNumberParam + "</p>");
                        out.println("<p>Seat Type: " + seatTypeParam + "</p>");
                        out.println("<p>Payment Method: " + paymentMethodParam + "</p>");
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid input. Please ensure all fields contain valid values.</h2>");
                } catch (SQLException e) {
                    out.println("<h2>Error processing ticket sale: " + e.getMessage() + "</h2>");
                }
            }
        %>
    </body>
</html>
