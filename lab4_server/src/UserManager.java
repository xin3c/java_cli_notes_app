import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManager {
    private static final Logger logger = Logger.getLogger(UserManager.class.getName());
    private static final String USERS_FILE = "users.txt";
    private Map<String, String> users;

    public UserManager() {
        this.users = new HashMap<>();
        loadUsers();
    }
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load users: " + e.getMessage());
        }
    }
    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save users: " + e.getMessage());
        }
    }

    public boolean registerUser(String login, String password) {
        if (users.containsKey(login)) {
            return false; // Пользователь уже существует
        }
        users.put(login, password);
        saveUsers();
        return true;
    }


    public boolean authenticateUser(String login, String password) {
        return users.containsKey(login) && users.get(login).equals(password);
    }
}
