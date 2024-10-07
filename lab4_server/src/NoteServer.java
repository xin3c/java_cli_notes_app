import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public class NoteServer {

    private static final int PORT = 12345;
    private static Map<String, User> users = new ConcurrentHashMap<>();
    private static Logger logger = Logger.getLogger(NoteServer.class.getName());
    private static final String DATA_FILE = "users_data.ser";

    public static void main(String[] args) {
        setupLogger();
        loadUserData();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("new client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, users);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("[!!!] Could not start server: " + e.getMessage());
            logger.severe("[!!!] Could not start server: " + e.getMessage());
        } finally {
            if (serverSocket != null) try { serverSocket.close(); } catch (IOException e) { }
        }
    }

    private static void setupLogger() {
        try {
            LogManager.getLogManager().reset();
            FileHandler fh = new FileHandler("server.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("[!!!] could not setup logger: " + e.getMessage());
            logger.severe("[!!!] could not setup logger: " + e.getMessage());
        }
    }

    static synchronized void saveUserData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
            logger.info("User data saved successfully.");
        } catch (IOException e) {
            logger.severe("[!!!] error saving user data: " + e.getMessage());
            System.err.println("[!!!] error saving user data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static synchronized void loadUserData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                users = (Map<String, User>) in.readObject();
                logger.info("user data loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                logger.severe("[!!!] error loading user data: " + e.getMessage());
            }
        } else {
            logger.info("no existing user data found.");
        }
    }
}
