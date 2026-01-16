package task;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String name, String description) {
        super(name, description);
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicId = subtask.epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }
}
