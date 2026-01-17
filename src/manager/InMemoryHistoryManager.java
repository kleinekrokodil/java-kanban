package manager;

import task.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final Integer capacity;
    private final LinkedList<Task> history;

    public InMemoryHistoryManager(Integer capacity) {
        this.capacity = capacity;
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            System.out.println("Передано не инициализированное задание");
            return;
        }
        if (history.size() == capacity) {
            history.removeFirst();
        }
        Task historyTask = new Task(task);
        history.add(historyTask);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(history);
    }
}
