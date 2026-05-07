package manager;

import task.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest <T extends TaskManager>  {
    protected T taskManager;

    @Test
    void createTask() {
        Task task = new Task("Test createTask", "Test createTask description");
        final int taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");

        // Проверки на неизменность полей
        assertEquals(task.getName(), savedTask.getName(), "Наименования задач не совпадают");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач не совпадают");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статусы задач не совпадают");

        // Задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
        Task taskWithEqualId = new Task("Test createTask with id 1", "Test taskWithId1 description");
        taskWithEqualId.setId(taskId);

        int taskWithEqualIdId = taskManager.createTask(taskWithEqualId);
        assertNotEquals(taskWithEqualIdId, taskId, "При добавлении новой задачи произошла замена старой");
        // Замена id через сеттер
        taskWithEqualId.setId(taskId);
        taskManager.updateTask(taskWithEqualId);
        assertEquals(taskWithEqualId.getName(), taskManager.getTaskById(taskId).getName(), "Должны обновиться данные задачи с id=1");
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Test createEpic", "Test createEpic description");
        final int epicId = taskManager.createEpic(epic);

        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic, savedEpic, "Задачи не совпадают.");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void createSubtask() {
        Epic epic = new Epic("Test createSubtask epic", "Test createSubtask epic description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test createSubtask", "Test createSubtask description");
        final int subtaskId = taskManager.createSubtask(subtask, epic);

        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Задача не найдена.");
        assertEquals(subtask, savedSubtask, "Задачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void testDeleteAllKindOfTasks() {
        Epic epic = new Epic("Test createSubtask epic", "Test createSubtask epic description");
        Integer epicId = taskManager.createEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size(), "Неверное количество эпиков.");

        Subtask subtask1 = new Subtask("Test createSubtask", "Test createSubtask description");
        final int subtaskId = taskManager.createSubtask(subtask1, taskManager.getEpicById(epicId));
        Subtask subtask2 = new Subtask("Test createSubtask 2", "Test createSubtask description 2");
        taskManager.createSubtask(subtask2, taskManager.getEpicById(epicId));
        assertEquals(2, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");

        Task task1 = new Task("Test createTask", "Test createTask description");
        final int taskId = taskManager.createTask(task1);
        Task task2 = new Task("Test createTask", "Test createTask description");
        taskManager.createTask(task2);
        assertEquals(2, taskManager.getAllTasks().size(), "Неверное количество задач.");

        taskManager.deleteSubtaskById(subtaskId);
        assertEquals(1, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(1, taskManager.getEpicById(epic.getId()).getChildrenTasks().size(), "Неверное количество подзадач в эпике.");

        taskManager.createSubtask(subtask1, taskManager.getEpicById(epicId));
        taskManager.deleteAllSubtasks();
        assertEquals(0, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(0, taskManager.getEpicById(epic.getId()).getChildrenTasks().size(), "Неверное количество подзадач в эпике.");

        taskManager.createSubtask(subtask1, taskManager.getEpicById(epicId));
        taskManager.createSubtask(subtask2, taskManager.getEpicById(epicId));
        assertEquals(2, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(2, taskManager.getEpicById(epic.getId()).getChildrenTasks().size(), "Неверное количество подзадач в эпике.");

        taskManager.deleteAllEpics();
        assertEquals(0, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(0, taskManager.getAllEpics().size(), "Список эпиков должен быть пуст");

        taskManager.deleteTaskById(taskId);
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач.");
        taskManager.createTask(task1);
        taskManager.deleteAllTasks();
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач.");
    }

    @Test
    void testCalculateEpicStatus() {
        Epic epic = new Epic("Test createSubtask epic", "Test createSubtask epic description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test createSubtask", "Test createSubtask description");
        Subtask subtask2 = new Subtask("Test createSubtask 2", "Test createSubtask description 2");
        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);
        taskManager.createSubtask(subtask2, epic);

        epic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, epic.getStatus());

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        epic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        epic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void testCalculateEpicDuration() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test createSubtask", "Test createSubtask description");
        subtask1.setDuration(60);
        subtask1.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));

        Subtask subtask2 = new Subtask("Test createSubtask 2", "Test createSubtask description 2");
        subtask2.setDuration(30);
        subtask2.setStartTime(LocalDateTime.of(2026, 5, 1, 11, 30));

        taskManager.createSubtask(subtask1, epic);
        taskManager.createSubtask(subtask2, epic);

        epic = taskManager.getEpicById(epic.getId());
        assertEquals(90, epic.getDuration().toMinutes());
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 0), epic.getStartTime());
    }

    @Test
    void testGetPrioritizedTasks() {
        Task task1 = new Task("Test getPrioritizedTasks 1", "task 1");
        task1.setStartTime(LocalDateTime.of(2026, 5, 1, 12, 0));
        task1.setDuration(60);

        Task task2 =  new Task("Test getPrioritizedTasks 2", "task 2");
        task2.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        task2.setDuration(30);

        Task task3 =  new Task("Test getPrioritizedTasks 3", "task 3");
        task3.setStartTime(LocalDateTime.of(2026, 5, 1, 14, 0));
        task3.setDuration(45);

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertEquals(3, prioritized.size());
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 0), prioritized.get(0).getStartTime());
        assertEquals(LocalDateTime.of(2026, 5, 1, 12, 0), prioritized.get(1).getStartTime());
        assertEquals(LocalDateTime.of(2026, 5, 1, 14, 0), prioritized.get(2).getStartTime());
    }

    @Test
    void testHasIntersections() {
        Task task1 = new Task("Test testHasIntersections 1", "task 1");
        task1.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        task1.setDuration(90);
        taskManager.createTask(task1);

        Task task2 =  new Task("Test testHasIntersections 2", "task 2");
        task2.setStartTime(LocalDateTime.of(2026, 5, 1, 11, 0));
        task2.setDuration(30);

        assertNull(taskManager.createTask(task2));
    }
}