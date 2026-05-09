package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;

public interface TaskManager {
    Task getTaskById(Integer taskId);

    Epic getEpicById(Integer epicId);

    Subtask getSubtaskById(Integer subtaskId);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void deleteAllTasks();

    void deleteTaskById(Integer taskId);

    // Эпики удаляем со всеми их подзадачами
    void deleteEpicById(Integer epicId);

    void deleteSubtaskById(Integer subtaskId);

    void deleteAllEpics();

    void deleteAllSubtasks();

    Integer createTask(Task task);

    Integer createEpic(Epic epic);

    Integer createSubtask(Subtask subtask, Epic epic);

    Integer updateTask(Task task);

    Integer updateEpic(Epic epic);

    Integer updateSubtask(Subtask subtask);

    List<Subtask> getEpicSubtasks(Integer epicId);

    void calcEpicStatus(Integer epicId);

    void calcEpicDuration(Integer epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
