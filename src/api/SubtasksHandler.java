package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager mgr;
    private final Gson gson;

    enum Endpoint {
        GET_SUBTASKS,
        GET_SUBTASK_BY_ID,
        CREATE_SUBTASK,
        UPDATE_SUBTASK,
        DELETE_SUBTASK,
        UNKNOWN
    }

    public SubtasksHandler(TaskManager mgr, Gson gson) {
        this.mgr = mgr;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASKS: {
                handleGetSubtasks(exchange);
                break;
            }
            case GET_SUBTASK_BY_ID: {
                handleGetSubtaskById(exchange);
                break;
            }
            case CREATE_SUBTASK: {
                handleCreateSubtask(exchange);
                break;
            }
            case UPDATE_SUBTASK: {
                handleUpdateSubtask(exchange);
                break;
            }
            case DELETE_SUBTASK: {
                handleDeleteSubtask(exchange);
                break;
            }
            default:
                sendNotFound(exchange, "Такого эндпоинта не существует");
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> allSubtasks = mgr.getAllSubtasks();
        String response = gson.toJson(allSubtasks);
        sendText(exchange, response);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskIdOpt = getTaskId(exchange);
        if (subtaskIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор подзадачи", 400);
            return;
        }
        Integer subtaskId = subtaskIdOpt.get();
        try {
            Subtask subtask = mgr.getSubtaskById(subtaskId);
            String response = gson.toJson(subtask);
            sendText(exchange, response);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateSubtask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        try {
            Epic epic = mgr.getEpicById(subtask.getEpicId());
            mgr.createSubtask(subtask, epic);
            sendSimpleMessage(exchange, "Подадача успешно создана", 201);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleUpdateSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskIdOpt = getTaskId(exchange);
        if (subtaskIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор подзадачи", 400);
            return;
        }
        Integer subtaskId = subtaskIdOpt.get();

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        subtask.setId(subtaskId);
        try {
            mgr.updateSubtask(subtask);
            sendSimpleMessage(exchange, "Подзадача успешно обновлена", 201);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskIdOpt = getTaskId(exchange);
        if (subtaskIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор подзадачи", 400);
            return;
        }
        Integer subtaskId = subtaskIdOpt.get();
        try {
            mgr.deleteSubtaskById(subtaskId);
            sendSimpleMessage(exchange, "Подзадача с id=" + subtaskId + " успешно удалена", 200);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.CREATE_SUBTASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.UPDATE_SUBTASK;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }
}
