package MyPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class MyServlet
 */
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String apiKey = "8d386ff22ea406263e559e71def6ef04";
        String cityName = request.getParameter("city");
        String city = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        String urlApi = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

        URL url = new URL(urlApi);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Reading the data from the network request that we sent above 
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            // Storing the data in String
            StringBuilder responseContent = new StringBuilder();

            // Scanning the data to store
            Scanner scanner = new Scanner(reader);

            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }
            scanner.close();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

            // Date and Time
            long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
            String date = new Date(dateTimestamp).toString();

            // Temperature
            double temperature = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int tempFahrenheit = (int) ((temperature - 273.15) * 1.8 + 32);

            // Humidity
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();

            // Wind Speed
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();

            // Weather Condition
            String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

            request.setAttribute("date", date);
            request.setAttribute("city", cityName);
            request.setAttribute("temperature", tempFahrenheit);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("weatherData", responseContent.toString());
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            // Handle invalid city name
            System.out.println("Invalid city name: " + cityName);
            // Sending the request to the Error.jsp page for rendering
            request.getRequestDispatcher("Error.jsp").forward(request, response);
         
        } else {
            // Handle other errors
            InputStream errorStream = connection.getErrorStream();
            InputStreamReader errorReader = new InputStreamReader(errorStream);
            StringBuilder errorContent = new StringBuilder();
            Scanner errorScanner = new Scanner(errorReader);

            while (errorScanner.hasNext()) {
                errorContent.append(errorScanner.nextLine());
            }
            errorScanner.close();

            System.out.print("Error response: " + errorContent);
            response.getWriter().write("Error response: " + errorContent.toString());
        }

        connection.disconnect();
        // Sending the request to the index.jsp page for rendering
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
