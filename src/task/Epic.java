package task;

import java.util.HashSet;

public class Epic extends Task {
    private final HashSet<Integer> childrenTasks;

    public Epic(String name, String description) {
        super(name, description);
        childrenTasks = new HashSet<>();
    }

    // Добавление подзадачи
    public void addChild(Integer subtaskId) {
        childrenTasks.add(subtaskId);
    }

    // Удаление подзадачи
    public void removeChild(Integer subtaskId) {
        childrenTasks.remove(subtaskId);
    }

    // Получение списка подзадач
    public HashSet<Integer> getChildrenTasks() {
        return childrenTasks;
    }
}
