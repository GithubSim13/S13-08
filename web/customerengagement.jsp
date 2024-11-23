<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertreports.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Customer Engagement Report</title>
        <style>
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                padding: 8px;
                text-align: left;
                border: 1px solid #ddd;
            }
            th {
                background-color: #f2f2f2;
            }
        </style>
    </head>
    <body>
        <h1>Customer Engagement Report</h1>

        <!-- Form to get the Year from the user -->
        <form method="get" action="customerengagement.jsp">
            <label for="year">Enter Year:</label>
            <input type="text" id="year" name="year" required/>
            <input type="submit" value="Generate Report"/>
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
                        // Create an instance of CustomerEngagementReport
                        ConcertReports concertReports = new ConcertReports(connection);

                        // Fetch customer engagement report and display it
                        String report = concertReports.customerEngagement(year);
                        out.println(report);
                    } else {
                        out.println("<h2>Error: Could not connect to the database. Please try again later.</h2>");
                    }
                } catch (NumberFormatException e) {
                    out.println("<h2>Invalid Year. Please enter a valid numeric value.</h2>");
                }
            } else {
                out.println("<h2>Please enter a Year to generate the report.</h2>");
            }
        %>
    </body>
</html>
