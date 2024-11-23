<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertrecords.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Transaction Records</title>
    </head>
    <body>
        <h1>Transaction Record Lookup</h1>

        <!-- Form to get the Transaction Code from the user -->
        <form method="get" action="transactionrecords.jsp">
            <label for="transactionCode">Enter Transaction Code:</label>
            <input type="text" id="transactionCode" name="transactionCode" required/>
            <input type="submit" value="Submit"/>
        </form>

        <%
            // Get the transaction code from the request parameter
            String transactionCodeParam = request.getParameter("transactionCode");
            if (transactionCodeParam != null && !transactionCodeParam.isEmpty()) {
                try {
                    int transactionCode = Integer.parseInt(transactionCodeParam);

                    // Establish the database connection
                    Connection connection = ConcertRecords.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertRecords
                        ConcertRecords concertRecords = new ConcertRecords(connection);

                        // Fetch transaction details and display them
                        String transactionDetails = concertRecords.transactionRecord(transactionCode);
                        out.println(transactionDetails);
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid Transaction Code. Please enter a valid numeric value.</h2>");
                }
            } else {
                out.println("<h2>Please enter a Transaction Code to search.</h2>");
            }
        %>
    </body>
</html>
