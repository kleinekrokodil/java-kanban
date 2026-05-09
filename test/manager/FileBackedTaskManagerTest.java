package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;


import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tmpFile;

    @BeforeEach
    public void beforeEach() {
        try {
            tmpFile = File.createTempFile("backup", ".csv", new File("./"));
            taskManager = new FileBackedTaskManager(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации FileBackedTaskManager");
        }
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        taskManager.deleteAllTasks(); // Сохраняем пустой менеджер
        FileBackedTaskManager anotherManager = FileBackedTaskManager.loadFromFile(tmpFile);

        assertTrue(anotherManager.getAllTasks().isEmpty());
        assertTrue(anotherManager.getAllEpics().isEmpty());
        assertTrue(anotherManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadNotEmptyFile() {
        Task task = new Task("Task 1", "Description 1");
        task.setStatus(TaskStatus.IN_PROGRESS);
        int taskId = taskManager.createTask(task);

        Epic epic = new Epic("Epic 1", "Epic 1 Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 Description");
        subtask.setEpicId(epicId);
        subtask.setStatus(TaskStatus.DONE);
        int subtaskId = taskManager.createSubtask(subtask, epic);

        // Создаем копию менеджера из сохраненного файла
        FileBackedTaskManager anotherManager = FileBackedTaskManager.loadFromFile(tmpFile);

        // Проверяем количество
        assertEquals(1, anotherManager.getAllTasks().size());
        assertEquals(1, anotherManager.getAllEpics().size());
        assertEquals(1, anotherManager.getAllSubtasks().size());

        // Проверяем данные конкретной задачи
        Task loadedTask = anotherManager.getTaskById(taskId);
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getStatus(), loadedTask.getStatus());

        // Проверяем связь эпика с подзадачей
        Subtask loadedSubtask = anotherManager.getSubtaskById(subtaskId);
        epic = taskManager.getEpicById(epicId); // загрузить эпик из менеджера
        Epic loadedEpic = anotherManager.getEpicById(epicId);
        assertEquals(epicId, loadedSubtask.getEpicId());
        assertTrue(anotherManager.getEpicById(epicId).getChildrenTasks().contains(subtaskId));
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
    }
}
