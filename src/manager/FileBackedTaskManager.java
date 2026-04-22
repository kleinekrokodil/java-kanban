package manager;

import manager.exceptions.*;
import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    static final String HEADER_STRING = "id,type,name,status,description,epic";

    public FileBackedTaskManager(String filename) {
        super();
        this.file = new File(filename);
    }

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager mgr = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.equals(HEADER_STRING) || line.isBlank()) continue;
                Task task = taskFromString(line);
                if (task == null) {
                    throw new ManagerReadException("Ошибка при восстановлении задачи из файла");
                }
                mgr.addTask(task);
            }
        } catch (IOException e) {
            throw new ManagerReadException("Невозможно восстановить состояние менеджера");
        }
        mgr.updateSubtasksDependencies();
        mgr.save();
        return mgr;
    }

    private void save() {
        // Словарь со строковыми представлениями всех задач, отсортированный по id
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER_STRING + "\n");
            for (Task task : getAllTasks()) {
                writer.write(taskToString(task));
            }
            for (Epic epic : getAllEpics()) {
                writer.write(taskToString(epic));
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(taskToString(subtask));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Невозможно сохранить состояние менеджера");
        }
    }

    private String taskToString(Task task) {
        TaskType taskType = TaskType.TASK;
        if (task.getType() == TaskType.EPIC) {
            taskType = TaskType.EPIC;
        } else if (task.getType() == TaskType.SUBTASK) {
            taskType = TaskType.SUBTASK;
        }
        return String.format("%d,%s,%s,%s,%s,%s\n",
                task.getId(),
                taskType,
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                taskType == TaskType.SUBTASK ? ((Subtask) task).getEpicId().toString() : "");
    }

    private static Task taskFromString(String value) {
        String[] fields = value.split(",");
        TaskType type = TaskType.valueOf(fields[1]);
        switch (type) {
            case TASK:
                Task task = new Task(fields[2], fields[4]);
                task.setId(Integer.parseInt(fields[0]));
                task.setStatus(TaskStatus.valueOf(fields[3]));
                return task;
            case EPIC:
                Epic epic = new Epic(fields[2], fields[4]);
                epic.setId(Integer.parseInt(fields[0]));
                epic.setStatus(TaskStatus.valueOf(fields[3]));
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(fields[2], fields[4]);
                subtask.setId(Integer.parseInt(fields[0]));
                subtask.setStatus(TaskStatus.valueOf(fields[3]));
                subtask.setEpicId(Integer.parseInt(fields[5]));
                return subtask;
        }
        return null;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer subtaskId) {
        super.deleteSubtaskById(subtaskId);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Integer createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public Integer createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer createSubtask(Subtask subtask, Epic epic) {
        int id = super.createSubtask(subtask, epic);
        save();
        return id;
    }

    @Override
    public Integer updateTask(Task task) {
        int id = super.updateTask(task);
        save();
        return id;
    }

    @Override
    public Integer updateEpic(Epic epic) {
        int id = super.updateEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer updateSubtask(Subtask subtask) {
        int id = super.updateSubtask(subtask);
        save();
        return id;
    }
}
