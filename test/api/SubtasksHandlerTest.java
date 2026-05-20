package api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubtasksHandlerTest extends HandlersTest {
    public SubtasksHandlerTest() throws IOException {
        super();
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);
        epic = mgr.getEpicById(epicId);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 description");
        Integer subtaskId = mgr.createSubtask(subtask1, epic);
        subtask1 = mgr.getSubtaskById(subtaskId);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 description");
        subtaskId = mgr.createSubtask(subtask2, epic);
        subtask2 = mgr.getSubtaskById(subtaskId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        List<Subtask> parsedSubtasks = gson.fromJson(responseJson, new SubtaskListTypeToken().getType());
        assertEquals(2, parsedSubtasks.size(), "Список задач не должен быть пустой");
        assertTrue(parsedSubtasks.contains(subtask1), "В ответе отсутсвует задача из менеджера");
        assertTrue(parsedSubtasks.contains(subtask2), "В ответе отсутсвует задача из менеджера");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);
        epic = mgr.getEpicById(epicId);

        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 description");
        Integer subtaskId = mgr.createSubtask(subtask, epic);
        subtask = mgr.getSubtaskById(subtaskId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonObject());
        Subtask parsedSubtask = gson.fromJson(responseJson, Subtask.class);
        assertEquals(subtask, parsedSubtask, "Подзадачи должны быть идентичны");

        // Получить подзадачу с несуществующим id
        url = URI.create("http://localhost:8080/subtasks/" + 42);
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 description");
        subtask.setEpicId(epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(300);

        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> tasksFromManager = mgr.getAllSubtasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        assertEquals(epicId, tasksFromManager.getFirst().getEpicId(), "Некорректный эпик подзадачи");

        // Попытка создать задачу, пересекающуюся по времени с первой
        Subtask subtaskCopy = new Subtask(subtask);
        subtaskJson = gson.toJson(subtaskCopy);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);
        epic = mgr.getEpicById(epicId);

        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 description");
        Integer subtaskId = mgr.createSubtask(subtask, epic);
        subtask = mgr.getSubtaskById(subtaskId);
        // Обновление задачи
        subtask.setEpicId(epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(300);

        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask subtaskFromManager = mgr.getSubtaskById(subtaskId);
        assertEquals(subtask, subtaskFromManager, "Подзадачи должны быть идентичны");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);
        epic = mgr.getEpicById(epicId);

        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 description");
        Integer subtaskId = mgr.createSubtask(subtask, epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, mgr.getAllSubtasks().size(), "Список задач должен быть пуст");
    }
}


