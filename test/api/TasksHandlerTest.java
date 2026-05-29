package api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksHandlerTest extends HandlersTest {
    public TasksHandlerTest() throws IOException {
        super();
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 description");
        Integer taskId = mgr.createTask(task1);
        task1 = mgr.getTaskById(taskId);

        Task task2 = new Task("Task 2", "Task 2 description");
        taskId = mgr.createTask(task2);
        task2 = mgr.getTaskById(taskId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        List<Task> parsedTasks = gson.fromJson(responseJson, new TaskListTypeToken().getType());
        assertEquals(2, parsedTasks.size(), "Список задач не должен быть пустой");
        assertTrue(parsedTasks.contains(task1), "В ответе отсутсвует задача из менеджера");
        assertTrue(parsedTasks.contains(task2), "В ответе отсутсвует задача из менеджера");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 description");
        Integer taskId = mgr.createTask(task);
        task = mgr.getTaskById(taskId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonObject());
        Task parsedTask = gson.fromJson(responseJson, Task.class);
        assertEquals(task, parsedTask, "Задачи должны быть идентичны");

        // Получить задачу с несуществующим id
        url = URI.create("http://localhost:8080/tasks/" + 42);
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_404, response.statusCode());
    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(300);

        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_201, response.statusCode());

        List<Task> tasksFromManager = mgr.getAllTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");

        // Попытка создать задачу, пересекающуюся по времени с первой
        Task taskCopy = new Task(task);
        taskJson = gson.toJson(taskCopy);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_406, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 description");
        Integer taskId = mgr.createTask(task);
        task = mgr.getTaskById(taskId);
        // Обновление задачи
        task.setStartTime(LocalDateTime.now());
        task.setDuration(300);
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_201, response.statusCode());

        Task taskFromManager = mgr.getTaskById(taskId);
        assertEquals(task, taskFromManager, "Задачи должны быть идентичны");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 description");
        Integer taskId = mgr.createTask(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());
        assertEquals(0, mgr.getAllTasks().size(), "Список задач должен быть пуст");
    }
}


