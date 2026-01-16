package manager;

import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager history;
    private int counter = 0;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        history = Managers.getDefaultHistory();
    }

    @Override
    public Task getTaskById(Integer taskId) {
        Task task = tasks.get(taskId);
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        Epic epic = epics.get(epicId);
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        history.add(subtask);
        return subtask;
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
    public Integer createTask(Task task) {
        task.setId(++counter);
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public Integer createEpic(Epic epic) {
        epic.setId(++counter);
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public Integer createSubtask(Subtask subtask, Epic epic) {
        subtask.setId(++counter);
        epic.addChild(subtask.getId());
        subtask.setEpicId(epic.getId());
        subtasks.put(subtask.getId(), subtask);
        calcEpicStatus(epic.getId());
        return subtask.getId();
    }

    @Override
    public Integer updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public Integer updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public Integer updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        calcEpicStatus(subtask.getEpicId());
        return subtask.getId();
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

    public ArrayList<Task> getHistory() {
        return history.getHistory();
    }
}
