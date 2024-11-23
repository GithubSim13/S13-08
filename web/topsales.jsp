<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertreports.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top Sales Report</title>
        <style>
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 20px;
            }
            th, td {
                padding: 10px;
                text-align: left;
                border: 1px solid #ddd;
            }
            th {
                background-color: #f2f2f2;
            }
        </style>
    </head>
    <body>
        <h1>Top Selling Concerts Report</h1>

        <!-- Form to get the Year from the user -->
        <form method="get" action="topsales.jsp">
            <label for="year">Enter Year:</label>
            <input type="text" id="year" name="year" required/>
            <input type="submit" value="Submit"/>
        </form>

        <%
            // Get the year from the request parameter
            String yearParam = request.getParameter("year");
            if (yearParam != null && !yearParam.isEmpty()) {
                try {
                    int year = Integer.parseInt(yearParam);

                    // Establish the database connection
                    Connection connection = ConcertReports.getConnection();
                    if (connection != null) {
                        // Create an instance of ConcertReports
                        ConcertReports concertReports = new ConcertReports(connection);

                        // Fetch top sales report for the specified year
                        String topSales = concertReports.topSales(year);
                        // Convert the output into a table format
                        String[] lines = topSales.split("\n");
                        out.println("<table>");
                        for (String line : lines) {
                            // Skip empty lines or lines that only contain whitespace or header lines
                            if (line.trim().isEmpty() || line.contains("---")) {
                                continue; // Skip empty or header lines
                            }
                            String[] columns = line.split("\\s{2,}"); // Split by multiple spaces for columns
                            if (columns.length > 1) {
                                out.println("<tr>");
                                for (String col : columns) {
                                    out.println("<td>" + col.trim() + "</td>");
                                }
                                out.println("</tr>");
                            }
                        }
                        out.println("</table>");
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid Year. Please enter a valid numeric value.</h2>");
                }
            } else {
                out.println("<h2>Please enter a Year to search.</h2>");
            }
        %>
    </body>
</html>
