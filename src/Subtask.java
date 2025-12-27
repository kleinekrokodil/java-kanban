public class Subtask extends Task {
    private Epic parent;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.parent = epic;
        epic.addChild(this);
    }

    public Epic getParent() {
        return parent;
    }

    public void setParent(Epic epic) {
        this.parent.removeChild(this);
        this.parent = epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        parent.updateEpicStatus(this);
    }
}
