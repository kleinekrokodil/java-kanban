package task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubtaskTest {
    @Test
    public void shouldBeEqualWithEqualId() {
        Subtask subtask1 = new Subtask("subtask1", "subtask1");
        subtask1.setId(1);
        Subtask subtask2 = new Subtask("subtask2", "subtask2");
        subtask2.setId(1);
        Assertions.assertEquals(subtask1, subtask2, "Subtasks with equal id should be equal");
    }
}