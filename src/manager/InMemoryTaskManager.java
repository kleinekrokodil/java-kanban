package manager;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private final Set<Task> prioritizedTasks;
    private final HistoryManager history;
    private int counter = 0;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
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
        removeFromPrioritizedTasks(tasks.remove(taskId));
        history.remove(taskId);
    }

    // Эпики удаляем со всеми их подзадачами
    @Override
    public void deleteEpicById(Integer epicId) {
        List<Integer> ids = new ArrayList<>(epics.get(epicId).getChildrenTasks());
        for (Integer subtaskId : ids) {
            removeFromPrioritizedTasks(subtasks.remove(subtaskId));
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
        removeFromPrioritizedTasks(subtasks.remove(subtaskId));
        history.remove(subtaskId);
        calcEpicStatus(epic.getId());
        calcEpicDuration(epic.getId());
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
            epic.removeAllChildren();
            calcEpicStatus(epicId);
            calcEpicDuration(epic.getId());
        }
        for (Integer subtaskId : subtasks.keySet()) {
            history.remove(subtaskId);
            removeFromPrioritizedTasks(subtasks.get(subtaskId));
        }
        subtasks.clear();
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(++counter);
        // Защита от последующих изменений в обход update-методов
        Task newTask = new Task(task);
        tasks.put(task.getId(), newTask);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(newTask);
        }
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
        Subtask newSubtask = new Subtask(subtask);
        subtasks.put(subtask.getId(), newSubtask);
        addToPrioritizedTasks(newSubtask);
        updateEpic(epic);
        calcEpicStatus(epic.getId());
        calcEpicDuration(epic.getId());
        return subtask.getId();
    }

    @Override
    public Integer updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return null; // Если задача не найдена - обновлять нечего
        }
        removeFromPrioritizedTasks(tasks.get(task.getId()));
        Task newTask = new Task(task);
        tasks.put(task.getId(), newTask);
        addToPrioritizedTasks(newTask);
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
        removeFromPrioritizedTasks(subtasks.get(subtask.getId()));
        Subtask newSubtask = new Subtask(subtask);
        subtasks.put(subtask.getId(), newSubtask);
        addToPrioritizedTasks(newSubtask);
        calcEpicStatus(subtask.getEpicId());
        calcEpicDuration(subtask.getEpicId());
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

    @Override
    public void calcEpicDuration(Integer epicId) {
        Epic epic = getEpicById(epicId);
        if (epic.getChildrenTasks().isEmpty()) {
            return;
        }
        Duration epicDuration = Duration.ZERO;
        LocalDateTime epicStart = null;
        for (Subtask subtask : getEpicSubtasks(epicId)) {
            if (subtask.getStartTime() != null && (epicStart == null || epicStart.isAfter(subtask.getStartTime()))) {
                epicStart = subtask.getStartTime();
            }
            epicDuration = epicDuration.plus(subtask.getDuration());
        }
        epic.setDuration(epicDuration.toMinutes());
        epic.setStartTime(epicStart);
        updateEpic(epic);
    }

    public List<Task> getHistory() {
        return history.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Добавление задачи в менеджер при восстановлении
    protected void addTask(Task task) {
        if (task.getType() == TaskType.EPIC) {
            epics.put(task.getId(), new Epic((Epic) task));
        } else if (task.getType() == TaskType.SUBTASK) {
            subtasks.put(task.getId(), new Subtask((Subtask) task));
        } else {
            tasks.put(task.getId(), new Task(task));
        }
        addToPrioritizedTasks(task);
        // Установить значение счетчика для корректного создания последующих задач
        counter = Integer.max(counter, task.getId());
    }

    // Восстановление связей между эпиками и подзадачами
    protected void updateSubtasksDependencies() {
        for (Integer subtaskId : subtasks.keySet()) {
            Subtask subtask = subtasks.get(subtaskId);
            Epic epic = epics.get(subtask.getEpicId());
            epic.addChild(subtaskId);
        }
    }

    private boolean isIntersects(Task task1, Task task2) {
        if (task1 == null || task2 == null) return false;
        if (task1.getId() == task2.getId()) return false;

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        // Если у какой-то задачи не задано время - пересечения нет
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        // Проверка наложения отрезков
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    private boolean hasIntersections(Task task) {
        if (task.getStartTime() == null) return false;

        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId()) // Исключаем саму задачу
                .anyMatch(t -> isIntersects(task, t));
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null && !hasIntersections(task)) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        if (task == null || task.getStartTime() == null) return;
        prioritizedTasks.remove(task);
    }
}
