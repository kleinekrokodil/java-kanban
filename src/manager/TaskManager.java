package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;

public interface TaskManager {
    Task getTaskById(Integer taskId);

    Epic getEpicById(Integer epicId);

    Subtask getSubtaskById(Integer subtaskId);

    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getAllSubtasks();

    void deleteAllTasks();

    void deleteTaskById(Integer taskId);

    // Эпики удаляем со всеми их подзадачами
    void deleteEpicById(Integer epicId);

    void deleteSubtaskById(Integer subtaskId);

    void deleteAllEpics();

    void deleteAllSubtasks();

    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask, Epic epic);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    ArrayList<Subtask> getEpicSubtasks(Integer epicId);

    void calcEpicStatus(Integer epicId);
}
