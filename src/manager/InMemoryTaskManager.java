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
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime).thenComparing(Task::getId));
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
        return tasks.values().stream()
                .peek(history::add)
                .toList();
    }

    @Override
    public List<Epic> getAllEpics() {
        return epics.values().stream()
                .peek(history::add)
                .toList();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return subtasks.values().stream()
                .peek(history::add)
                .toList();
    }

    @Override
    public void deleteAllTasks() {
        List.copyOf(tasks.keySet()).forEach(this::deleteTaskById);
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        removeFromPrioritizedTasks(tasks.remove(taskId));
        history.remove(taskId);
    }

    // Эпики удаляем со всеми их подзадачами
    @Override
    public void deleteEpicById(Integer epicId) {
        epics.get(epicId).getChildrenTasks().forEach(subtaskId -> {
            removeFromPrioritizedTasks(subtasks.remove(subtaskId));
            history.remove(subtaskId);
        });
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
        List.copyOf(epics.keySet()).forEach(this::deleteEpicById);
    }

    @Override
    public void deleteAllSubtasks() {
        epics.keySet().forEach(epicId -> {
            Epic epic = epics.get(epicId);
            epic.removeAllChildren();
            calcEpicStatus(epicId);
            calcEpicDuration(epic.getId());
        });
        subtasks.keySet().forEach(subtaskId -> {
            history.remove(subtaskId);
            removeFromPrioritizedTasks(subtasks.get(subtaskId));
        });
        subtasks.clear();
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(++counter);
        // Защита от последующих изменений в обход update-методов
        Task newTask = new Task(task);
        try {
            addToPrioritizedTasks(newTask);
            tasks.put(task.getId(), newTask);
            return task.getId();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
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
        try {
            Subtask newSubtask = new Subtask(subtask);
            addToPrioritizedTasks(newSubtask);
            subtasks.put(subtask.getId(), newSubtask);
            updateEpic(epic);
            calcEpicStatus(epic.getId());
            calcEpicDuration(epic.getId());
            return subtask.getId();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Integer updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return null; // Если задача не найдена - обновлять нечего
        }
        Task oldTask = tasks.get(task.getId());
        try {
            removeFromPrioritizedTasks(oldTask);
            Task newTask = new Task(task);
            addToPrioritizedTasks(newTask);
            tasks.put(task.getId(), newTask);
            return task.getId();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            prioritizedTasks.add(oldTask); // Старое задание возвращаем без проверок
            return null;
        }
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
        Task oldSubtask = subtasks.get(subtask.getId());
        try {
            removeFromPrioritizedTasks(oldSubtask);
            Subtask newSubtask = new Subtask(subtask);
            addToPrioritizedTasks(newSubtask);
            subtasks.put(subtask.getId(), newSubtask);
            calcEpicStatus(subtask.getEpicId());
            calcEpicDuration(subtask.getEpicId());
            return subtask.getId();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            prioritizedTasks.add(oldSubtask);
            return null;
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(Integer epicId) {
        return epics.get(epicId).getChildrenTasks().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void calcEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId);
        Set<Integer> epicSubtasks = epic.getChildrenTasks();
        boolean allNew = epicSubtasks.stream()
                .allMatch(subtaskId -> subtasks.containsKey(subtaskId) && subtasks.get(subtaskId).getStatus() == TaskStatus.NEW);
        boolean allDone = epicSubtasks.stream()
                .allMatch(subtaskId -> subtasks.containsKey(subtaskId) && subtasks.get(subtaskId).getStatus() == TaskStatus.DONE);
        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public void calcEpicDuration(Integer epicId) {
        Epic epic = epics.get(epicId);
        if (epic.getChildrenTasks().isEmpty()) {
            epic.setDuration(Duration.ZERO.toMinutes());
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        Duration epicDuration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        Optional<LocalDateTime> epicStart = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        epic.setDuration(epicDuration.toMinutes());
        epicStart.ifPresent(epic::setStartTime);
        epicStart.ifPresent(epic::setEndTime);
    }

    public List<Task> getHistory() {
        return history.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Добавление задачи в менеджер при восстановлении
    protected void addTask(Task task) {
        try {
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
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // Восстановление связей между эпиками и подзадачами
    protected void updateSubtasksDependencies() {
        subtasks.keySet().forEach(subtaskId -> {
            Subtask subtask = subtasks.get(subtaskId);
            Epic epic = epics.get(subtask.getEpicId());
            epic.addChild(subtaskId);
        });
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

    private void addToPrioritizedTasks(Task task) throws IllegalArgumentException {
        if (hasIntersections(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        } else if (task.getStartTime() != null && task.getDuration() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        if (task == null || task.getStartTime() == null) return;
        prioritizedTasks.remove(task);
    }
}
