package task;

import java.util.HashSet;
import java.util.Set;

public class Epic extends Task {
    private final Set<Integer> childrenTasks;

    public Epic(String name, String description) {
        super(name, description);
        childrenTasks = new HashSet<>();
    }

    public Epic(Epic epic) {
        super(epic);
        this.childrenTasks = epic.childrenTasks;
    }

    // Добавление подзадачи
    public void addChild(Integer subtaskId) {
        childrenTasks.add(subtaskId);
    }

    // Удаление подзадачи
    public void removeChild(Integer subtaskId) {
        childrenTasks.remove(subtaskId);
    }

    // Удаление всех подзадач
    public void removeAllChilds() {
        childrenTasks.clear();
    }

    // Получение списка подзадач
    public Set<Integer> getChildrenTasks() {
        return childrenTasks;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}
