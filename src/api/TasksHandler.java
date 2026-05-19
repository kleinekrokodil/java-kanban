package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager mgr;
    enum Endpoint {GET_TASKS, GET_TASK_BY_ID, CREATE_TASK, UPDATE_TASK, DELETE_TASK, UNKNOWN}

    public TasksHandler(TaskManager mgr) {
        this.mgr = mgr;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASK_BY_ID: {
                handleGetTaskById(exchange);
                break;
            }
            case CREATE_TASK: {
                handleCreateTask(exchange);
                break;
            }
            case UPDATE_TASK: {
                handleUpdateTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(exchange);
                break;
            }
            default:
                sendNotFound(exchange, "Такого эндпоинта не существует");
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        List<Task> allTasks = mgr.getAllTasks();
        String response = gson.toJson(allTasks);
        sendText(exchange, response);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }
        Integer taskId = taskIdOpt.get();
        try {
            Task task = mgr.getTaskById(taskId);
            Gson gson = new Gson();
            String response = gson.toJson(task);
            sendText(exchange, response);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        Task task = gson.fromJson(body, Task.class);
        try {
            mgr.createTask(task);
            sendSimpleMessage(exchange, "Задача успешно создана", 201);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleUpdateTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }
        Integer taskId = taskIdOpt.get();

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        Task task = gson.fromJson(body, Task.class);
        task.setId(taskId);
        try {
            mgr.updateTask(task);
            sendSimpleMessage(exchange, "Задача успешно обновлена", 201);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            sendSimpleMessage(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }
        Integer taskId = taskIdOpt.get();
        try {
            mgr.deleteTaskById(taskId);
            sendSimpleMessage(exchange, "Задача с id=" + taskId + " успешно удалена", 200);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.CREATE_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.UPDATE_TASK;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

}
