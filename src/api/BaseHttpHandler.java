package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {
    protected void sendText(HttpExchange exchange, String responseString) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String responseString) throws IOException {
        exchange.sendResponseHeaders(404, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected void sendHasInteractions(HttpExchange exchange, String responseString) throws IOException {
        exchange.sendResponseHeaders(406, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    protected void sendSimpleMessage(HttpExchange exchange, String responseString, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, 0);
        exchange.getResponseBody().write(responseString.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }
}
