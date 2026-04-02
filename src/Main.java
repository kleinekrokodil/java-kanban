import manager.*;
import task.*;

public class Main {

    public static void main(String[] args) {
        TaskManager mgr = Managers.getDefault();
        // Две задачи
        Task task1 = new Task("task1", "first task");
        Task task2 = new Task("task2", "second task");
        mgr.createTask(task1);
        mgr.createTask(task2);
        // Эпик с двумя подзадачами
        Epic epic1 = new Epic("epic1", "very epic task");
        mgr.createEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "part of a great project 1");
        mgr.createSubtask(subtask1, epic1);
        Subtask subtask2 = new Subtask("subtask2", "part of a great project 2");
        mgr.createSubtask(subtask2, epic1);
        // Эпик с одной подзадачей
        Epic epic2 = new Epic("epic2", "another epic task");
        mgr.createEpic(epic2);
        Subtask subtask3 = new Subtask("subtask3", "part of the second epic");
        mgr.createSubtask(subtask3, epic2);

        System.out.println("mgr.getHistory()");
        System.out.println(mgr.getHistory().size());
        System.out.println(mgr.getHistory());

        System.out.println("After create");
        System.out.println("mgr.getAllEpics():");
        System.out.println(mgr.getAllEpics());
        System.out.println("mgr.getAllTasks():");
        System.out.println(mgr.getAllTasks());
        System.out.println("mgr.getAllSubtasks()");
        System.out.println(mgr.getAllSubtasks());
        System.out.println("mgr.getHistory()");
        System.out.println(mgr.getHistory().size());
        System.out.println(mgr.getHistory());

        // Измените статусы созданных объектов, распечатайте их.
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач
        task1.setStatus(TaskStatus.IN_PROGRESS);
        mgr.updateTask(task1);
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        mgr.updateSubtask(subtask1);
        subtask3.setStatus(TaskStatus.DONE);
        mgr.updateSubtask(subtask3);

        System.out.println("After statuses update");
        System.out.println("mgr.getAllEpics():");
        System.out.println(mgr.getAllEpics());
        System.out.println("mgr.getHistory()");
        System.out.println(mgr.getHistory().size());
        System.out.println(mgr.getHistory());
        System.out.println("mgr.getAllTasks():");
        System.out.println(mgr.getAllTasks());
        System.out.println("mgr.getHistory()");
        System.out.println(mgr.getHistory().size());
        System.out.println(mgr.getHistory());
        System.out.println("mgr.getAllSubtasks()");
        System.out.println(mgr.getAllSubtasks());
        System.out.println("mgr.getHistory()");
        System.out.println(mgr.getHistory().size());
        System.out.println(mgr.getHistory());

        // удалить одну из задач и один из эпиков
        mgr.deleteTaskById(task2.getId());
        mgr.deleteEpicById(epic2.getId()); // Должен удалиться с subtask 3
        mgr.deleteSubtaskById(subtask1.getId()); // Статус эпика должен поменяться на NEW

        System.out.println("After tasks delete");
        System.out.println("mgr.getAllEpics():");
        System.out.println(mgr.getAllEpics());
        System.out.println("mgr.getAllTasks():");
        System.out.println(mgr.getAllTasks());
        System.out.println("mgr.getAllSubtasks()");
        System.out.println(mgr.getAllSubtasks());
        System.out.println("mgr.getHistory()");
        System.out.println(mgr.getHistory().size());
        System.out.println(mgr.getHistory());
    }
}
