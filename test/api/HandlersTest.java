package api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.util.List;

public abstract class HandlersTest {
    TaskManager mgr = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(mgr);
    Gson gson = taskServer.getGson();

    public HandlersTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        mgr.deleteAllTasks();
        mgr.deleteAllSubtasks();
        mgr.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }
}

class TaskListTypeToken extends TypeToken<List<Task>> {

}

class SubtaskListTypeToken extends TypeToken<List<Subtask>> {

}

class EpicListTypeToken extends TypeToken<List<Epic>> {

}
