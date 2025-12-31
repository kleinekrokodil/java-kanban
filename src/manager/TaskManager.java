package manager;

import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private int counter = 0;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public Task getTaskById(Integer taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpicById(Integer epicId) {
        return epics.get(epicId);
    }

    public Subtask getSubtaskById(Integer subtaskId) {
        return subtasks.get(subtaskId);
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Integer taskId : tasks.keySet()) {
            allTasks.add(getTaskById(taskId));
        }
        return allTasks;
    }

    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> allEpics = new ArrayList<>();
        for (Integer epicId : epics.keySet()) {
            allEpics.add(getEpicById(epicId));
        }
        return allEpics;
    }

    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        for (Integer subtaskId : subtasks.keySet()) {
            allSubtasks.add(getSubtaskById(subtaskId));
        }
        return allSubtasks;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
    }

    // Эпики удаляем со всеми их подзадачами
    public void deleteEpicById(Integer epicId) {
        for (Integer subtaskId : epics.get(epicId).getChildrenTasks()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(epicId);
    }

    public void deleteSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeChild(subtaskId);
        subtasks.remove(subtaskId);
        calcEpicStatus(epic.getId());
    }

    public void deleteAllEpics() {
        for (Integer epicId : epics.keySet()) {
            deleteEpicById(epicId);
        }
    }

    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            deleteSubtaskById(subtaskId);
        }
    }

    public void createTask(Task task) {
        task.setId(++counter);
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        epic.setId(++counter);
        epics.put(epic.getId(), epic);
    }

    public void createSubtask(Subtask subtask, Epic epic) {
        subtask.setId(++counter);
        epic.addChild(subtask.getId());
        subtask.setEpicId(epic.getId());
        subtasks.put(subtask.getId(), subtask);
        calcEpicStatus(epic.getId());
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        calcEpicStatus(subtask.getEpicId());
    }

    public ArrayList<Subtask> getEpicSubtasks(Integer epicId) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        Epic epic = getEpicById(epicId);
        for (Integer subtaskId : epic.getChildrenTasks()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    public void calcEpicStatus(Integer epicId) {
        Epic epic = getEpicById(epicId);
        TaskStatus status = TaskStatus.NEW;
        if (!epic.getChildrenTasks().isEmpty()) {
            HashSet<TaskStatus> statuses = new HashSet<>();
            for (Subtask subtask : getEpicSubtasks(epicId)) {
                status = subtask.getStatus();
                statuses.add(status);
            }
            if (statuses.size() == 1) {
                // Если размер сета равен 1, то все задачи эпика находятся в одном статусе
                epic.setStatus(status);
            } else {
                // Иначе - статус "В работе"
                epic.setStatus(TaskStatus.IN_PROGRESS);
            }
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }
}
