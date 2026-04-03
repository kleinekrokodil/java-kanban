package manager;

import task.Task;

public class Node {
    private Task task;
    private manager.Node next;
    private manager.Node prev;

    public Node(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public manager.Node getNext() {
        return next;
    }

    public void setNext(manager.Node next) {
        this.next = next;
    }

    public manager.Node getPrev() {
        return prev;
    }

    public void setPrev(manager.Node prev) {
        this.prev = prev;
    }
}
