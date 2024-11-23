<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concerttransactions.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Concert Cancellation</title>
    </head>
    <body>
        <h1>Cancel Concert</h1>

        <!-- Form to get the Concert Code -->
        <form method="post" action="concertcancelling.jsp">
            <label for="concertCode">Enter Concert Code to Cancel:</label>
            <input type="text" id="concertCode" name="concertCode" required /><br/>
            <input type="submit" value="Cancel Concert" />
        </form>

        <%
            // Check if the form was submitted and process the input
            String concertCodeParam = request.getParameter("concertCode");

            if (concertCodeParam != null) {
                try {
                    // Parse the concert code
                    int concertCode = Integer.parseInt(concertCodeParam);

                    // Establish the database connection
                    Connection connection = ConcertTransactions.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertTransactions
                        ConcertTransactions transactions = new ConcertTransactions(connection);

                        // Call the cancelConcert method to perform the cancellation
                        transactions.cancelConcert(concertCode);

                        // Display a success message
                        out.println("<h2>Concert cancelled successfully, refunds processed, and venue marked as available!</h2>");
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid input. Please ensure the concert code is a numeric value.</h2>");
                } catch (Exception e) {
                    out.println("<h2>An error occurred: " + e.getMessage() + "</h2>");
                }
            }
        %>
    </body>
</html>
