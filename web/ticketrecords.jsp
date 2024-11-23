<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertrecords.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ticket Records</title>
    </head>
    <body>
        <h1>Ticket Record Lookup</h1>

        <!-- Form to get the Ticket Code from the user -->
        <form method="get" action="ticketrecords.jsp">
            <label for="ticketCode">Enter Ticket Code:</label>
            <input type="text" id="ticketCode" name="ticketCode" required/>
            <input type="submit" value="Submit"/>
        </form>

        <%
            // Get the ticket code from the request parameter
            String ticketCodeParam = request.getParameter("ticketCode");
            if (ticketCodeParam != null && !ticketCodeParam.isEmpty()) {
                try {
                    int ticketCode = Integer.parseInt(ticketCodeParam);

                    // Establish the database connection
                    Connection connection = ConcertRecords.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertRecords
                        ConcertRecords concertRecords = new ConcertRecords(connection);

                        // Fetch ticket details and display them
                        String ticketDetails = concertRecords.ticketRecord(ticketCode);
                        out.println(ticketDetails);
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid Ticket Code. Please enter a valid numeric value.</h2>");
                }
            } else {
                out.println("<h2>Please enter a Ticket Code to search.</h2>");
            }
        %>
    </body>
</html>
