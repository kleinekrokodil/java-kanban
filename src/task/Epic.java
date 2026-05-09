package task;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Epic extends Task {
    private final Set<Integer> childrenTasks;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        childrenTasks = new HashSet<>();
    }

    public Epic(Epic epic) {
        super(epic);
        this.childrenTasks =  new HashSet<>(epic.childrenTasks);
        this.endTime = epic.endTime;
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
    public void removeAllChildren() {
        childrenTasks.clear();
    }

    // Получение списка подзадач
    public Set<Integer> getChildrenTasks() {
        return new HashSet<>(childrenTasks);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
