import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class app {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Server started on port 8080");

        while (true) {
            try (Socket socket = server.accept()) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );
                OutputStream outputStream = socket.getOutputStream();
                String requestLine = in.readLine();

                while (in.ready()) {
                    String headerLine = in.readLine();
                    if (headerLine == null || headerLine.isEmpty()) {
                        break;
                    }
                }

                RequestData requestData = parseRequest(requestLine);
                String body = renderPage(requestData);

                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                    true
                );

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html; charset=UTF-8");
                out.println("Content-Length: " + bodyBytes.length);
                out.println();
                out.write(body);
                out.flush();
            }
        }
    }

    private static String renderPage(RequestData requestData) {
        if ("/calculator".equals(requestData.path)) {
            return renderCalculatorPage(requestData.queryParams);
        }

        return renderWelcomePage();
    }

    private static String renderWelcomePage() {
        return "<html><body>"
            + "<h1>Welcome to the Java Docker App</h1>"
            + "<p>Enter your name to continue to the calculator page.</p>"
            + "<form method=\"GET\" action=\"/calculator\">"
            + "<label for=\"name\">What is your name?</label><br><br>"
            + "<input id=\"name\" name=\"name\" type=\"text\" required />"
            + "<button type=\"submit\">Continue</button>"
            + "</form>"
            + "</body></html>";
    }

    private static String renderCalculatorPage(Map<String, String> queryParams) {
        String name = queryParams.getOrDefault("name", "").trim();
        String firstNumber = queryParams.getOrDefault("a", "");
        String secondNumber = queryParams.getOrDefault("b", "");
        String operation = queryParams.getOrDefault("op", "add");
        String resultMessage = "";

        if (name.isBlank()) {
            return "<html><body>"
                + "<h1>Name is required</h1>"
                + "<a href=\"/\">Go back</a>"
                + "</body></html>";
        }

        if (!firstNumber.isBlank() && !secondNumber.isBlank()) {
            try {
                double left = Double.parseDouble(firstNumber);
                double right = Double.parseDouble(secondNumber);
                double result = calculator.calculate(left, right, operation);
                resultMessage = "<p><strong>Result:</strong> " + result + "</p>";
            } catch (IllegalArgumentException exception) {
                resultMessage = "<p><strong>Error:</strong> "
                    + escapeHtml(exception.getMessage()) + "</p>";
            }
        }

        return "<html><body>"
            + "<h1>Hello, " + escapeHtml(name) + "!</h1>"
            + "<p>Welcome to the calculator page.</p>"
            + "<form method=\"GET\" action=\"/calculator\">"
            + "<input type=\"hidden\" name=\"name\" value=\"" + escapeHtml(name) + "\" />"
            + "<label for=\"a\">First number</label><br><br>"
            + "<input id=\"a\" name=\"a\" type=\"number\" step=\"any\" value=\"" + escapeHtml(firstNumber) + "\" required /><br><br>"
            + "<label for=\"b\">Second number</label><br><br>"
            + "<input id=\"b\" name=\"b\" type=\"number\" step=\"any\" value=\"" + escapeHtml(secondNumber) + "\" required /><br><br>"
            + "<label for=\"op\">Operation</label><br><br>"
            + "<select id=\"op\" name=\"op\">"
            + buildSelectedOption("add", operation, "Add")
            + buildSelectedOption("subtract", operation, "Subtract")
            + buildSelectedOption("multiply", operation, "Multiply")
            + buildSelectedOption("divide", operation, "Divide")
            + "</select><br><br>"
            + "<button type=\"submit\">Calculate</button>"
            + "</form>"
            + resultMessage
            + "<p><a href=\"/\">Enter another name</a></p>"
            + "</body></html>";
    }

    private static String buildSelectedOption(String value, String selectedValue, String label) {
        String selectedAttribute = value.equals(selectedValue) ? " selected" : "";
        return "<option value=\"" + value + "\"" + selectedAttribute + ">" + label + "</option>";
    }

    private static RequestData parseRequest(String requestLine) throws UnsupportedEncodingException {
        if (requestLine == null || requestLine.isBlank()) {
            return new RequestData("/", new HashMap<String, String>());
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return new RequestData("/", new HashMap<String, String>());
        }

        String requestTarget = parts[1];
        int queryIndex = requestTarget.indexOf('?');
        if (queryIndex < 0) {
            return new RequestData(requestTarget, new HashMap<String, String>());
        }

        String path = requestTarget.substring(0, queryIndex);
        String query = requestTarget.substring(queryIndex + 1);
        Map<String, String> queryParams = new HashMap<String, String>();

        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                queryParams.put(
                    URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name()),
                    URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name())
                );
            }
        }

        return new RequestData(path, queryParams);
    }

    private static String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private static class RequestData {
        private final String path;
        private final Map<String, String> queryParams;

        private RequestData(String path, Map<String, String> queryParams) {
            this.path = path;
            this.queryParams = queryParams;
        }
    }
}