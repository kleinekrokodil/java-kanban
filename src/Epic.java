import java.util.HashSet;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, Subtask> childrenTasks;
    private final HashSet<Integer> inProgressTasks; // если пусты inProgressTasks и resolvedTasks - значит статус NEW
    private final HashSet<Integer> resolvedTasks; // Когда размер сравняется с childrenTasks - поставить статус DONE

    public Epic(String name, String description) {
        super(name, description);
        childrenTasks = new HashMap<>();
        inProgressTasks = new HashSet<>();
        resolvedTasks = new HashSet<>();
    }

    // Добавление подзадачи
    public void addChild(Subtask subtask) {
        childrenTasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask);
    }

    // Удаление подзадачи
    public void removeChild(Subtask subtask) {
        childrenTasks.remove(subtask.getId());
        inProgressTasks.remove(subtask.getId());
        resolvedTasks.remove(subtask.getId());
        calculateStatus();
    }

    // Получение списка подзадач
    public HashMap<Integer, Subtask> getChildrenTasks() {
        return childrenTasks;
    }

    public void updateEpicStatus(Subtask subtask) {
        TaskStatus subStatus = subtask.getStatus();
        Integer subtaskId = subtask.getId();
        if (subStatus == TaskStatus.NEW) {
            inProgressTasks.remove(subtaskId);
            resolvedTasks.remove(subtaskId);
        } else if (subStatus == TaskStatus.IN_PROGRESS) {
            inProgressTasks.add(subtaskId);
            resolvedTasks.remove(subtaskId);
        } else if (subStatus == TaskStatus.DONE) {
            inProgressTasks.remove(subtaskId);
            resolvedTasks.add(subtaskId);
        }
        calculateStatus();
    }

    private void calculateStatus() {
        if (inProgressTasks.isEmpty() && resolvedTasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
        } else if (resolvedTasks.size() == childrenTasks.size()) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
