package placementprep.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import placementprep.model.Question;
import placementprep.model.QuizResult;
import placementprep.model.Student;
import placementprep.quiz.QuizSession;
import placementprep.service.QuizCatalog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class PlacementPrepServer {
    private static final int MAX_REQUEST_BYTES = 8192;
    private static final Path WEB_ROOT = Path.of("web");
    private final Map<String, List<Question>> quizzes = QuizCatalog.createQuizzes();
    private final Map<String, Student> students = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        PlacementPrepServer application = new PlacementPrepServer();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", application::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Placement Prep is running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if ("/".equals(path) && "GET".equals(method)) {
                serveFile(exchange, "index.html", "text/html; charset=utf-8");
            } else if ("/styles.css".equals(path) && "GET".equals(method)) {
                serveFile(exchange, "styles.css", "text/css; charset=utf-8");
            } else if ("/app.js".equals(path) && "GET".equals(method)) {
                serveFile(exchange, "app.js", "text/javascript; charset=utf-8");
            } else if ("/api/topics".equals(path) && "GET".equals(method)) {
                sendJson(exchange, 200, topicsJson());
            } else if ("/api/quiz".equals(path) && "GET".equals(method)) {
                sendJson(exchange, 200, quizJson(queryParameters(exchange).get("topic")));
            } else if ("/api/submit".equals(path) && "POST".equals(method)) {
                submitQuiz(exchange);
            } else if ("/api/history".equals(path) && "GET".equals(method)) {
                sendJson(exchange, 200, historyJson(queryParameters(exchange).get("name")));
            } else {
                sendJson(exchange, 404, "{\"error\":\"Not found.\"}");
            }
        } catch (IllegalArgumentException exception) {
            sendJson(exchange, 400, "{\"error\":" + jsonString(exception.getMessage()) + "}");
        } catch (IOException exception) {
            System.err.println("Request failed: " + exception.getMessage());
            sendJson(exchange, 500, "{\"error\":\"The server could not complete the request.\"}");
        } finally {
            exchange.close();
        }
    }

    private void serveFile(HttpExchange exchange, String fileName, String contentType) throws IOException {
        Path file = WEB_ROOT.resolve(fileName);
        if (!Files.isRegularFile(file)) {
            sendJson(exchange, 500, "{\"error\":\"Web application files are missing.\"}");
            return;
        }
        byte[] content = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'self'; style-src 'self'; script-src 'self'; connect-src 'self'; base-uri 'none'; frame-ancestors 'none'");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(200, content.length);
        exchange.getResponseBody().write(content);
    }

    private String topicsJson() {
        StringBuilder json = new StringBuilder("{\"topics\":[");
        int index = 0;
        for (Map.Entry<String, List<Question>> entry : quizzes.entrySet()) {
            if (index++ > 0) {
                json.append(',');
            }
            json.append("{\"name\":").append(jsonString(entry.getKey()))
                    .append(",\"questionCount\":").append(entry.getValue().size()).append('}');
        }
        return json.append("]}").toString();
    }

    private String quizJson(String topic) {
        List<Question> questions = requireQuiz(topic);
        StringBuilder json = new StringBuilder("{\"topic\":")
                .append(jsonString(topic)).append(",\"questions\":[");
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            Question question = questions.get(i);
            json.append("{\"prompt\":").append(jsonString(question.getPrompt()))
                    .append(",\"options\":[");
            for (int option = 0; option < question.getOptions().size(); option++) {
                if (option > 0) {
                    json.append(',');
                }
                json.append(jsonString(question.getOptions().get(option)));
            }
            json.append("]}");
        }
        return json.append("]}").toString();
    }

    private void submitQuiz(HttpExchange exchange) throws IOException {
        Map<String, String> form = formParameters(exchange);
        String name = requireName(form.get("name"));
        String topic = form.get("topic");
        List<Question> questions = requireQuiz(topic);
        List<Integer> answers = parseAnswers(form.get("answers"));
        QuizSession session = new QuizSession(topic, questions);
        QuizResult result = session.evaluate(answers);

        Student student = students.computeIfAbsent(name, Student::new);
        synchronized (student) {
            student.recordResult(result);
        }

        StringBuilder json = new StringBuilder("{\"result\":")
                .append(resultJson(result)).append(",\"review\":[");
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            Question question = questions.get(i);
            int selected = answers.get(i);
            json.append("{\"prompt\":").append(jsonString(question.getPrompt()))
                    .append(",\"selected\":").append(jsonString(question.getOptions().get(selected - 1)))
                    .append(",\"correct\":").append(jsonString(question.getCorrectAnswer()))
                    .append(",\"isCorrect\":").append(question.isCorrect(selected))
                    .append(",\"explanation\":").append(jsonString(question.getExplanation())).append('}');
        }
        sendJson(exchange, 200, json.append("]}").toString());
    }

    private String historyJson(String rawName) {
        String name = requireName(rawName);
        Student student = students.get(name);
        if (student == null) {
            return "{\"results\":[]}";
        }
        StringBuilder json = new StringBuilder("{\"results\":[");
        synchronized (student) {
            List<QuizResult> results = student.getResults();
            for (int i = 0; i < results.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append(resultJson(results.get(i)));
            }
        }
        return json.append("]}").toString();
    }

    private String resultJson(QuizResult result) {
        return "{\"topic\":" + jsonString(result.getTopic())
                + ",\"score\":" + result.getScore()
                + ",\"total\":" + result.getTotalQuestions()
                + ",\"percentage\":" + String.format(java.util.Locale.ROOT, "%.1f", result.getPercentage())
                + ",\"completedAt\":" + jsonString(result.getCompletedAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) + "}";
    }

    private List<Question> requireQuiz(String topic) {
        List<Question> questions = quizzes.get(topic);
        if (questions == null) {
            throw new IllegalArgumentException("Choose a valid quiz topic.");
        }
        return questions;
    }

    private String requireName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 60) {
            throw new IllegalArgumentException("Enter a name between 1 and 60 characters.");
        }
        return name.trim();
    }

    private List<Integer> parseAnswers(String rawAnswers) {
        if (rawAnswers == null || rawAnswers.isBlank()) {
            throw new IllegalArgumentException("Please answer every question.");
        }
        String[] values = rawAnswers.split(",", -1);
        List<Integer> answers = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                answers.add(Integer.parseInt(value));
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("One or more answers are not valid.");
        }
        return answers;
    }

    private Map<String, String> queryParameters(HttpExchange exchange) {
        return decodeParameters(exchange.getRequestURI().getRawQuery());
    }

    private Map<String, String> formParameters(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
        if (body.length > MAX_REQUEST_BYTES) {
            throw new IllegalArgumentException("Request is too large.");
        }
        return decodeParameters(new String(body, StandardCharsets.UTF_8));
    }

    private Map<String, String> decodeParameters(String encoded) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (encoded == null || encoded.isBlank()) {
            return parameters;
        }
        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            parameters.put(key, value);
        }
        return parameters;
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }

    private String jsonString(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }
}
