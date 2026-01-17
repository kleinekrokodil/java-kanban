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
        taskWithEqualId.setId(taskId);
        taskWithEqualIdId = taskManager.updateTask(taskWithEqualId);
        assertEquals(taskWithEqualIdId, taskId, "При обновлении задачи изменился id");
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
}