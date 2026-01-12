package manager;

import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private int counter = 0;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    @Override
    public Task getTaskById(Integer taskId) {
        return tasks.get(taskId);
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        return epics.get(epicId);
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        return subtasks.get(subtaskId);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Integer taskId : tasks.keySet()) {
            allTasks.add(getTaskById(taskId));
        }
        return allTasks;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> allEpics = new ArrayList<>();
        for (Integer epicId : epics.keySet()) {
            allEpics.add(getEpicById(epicId));
        }
        return allEpics;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        for (Integer subtaskId : subtasks.keySet()) {
            allSubtasks.add(getSubtaskById(subtaskId));
        }
        return allSubtasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
    }

    // Эпики удаляем со всеми их подзадачами
    @Override
    public void deleteEpicById(Integer epicId) {
        for (Integer subtaskId : epics.get(epicId).getChildrenTasks()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(epicId);
    }

    @Override
    public void deleteSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeChild(subtaskId);
        subtasks.remove(subtaskId);
        calcEpicStatus(epic.getId());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId : epics.keySet()) {
            deleteEpicById(epicId);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            deleteSubtaskById(subtaskId);
        }
    }

    @Override
    public void createTask(Task task) {
        task.setId(++counter);
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(++counter);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask, Epic epic) {
        subtask.setId(++counter);
        epic.addChild(subtask.getId());
        subtask.setEpicId(epic.getId());
        subtasks.put(subtask.getId(), subtask);
        calcEpicStatus(epic.getId());
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        calcEpicStatus(subtask.getEpicId());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Integer epicId) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        Epic epic = getEpicById(epicId);
        for (Integer subtaskId : epic.getChildrenTasks()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    @Override
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
