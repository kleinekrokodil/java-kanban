package task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EpicTest {
    @Test
    public void shouldBeEqualWithEqualId() {
        Epic epic1 = new Epic("epic1", "epic1");
        epic1.setId(1);
        Epic epic2 = new Epic("epic2", "epic2");
        epic2.setId(1);
        Assertions.assertEquals(epic1, epic2, "Epics with equal id should be equal");
    }
}