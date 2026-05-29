package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler  extends BaseHttpHandler implements HttpHandler {
    private final TaskManager mgr;
    private final Gson gson;

    enum Endpoint {
        GET_HISTORY,
        UNKNOWN
    }

    public HistoryHandler(TaskManager mgr, Gson gson) {
        this.mgr = mgr;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        if (endpoint == Endpoint.GET_HISTORY) {
            handleGetHistory(exchange);
        } else {
            sendNotFound(exchange, "Такого эндпоинта не существует");
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = mgr.getHistory();
        String response = gson.toJson(history);
        sendText(exchange, response);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (pathParts.length == 2 && pathParts[1].equals("history") && requestMethod.equals("GET")) {
            return Endpoint.GET_HISTORY;
        }
        return Endpoint.UNKNOWN;
    }
}
