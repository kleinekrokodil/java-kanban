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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrioritizedHandlerTest extends HandlersTest {
    public PrioritizedHandlerTest() throws IOException {
        super();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        List<Task> parsedTasks = gson.fromJson(responseJson, new TaskListTypeToken().getType());
        assertEquals(0, parsedTasks.size(), "Список приоритетов должен быть пустой");

        Task task = new Task("Task 1", "Task 1 description");
        task.setDuration(90);
        task.setStartTime(LocalDateTime.now());
        Integer taskId = mgr.createTask(task);
        task = mgr.getTaskById(taskId);

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        responseJson = JsonParser.parseString(response.body());
        assertTrue(responseJson.isJsonArray());
        parsedTasks = gson.fromJson(responseJson, new TaskListTypeToken().getType());
        assertEquals(1, parsedTasks.size(),
                "Список приоритетов не должен быть пустой после добавления задачи");
        assertEquals(task, parsedTasks.getFirst(), "Задачи должны быть идентичны");
    }
}


