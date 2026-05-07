package manager;

import org.junit.jupiter.api.BeforeEach;

public class InMemoryHistoryManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }
}
