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

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager mgr;
    private final Gson gson;

    enum Endpoint {
        GET_EPICS,
        GET_EPIC_BY_ID,
        GET_EPIC_SUBTASKS,
        CREATE_EPIC,
        UPDATE_EPIC,
        DELETE_SUBTASK,
        UNKNOWN
    }

    public EpicsHandler(TaskManager mgr, Gson gson) {
        this.mgr = mgr;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPICS: {
                handleGetEpics(exchange);
                break;
            }
            case GET_EPIC_BY_ID: {
                handleGetEpicById(exchange);
                break;
            }
            case GET_EPIC_SUBTASKS: {
                handleGetEpicSubtasks(exchange);
                break;
            }
            case CREATE_EPIC: {
                handleCreateEpic(exchange);
                break;
            }
            case UPDATE_EPIC: {
                handleUpdateEpic(exchange);
                break;
            }
            case DELETE_SUBTASK: {
                handleDeleteEpic(exchange);
                break;
            }
            default:
                sendNotFound(exchange, "Такого эндпоинта не существует");
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        List<Epic> allEpics = mgr.getAllEpics();
        String response = gson.toJson(allEpics);
        sendText(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getTaskId(exchange);
        if (epicIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор эпика", STATUS_CODE_400);
            return;
        }
        Integer epicId = epicIdOpt.get();
        try {
            Epic epic = mgr.getEpicById(epicId);
            String response = gson.toJson(epic);
            sendText(exchange, response);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getTaskId(exchange);
        if (epicIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор эпика", STATUS_CODE_400);
            return;
        }
        Integer epicId = epicIdOpt.get();
        try {
            List<Subtask> epicSubtasks = mgr.getEpicSubtasks(epicId);
            String response = gson.toJson(epicSubtasks);
            sendText(exchange, response);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);
        try {
            mgr.createEpic(epic);
            sendSimpleMessage(exchange, "Эпик успешно создан", STATUS_CODE_201);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleUpdateEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getTaskId(exchange);
        if (epicIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор эпика", STATUS_CODE_400);
            return;
        }
        Integer epicId = epicIdOpt.get();

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);
        epic.setId(epicId);
        try {
            mgr.updateEpic(epic);
            sendSimpleMessage(exchange, "Эпик успешно обновлен", STATUS_CODE_201);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getTaskId(exchange);
        if (epicIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор эпика", STATUS_CODE_400);
            return;
        }
        Integer epicId = epicIdOpt.get();
        try {
            mgr.deleteEpicById(epicId);
            sendSimpleMessage(exchange, "Эпик с id=" + epicId + " успешно удален", STATUS_CODE_200);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.CREATE_EPIC;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.UPDATE_EPIC;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            return Endpoint.GET_EPIC_SUBTASKS;
        }
        return Endpoint.UNKNOWN;
    }
}
