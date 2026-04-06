package manager;

import task.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
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
        // Защита от изменений в обход update-методов
        Task task = new Task(tasks.get(taskId));
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        Epic epic = new Epic(epics.get(epicId));
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        Subtask subtask = new Subtask(subtasks.get(subtaskId));
        history.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (Integer taskId : tasks.keySet()) {
            allTasks.add(getTaskById(taskId));
        }
        return allTasks;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> allEpics = new ArrayList<>();
        for (Integer epicId : epics.keySet()) {
            allEpics.add(getEpicById(epicId));
        }
        return allEpics;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> allSubtasks = new ArrayList<>();
        for (Integer subtaskId : subtasks.keySet()) {
            allSubtasks.add(getSubtaskById(subtaskId));
        }
        return allSubtasks;
    }

    @Override
    public void deleteAllTasks() {
        List<Integer> taskIds = new ArrayList<>(tasks.keySet());
        for (Integer taskId : taskIds) {
            deleteTaskById(taskId);
        }
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
        history.remove(taskId);
    }

    // Эпики удаляем со всеми их подзадачами
    @Override
    public void deleteEpicById(Integer epicId) {
        List<Integer> ids = new ArrayList<>(epics.get(epicId).getChildrenTasks());
        for (Integer subtaskId : ids) {
            subtasks.remove(subtaskId);
            history.remove(subtaskId);
        }
        epics.remove(epicId);
        history.remove(epicId);
    }

    @Override
    public void deleteSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeChild(subtaskId);
        subtasks.remove(subtaskId);
        history.remove(subtaskId);
        calcEpicStatus(epic.getId());
    }

    @Override
    public void deleteAllEpics() {
        List<Integer> epicIds = new ArrayList<>(epics.keySet());
        for (Integer epicId : epicIds) {
            deleteEpicById(epicId);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer epicId : epics.keySet()) {
            Epic epic = epics.get(epicId);
            epic.removeAllChilds();
            calcEpicStatus(epicId);
        }
        for (Integer subtaskId : subtasks.keySet()) {
            history.remove(subtaskId);
        }
        subtasks.clear();
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(++counter);
        // Защита от последующих изменений в обход update-методов
        tasks.put(task.getId(), new Task(task));
        return task.getId();
    }

    @Override
    public Integer createEpic(Epic epic) {
        epic.setId(++counter);
        epics.put(epic.getId(), new Epic(epic));
        return epic.getId();
    }

    @Override
    public Integer createSubtask(Subtask subtask, Epic epic) {
        subtask.setId(++counter);
        epic.addChild(subtask.getId());
        subtask.setEpicId(epic.getId());
        subtasks.put(subtask.getId(), new Subtask(subtask));
        updateEpic(epic);
        calcEpicStatus(epic.getId());
        return subtask.getId();
    }

    @Override
    public Integer updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return null; // Если задача не найдена - обновлять нечего
        }
        tasks.put(task.getId(), new Task(task));
        return task.getId();
    }

    @Override
    public Integer updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return null; // Если задача не найдена - обновлять нечего
        }
        epics.put(epic.getId(), new Epic(epic));
        return epic.getId();
    }

    @Override
    public Integer updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return null; // Если задача не найдена - обновлять нечего
        }
        subtasks.put(subtask.getId(), new Subtask(subtask));
        calcEpicStatus(subtask.getEpicId());
        return subtask.getId();
    }

    @Override
    public List<Subtask> getEpicSubtasks(Integer epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
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
        updateEpic(epic); // Поменять статус эпика в менеджере
    }

    public List<Task> getHistory() {
        return history.getHistory();
    }
}
