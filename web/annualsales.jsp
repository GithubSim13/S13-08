<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, concertreports.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Annual Sales Report</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f9f9f9;
                color: #333;
                margin: 0;
                padding: 0;
            }
            h1 {
                background-color: #2c3e50;
                color: white;
                text-align: center;
                padding: 20px;
                margin: 0;
            }
            h2 {
                color: #2c3e50;
                text-align: center;
            }
            .container {
                width: 80%;
                margin: 20px auto;
                padding: 20px;
                background-color: white;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                border-radius: 8px;
            }
            form {
                text-align: center;
                margin-bottom: 30px;
            }
            form label {
                font-size: 16px;
                margin-right: 10px;
            }
            form input[type="number"] {
                padding: 5px;
                font-size: 14px;
                width: 120px;
            }
            form input[type="submit"] {
                padding: 8px 20px;
                font-size: 14px;
                background-color: #3498db;
                color: white;
                border: none;
                cursor: pointer;
                border-radius: 4px;
                transition: background-color 0.3s ease;
            }
            form input[type="submit"]:hover {
                background-color: #2980b9;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 30px;
            }
            th, td {
                padding: 12px 15px;
                text-align: left;
                border: 1px solid #ddd;
            }
            th {
                background-color: #3498db;
                color: white;
            }
            tr:nth-child(even) {
                background-color: #f2f2f2;
            }
            .total-row {
                font-weight: bold;
                background-color: #ecf0f1;
            }
            .error-message {
                color: #e74c3c;
                text-align: center;
                margin-top: 30px;
                font-size: 18px;
            }
        </style>
    </head>
    <body>
        <h1>Annual Sales Report</h1>

        <div class="container">
            <!-- Form to get the year for the sales report -->
            <form method="get" action="annualsales.jsp">
                <label for="year">Enter Year:</label>
                <input type="number" id="year" name="year" required min="2000" max="2100" />
                <input type="submit" value="Generate Report" />
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

                            // Fetch the annual sales report for the given year
                            String annualSalesReport = concertReports.annualSales(year);
                            out.println(annualSalesReport);
                        } else {
                            out.println("<h2 class='error-message'>Error: Could not connect to the database. Please try again later.</h2>");
                        }
                    } catch (NumberFormatException e) {
                        out.println("<h2 class='error-message'>Invalid Year. Please enter a valid numeric value.</h2>");
                    }
                } else {
                    out.println("<h2>Please enter a year to generate the sales report.</h2>");
                }
            %>
        </div>

    </body>
</html>
