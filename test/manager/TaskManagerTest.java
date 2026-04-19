package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

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
        taskManager.createEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size(), "Неверное количество эпиков.");

        Subtask subtask1 = new Subtask("Test createSubtask", "Test createSubtask description");
        final int subtaskId = taskManager.createSubtask(subtask1, epic);
        Subtask subtask2 = new Subtask("Test createSubtask 2", "Test createSubtask description 2");
        taskManager.createSubtask(subtask2, epic);
        assertEquals(2, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");

        Task task1 = new Task("Test createTask", "Test createTask description");
        final int taskId = taskManager.createTask(task1);
        Task task2 = new Task("Test createTask", "Test createTask description");
        taskManager.createTask(task2);
        assertEquals(2, taskManager.getAllTasks().size(), "Неверное количество задач.");

        taskManager.deleteSubtaskById(subtaskId);
        assertEquals(1, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(1, epic.getChildrenTasks().size(), "Неверное количество подзадач в эпике.");

        taskManager.createSubtask(subtask1, epic);
        taskManager.deleteAllSubtasks();
        assertEquals(0, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(0, epic.getChildrenTasks().size(), "Неверное количество подзадач в эпике.");

        taskManager.createSubtask(subtask1, epic);
        taskManager.createSubtask(subtask2, epic);
        assertEquals(2, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(2, epic.getChildrenTasks().size(), "Неверное количество подзадач в эпике.");

        taskManager.deleteAllEpics();
        assertEquals(0, taskManager.getAllSubtasks().size(), "Неверное количество подзадач.");
        assertEquals(0, taskManager.getAllEpics().size(), "Список эпиков должен быть пуст");

        taskManager.deleteTaskById(taskId);
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач.");
        taskManager.createTask(task1);
        taskManager.deleteAllTasks();
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач.");
    }
}