package manager;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Integer, Node> id2Node;
    private Node head;
    private Node tail;

    private void linkLast(Task task) {
        Node node = new Node(task);
        final int id = task.getId();
        id2Node.put(id, node);
        Node prevNode = tail;
        tail = node;
        if (prevNode == null) {
            head = node;
        } else {
            prevNode.setNext(tail);
        }
        tail.setPrev(prevNode);
    }

    private List<Task> getTasks() {
        List<Task> allTasks = new ArrayList<>();
        Node node = head;
        while (node != null) {
            allTasks.add(node.getTask());
            node = node.getNext();
        }
        return allTasks;
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        Node prev = node.getPrev();
        Node next = node.getNext();
        if (node == head) {
            head = next;
        }
        if (node == tail) {
            tail = prev;
        }
        if (prev != null) {
            prev.setNext(next);
        }
        if (next != null) {
            next.setPrev(prev);
        }
    }

    public InMemoryHistoryManager() {
        this.id2Node = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            System.out.println("Передано не инициализированное задание");
            return;
        }
        if (id2Node.containsKey(task.getId())) {
            removeNode(id2Node.remove(task.getId()));
        }
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        removeNode(id2Node.remove(id));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
