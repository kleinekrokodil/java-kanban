package task;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TaskTest {
    @Test
    public void shouldBeEqualWithEqualId() {
        Task task1 = new Task("task1", "task1");
        task1.setId(1);
        Task task2 = new Task("task2", "task2");
        task2.setId(1);
        assertEquals(task1, task2, "Tasks with equal id should be equal");
    }
}