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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryHandlerTest extends HandlersTest {
    public HistoryHandlerTest() throws IOException {
        super();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        List<Task> parsedHistory = gson.fromJson(responseJson, new TaskListTypeToken().getType());
        assertEquals(0, parsedHistory.size(), "История должна быть пустой");

        Task task = new Task("Task 1", "Task 1 description");
        Integer taskId = mgr.createTask(task);
        task = mgr.getTaskById(taskId);

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(BaseHttpHandler.STATUS_CODE_200, response.statusCode());

        responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        parsedHistory = gson.fromJson(responseJson, new TaskListTypeToken().getType());
        assertEquals(1, parsedHistory.size(), "История не должна быть пустой после добавления задачи");
        assertEquals(task, parsedHistory.getFirst(), "Задачи должны быть идентичны");
    }
}
