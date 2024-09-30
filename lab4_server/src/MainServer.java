import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer {
    private static final int PORT = 12345;
    private static final Logger logger = Logger.getLogger(MainServer.class.getName());
    private ExecutorService executorService;
    private boolean isRunning;

    public MainServer() {
        this.executorService = Executors.newCachedThreadPool();
        this.isRunning = true;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.log(Level.INFO, "Server started on port " + PORT);
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                logger.log(Level.INFO, "Client connected: " + clientSocket.getInetAddress());
                executorService.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error starting server: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                logger.log(Level.INFO, "Server is shutting down.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error shutting down server: " + e.getMessage());
        }
    }

    public static void main() {
        MainServer server = new MainServer();
        server.startServer();
    }
}
