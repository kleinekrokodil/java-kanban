package manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


class ManagersTest {
    @Test
    public void shouldReturnInitializedManagers() {
        TaskManager mgr = Managers.getDefault();
        assertNotNull(mgr, "Task manager not initialized");

        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history, "History manager not initialized");
    }
}