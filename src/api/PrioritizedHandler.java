package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager mgr;
    enum Endpoint {GET_PRIORITIZED, UNKNOWN}

    public PrioritizedHandler(TaskManager mgr) {
        this.mgr = mgr;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        if (endpoint == Endpoint.GET_PRIORITIZED) {
            handleGetPrioritized(exchange);
        } else {
            sendNotFound(exchange, "Такого эндпоинта не существует");
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        List<Task> prioritized = mgr.getPrioritizedTasks();
        String response = gson.toJson(prioritized);
        sendText(exchange, response);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (pathParts.length == 2 && pathParts[1].equals("prioritized") && requestMethod.equals("GET")) {
            return Endpoint.GET_PRIORITIZED;
        }
        return Endpoint.UNKNOWN;
    }
}
