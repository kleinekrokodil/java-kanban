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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicsHandlerTest extends HandlersTest {
    public EpicsHandlerTest() throws IOException {
        super();
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic1);
        epic1 = mgr.getEpicById(epicId);

        Epic epic2 = new Epic("Epic 2", "Very epic test task 2");
        epicId = mgr.createEpic(epic2);
        epic2 = mgr.getEpicById(epicId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        List<Epic> parsedEpics = gson.fromJson(responseJson, new EpicListTypeToken().getType());
        assertEquals(2, parsedEpics.size(), "Список эпиков не должен быть пустой");
        assertTrue(parsedEpics.contains(epic1), "В ответе отсутсвует эпик из менеджера");
        assertTrue(parsedEpics.contains(epic2), "В ответе отсутсвует эпик из менеджера");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);
        epic = mgr.getEpicById(epicId);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonObject());
        Epic parsedEpic = gson.fromJson(responseJson, Epic.class);
        assertEquals(epic, parsedEpic, "Эпики должны быть идентичны");

        // Получить эпик с несуществующим id
        url = URI.create("http://localhost:8080/epics/" + 42);
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_404, response.statusCode());
    }

    @Test
    public void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_201, response.statusCode());

        List<Epic> epicsFromManager = mgr.getAllEpics();
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);
        epic = mgr.getEpicById(epicId);
        // Обновление задачи
        epic.setDescription("A great test project");

        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_201, response.statusCode());

        Epic epicFromManager = mgr.getEpicById(epicId);
        assertEquals(epic, epicFromManager, "Эпики должны быть идентичны");
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
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
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        List<Subtask> parsedSubtasks = gson.fromJson(responseJson, new SubtaskListTypeToken().getType());
        assertEquals(2, parsedSubtasks.size(), "Список задач не должен быть пустой");
        assertTrue(parsedSubtasks.contains(subtask1), "В ответе отсутсвует подзадача из менеджера");
        assertTrue(parsedSubtasks.contains(subtask2), "В ответе отсутсвует подзадача из менеджера");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Very epic test task");
        Integer epicId = mgr.createEpic(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());
        assertEquals(0, mgr.getAllSubtasks().size(), "Список эпиков должен быть пуст");
    }
}


