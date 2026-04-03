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
        assertEquals(1, history.size(), "После добавления задачи история не должна быть пустой.");
        task.setName("Another name");
        task.setDescription("Another Description");
        historyManager.add(task);
        history = historyManager.getHistory();
        assertEquals(1, history.size(),
                "После обновления задачи количество записей не должно измениться");
    }

    @Test
    void remove() {
        Task task1 = new Task("Test history remove", "Test history remove");
        task1.setId(1);
        historyManager.add(task1);
        Task task2 = new Task("Test history remove - 2", "Test history remove - 2");
        task2.setId(2);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "После добавления задачи история не должна быть пустой.");
        historyManager.remove(1);
        history = historyManager.getHistory();
        assertEquals(1, history.size(),
                "После удаления одной из двух задач в истории должна остаться запись о второй");
        assertEquals(2, history.getFirst().getId(),
                "ID оставшейся задачи не соответствует ожидаемому (2)");
        historyManager.remove(2);
        history = historyManager.getHistory();
        assertEquals(0, history.size(),
                "После удаления единственной задачи история должна быть пустой.");
    }
}
