<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertrecords.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Customer Records</title>
    </head>
    <body>
        <h1>Customer Record Lookup</h1>

        <!-- Form to get the Customer ID from the user -->
        <form method="get" action="customerrecords.jsp">
            <label for="customerId">Enter Customer ID:</label>
            <input type="text" id="customerId" name="customerId" required/>
            <input type="submit" value="Submit"/>
        </form>

        <%
            // Get the customer ID from the request parameter
            String customerIdParam = request.getParameter("customerId");
            if (customerIdParam != null && !customerIdParam.isEmpty()) {
                try {
                    int customerId = Integer.parseInt(customerIdParam);

                    // Establish the database connection
                    Connection connection = ConcertRecords.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertRecords
                        ConcertRecords concertRecords = new ConcertRecords(connection);

                        // Fetch customer details and display them
                        String customerDetails = concertRecords.customerRecord(customerId);
                        out.println(customerDetails);
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid Customer ID. Please enter a valid numeric value.</h2>");
                }
            } else {
                out.println("<h2>Please enter a Customer ID to search.</h2>");
            }
        %>
    </body>
</html>
