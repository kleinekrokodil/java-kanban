package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void add() {
        Task task = new Task("Test history add", "Test history add description");

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
        task.setName("Another name");
        task.setDescription("Another Description");
        historyManager.add(task);
        history = historyManager.getHistory();
        assertEquals(2, history.size(), "После обновления задачи должна добавиться новая запись");
        //assertNotEquals(history.get(0).getName(), history.get(1).getName(), "Данные в задачах не должны обновляться");
    }
}
