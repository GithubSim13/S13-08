<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertrecords.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Concert Records</title>
    </head>
    <body>
        <h1>Concert Record Lookup</h1>

        <!-- Form to get the Concert ID from the user -->
        <form method="get" action="concertrecords.jsp">
            <label for="concertId">Enter Concert ID:</label>
            <input type="text" id="concertId" name="concertId" required/>
            <input type="submit" value="Submit"/>
        </form>

        <%
            // Get the concert ID from the request parameter
            String concertIdParam = request.getParameter("concertId");
            if (concertIdParam != null && !concertIdParam.isEmpty()) {
                try {
                    int concertId = Integer.parseInt(concertIdParam);

                    // Establish the database connection
                    Connection connection = ConcertRecords.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertRecords
                        ConcertRecords concertRecords = new ConcertRecords(connection);

                        // Fetch concert details and display them
                        String concertDetails = concertRecords.concertRecord(concertId);
                        out.println(concertDetails);
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid Concert ID. Please enter a valid numeric value.</h2>");
                }
            } else {
                out.println("<h2>Please enter a Concert ID to search.</h2>");
            }
        %>
    </body>
</html>
