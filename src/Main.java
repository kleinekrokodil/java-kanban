public class Main {

    public static void main(String[] args) {
        System.out.println("Создание, обновление, удаление одной задачи");
        TaskManager mgr = new TaskManager();
        Task task1 = new Task("task1", "first task");
        Task task2 = new Task("task2", "second task");

        mgr.createTask(task1);
        mgr.createTask(task2);
        System.out.println(mgr.getAllTasks());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        System.out.println(mgr.getAllTasks());

        mgr.deleteTaskById(task1.getId());
        System.out.println(mgr.getAllTasks());

        mgr.deleteAllTasks();
        System.out.println(mgr.getAllTasks());

        System.out.println("Эпики и подзадачи");
        Epic epic1 = new Epic("epic1", "very epic task");
        Subtask subtask1 = new Subtask("subtask1", "part of a great project 1", epic1);
        Subtask subtask2 = new Subtask("subtask2", "part of a great project 2", epic1);

        mgr.createEpic(epic1);
        // Вместе с эпиком будут добавлены его подзадачи
        System.out.println(mgr.getAllTasks());
        System.out.println(mgr.getEpicSubtasks(epic1.getId()));

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        // Статус эпика поменяется на "В работе"
        System.out.println(mgr.getEpicSubtasks(epic1.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        // Статус эпика не поменяется, т.к. есть задача "В работе"
        System.out.println(mgr.getEpicSubtasks(epic1.getId()));
        System.out.println(mgr.getAllTasks());
        // Удаление подзадачи из менеджера, статус эпика должен измениться на "Выполнен"
        mgr.deleteSubtaskById(subtask2.getId());
        System.out.println(mgr.getAllTasks());
        System.out.println(mgr.getSubtasks());

        // Создание пустого эпика и добавление его в менеджер
        Epic epic2 = new Epic("epic2", "another epic task");
        mgr.createEpic(epic2);
        Subtask subtask3 = new Subtask("subtask3", "part of the second epic", epic2);
        mgr.createSubtask(subtask3);
        // Проверка метода getEpics
        System.out.println(mgr.getEpics());
        System.out.println(mgr.getAllTasks());
        mgr.deleteEpicById(epic2.getId());
        // Эпик удаляется вместе с подзадачами
        System.out.println(mgr.getAllTasks());
    }
}
