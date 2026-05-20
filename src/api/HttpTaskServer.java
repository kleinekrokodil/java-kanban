package api;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    TaskManager mgr;
    HttpServer httpServer;

    public HttpTaskServer(TaskManager mgr) throws IOException {
        this.mgr = mgr;
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler(mgr));
        httpServer.createContext("/subtasks", new SubtasksHandler(mgr));
        httpServer.createContext("/epics", new EpicsHandler(mgr));
        httpServer.createContext("/history", new HistoryHandler(mgr));
        httpServer.createContext("/prioritized", new PrioritizedHandler(mgr));
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
