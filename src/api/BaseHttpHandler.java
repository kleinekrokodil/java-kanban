package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler {
    static final int STATUS_CODE_200 = 200;
    static final int STATUS_CODE_201 = 201;
    static final int STATUS_CODE_400 = 400;
    static final int STATUS_CODE_404 = 404;
    static final int STATUS_CODE_406 = 404;

    protected void sendText(HttpExchange exchange, String responseString) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(STATUS_CODE_200, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String responseString) throws IOException {
        exchange.sendResponseHeaders(STATUS_CODE_404, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected void sendHasInteractions(HttpExchange exchange, String responseString) throws IOException {
        exchange.sendResponseHeaders(STATUS_CODE_406, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected void sendSimpleMessage(HttpExchange exchange, String responseString, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
