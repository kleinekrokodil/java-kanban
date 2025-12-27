import java.util.HashMap;
import java.util.HashSet;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashSet<Integer> epicIds;
    private final HashSet<Integer> subtaskIds;

    public TaskManager() {
        tasks = new HashMap<>();
        epicIds = new HashSet<>();
        subtaskIds = new HashSet<>();
    }

    public Task getTaskById(Integer taskId) {
        if (!tasks.containsKey(taskId)) {
            return null;
        }
        return tasks.get(taskId);
    }

    public Epic getEpicById(Integer epicId) {
        if (!epicIds.contains(epicId)) {
            return null;
        }
        return (Epic)tasks.get(epicId);
    }

    public Subtask getSubtaskById(Integer subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            return null;
        }
        return (Subtask)tasks.get(subtaskId);
    }

    public HashSet<Task> getAllTasks() {
        HashSet<Task> allTasks = new HashSet<>();
        for (Integer taskId : tasks.keySet()) {
            allTasks.add(getTaskById(taskId));
        }
        return allTasks;
    }

    public HashSet<Epic> getEpics() {
        HashSet<Epic> epics = new HashSet<>();
        for (Integer epicId : epicIds) {
            epics.add(getEpicById(epicId));
        }
        return epics;
    }

    public HashSet<Subtask> getSubtasks() {
        HashSet<Subtask> subtasks = new HashSet<>();
        for (Integer subtaskId : subtaskIds) {
            subtasks.add(getSubtaskById(subtaskId));
        }
        return subtasks;
    }

    public void deleteAllTasks() {
        tasks.clear();
        epicIds.clear();
        subtaskIds.clear();
    }

    public void deleteTaskById(Integer taskId) {
        if (epicIds.contains(taskId)) {
            deleteEpicById(taskId);
        } else if (subtaskIds.contains(taskId)) {
            deleteSubtaskById(taskId);
        } else {
            tasks.remove(taskId);
        }
    }

    // Эпики удаляем со всеми их подзадачами
    public void deleteEpicById(Integer epicId) {
        if (!epicIds.contains(epicId)) {
            return;
        }
        for (Integer subtaskId : ((Epic)tasks.get(epicId)).getChildrenTasks().keySet()) {
            tasks.remove(subtaskId);
            subtaskIds.remove(subtaskId);
        }
        tasks.remove(epicId);
        epicIds.remove(epicId);
    }

    public void deleteSubtaskById(Integer subtaskId) {
        if(!subtaskIds.contains(subtaskId)) {
            return;
        }
        Epic epic = ((Subtask)tasks.get(subtaskId)).getParent();
        epic.removeChild((Subtask)tasks.get(subtaskId));
        tasks.remove(subtaskId);
        subtaskIds.remove(subtaskId);
    }

    public void deleteAllEpics() {
        for (Integer epicId : epicIds) {
            deleteEpicById(epicId);
        }
    }

    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtaskIds) {
            deleteSubtaskById(subtaskId);
        }
    }

    public void createTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        tasks.put(epic.getId(), epic);
        epicIds.add(epic.getId());
        for (Subtask child : epic.getChildrenTasks().values()) {
            createSubtask(child);
        }
    }

    public void createSubtask(Subtask subtask) {
        tasks.put(subtask.getId(), subtask);
        subtaskIds.add(subtask.getId());
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public HashSet<Subtask> getEpicSubtasks(Integer epicId) {
        return new HashSet<>(((Epic)tasks.get(epicId)).getChildrenTasks().values());
    }
}
