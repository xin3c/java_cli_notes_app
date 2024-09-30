import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private UserManager userManager;
    private NoteManager noteManager;
    private boolean isAuthenticated = false;
    private String currentUser;

    // Маппинг команд
    private Map<String, Runnable> commandMap;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.userManager = new UserManager();
        this.noteManager = new NoteManager();
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing streams: " + e.getMessage());
        }

        // Инициализация командного маппинга
        commandMap = new HashMap<>();
        commandMap.put("CREATE_NOTE", this::handleCreateNote);
        commandMap.put("VIEW_NOTES", this::handleViewNotes);
    }

    @Override
    public void run() {
        try {
            String command;
            while ((command = in.readLine()) != null) {
                logger.log(Level.INFO, "Received command from client: " + command);
                if (command.startsWith("REGISTER")) {
                    handleRegistration(command);
                } else if (command.startsWith("LOGIN")) {
                    handleLogin(command);
                } else if (isAuthenticated) {
                    executeAuthenticatedCommand(command);
                } else {
                    out.println("Please log in first.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling client: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnections();
        }
    }

    // Метод для выполнения команд после аутентификации
    private void executeAuthenticatedCommand(String command) {
        String[] parts = command.split(" ", 2);
        String commandKey = parts[0];
        Runnable action = commandMap.get(commandKey);

        if (action != null) {
            action.run();
        } else {
            out.println("Unknown command: " + commandKey);
        }
    }

    // Логика работы с пользователем
    private void handleRegistration(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 3) {
            String login = parts[1];
            String password = parts[2];
            if (userManager.registerUser(login, password)) {
                out.println("Registration successful");
            } else {
                out.println("User already exists");
            }
        } else {
            out.println("Invalid REGISTER command format");
        }
    }

    private void handleLogin(String command) throws InterruptedException {
        String[] parts = command.split(" ");
        if (parts.length == 3) {
            String login = parts[1];
            String password = parts[2];
            if (userManager.authenticateUser(login, password)) {
                isAuthenticated = true;
                currentUser = login;
                out.println("Login successful");
            } else {
                out.println("Invalid login or password");
            }
        } else {
            out.println("Invalid LOGIN command format");
        }
    }


    // Команды работы с заметками
    private void handleCreateNote() {
        try {
            String title = in.readLine();
            String content = in.readLine();
            noteManager.createNote(currentUser, title, content);
            out.println("Note created successfully.\0");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating note: " + e.getMessage());
        }
    }



    private void handleViewNotes() {
        try {
            List<Note> notes = noteManager.getAllNotes(currentUser);

            if (notes.isEmpty()) {
                out.println("No notes to display.");
                return;
            }

            for (int i = 0; i < notes.size(); i++) {
                out.println((i + 1) + ". " + notes.get(i).getTitle());
            }

            out.println("Select a note.\0");
            String input = in.readLine();
            logger.log(Level.INFO, "Received command from client: " + input);

            try {
                int noteIndex = Integer.parseInt(input) - 1;
                if (noteIndex >= 0 && noteIndex < notes.size()) {
                    handleViewSingleNote(noteIndex-1);
                } else {
                    out.println("Invalid note selection.");
                    handleViewNotes();  // Reload the current page
                }
            } catch (NumberFormatException e) {
                out.println("Invalid input.");
                handleViewNotes();  // Reload the current page
            }

        } catch (IOException e) {
            out.println("Error handling view notes.");
            logger.log(Level.SEVERE, "Error handling view notes", e);
        }
    }


    private void handleViewSingleNote(int noteIndex) {
        out.println("Note chosen");
        Note note = noteManager.getNote(currentUser, noteIndex);
        if (note != null) {
            try {
                String choice = in.readLine();
                switch (choice) {
                    case "1":
                        handleEditNote(noteIndex);
                        break;
                    case "2":
                        handleDeleteNote(noteIndex);
                        break;
                    case "3":
                        handleShareNote(noteIndex);
                        break;
                    default:
                        out.println("Invalid choice.");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error processing note options: " + e.getMessage());
            }
        } else {
            out.println("Note not found.");
        }
    }

    private void handleEditNote(int index) {
        try {
            logger.log(Level.INFO, "EditNote call");
            out.println("Enter new title: ");
            String newTitle = in.readLine();
            out.println("Enter new content: ");
            String newContent = in.readLine();
            noteManager.editNote(currentUser, index, newTitle, newContent);
            out.println("Note edited successfully.");
        } catch (IOException | NumberFormatException e) {
            out.println("Error editing note.");
        }
    }

    private void handleDeleteNote(int index) {
        try {
            noteManager.deleteNote(currentUser, index);
            out.println("Note deleted successfully.");
        } catch (NumberFormatException e) {
            out.println("Error deleting note.");
        }
    }

    private void handleShareNote(int index) {
        try {
            out.println("Enter username to share with: ");
            String otherUser = in.readLine();
            noteManager.shareNote(currentUser, index, otherUser);
            out.println("Note shared successfully.");
        } catch (IOException | NumberFormatException e) {
            out.println("Error sharing note.");
        }
    }

    private void closeConnections() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing resources: " + e.getMessage());
        }
    }
}
