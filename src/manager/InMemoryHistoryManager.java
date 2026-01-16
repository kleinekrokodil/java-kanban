package manager;

import task.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final Integer capacity;
    private final ArrayList<Task> history;

    public InMemoryHistoryManager(Integer capacity) {
        this.capacity = capacity;
        history = new ArrayList<>(capacity);
    }

    @Override
    public void add(Task task) {
        if (history.size() == capacity) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
