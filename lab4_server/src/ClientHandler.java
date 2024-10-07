import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Map<String, User> users;
    private BufferedReader in;
    private PrintWriter out;
    private User currentUser;
    private Logger logger = Logger.getLogger(NoteServer.class.getName());

    public ClientHandler(Socket socket, Map<String, User> users) {
        this.clientSocket = socket;
        this.users = users;
        this.currentUser = null;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Welcome to the Note Manager");

            boolean running = true;
            while (running) {
                if (currentUser == null) {
                    showAuthMenu();
                } else {
                    showMainMenu();
                }
            }

        } catch (IOException e) {
            logger.warning("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void showAuthMenu() throws IOException {
        out.println("Enter a command: \n 1 - LOGIN\n 2 - REGISTER\n 3 - EXIT");
        String choice = in.readLine();
        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                register();
                break;
            case "3":
                out.println("Goodbye!");
                logger.info("Client disconnected.");
                System.exit(0);
                break;
            default:
                out.println("Invalid command.");
                break;
        }
    }

    private void login() throws IOException {
        out.println("Enter your username:");
        String username = in.readLine();
        out.println("Enter your password:");
        String password = in.readLine();
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            out.println("Login successful");
            logger.info("User " + username + " logged in.");
        } else {
            out.println("Invalid username or password.");
        }
    }

    private void register() throws IOException {
        out.println("Enter a username:");
        String username = in.readLine();
        out.println("Enter a password:");
        String password = in.readLine();
        synchronized (users) {
            if (users.containsKey(username)) {
                out.println("Username already exists.");
            } else {
                User newUser = new User(username, password);
                users.put(username, newUser);
                NoteServer.saveUserData();
                out.println("Registration successful");
                logger.info("User " + username + " registered.");
            }
        }
    }

    private void showMainMenu() throws IOException {
        out.println("Main Menu:\nEnter a command:\n 1 - CREATE_NOTE\n 2 - VIEW_NOTES\n 3 - LOGOUT");
        String choice = in.readLine();
        switch (choice) {
            case "1":
                createNote();
                break;
            case "2":
                viewNotes();
                break;
            case "3":
                currentUser = null;
                out.println("Logged out.");
                logger.info("User logged out.");
                break;
            default:
                out.println("Invalid command.");
                break;
        }
    }

    private void createNote() throws IOException {
        out.println("Enter the note title:");
        String title = in.readLine();
        out.println("Enter the note content:");
        String content = in.readLine();
        Note note = new Note(title, content, currentUser.getUsername());
        currentUser.addNote(note);
        NoteServer.saveUserData();
        out.println("Note created successfully.");
        logger.info("User " + currentUser.getUsername() + " created a note.");
    }

    private void viewNotes() throws IOException {
        List<Note> notes = currentUser.getAllNotes();
        if (notes.isEmpty()) {
            out.println("No notes available.");
            return;
        }
        for (int i = 0; i < notes.size(); i++) {
            out.println((i + 1) + "• Author: "+ notes.get(i).getOwnerUsername() + ". Title: " + notes.get(i).getTitle());
        }
        out.println("Select a note.");
        String choice = in.readLine();
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < notes.size()) {
                Note selectedNote = notes.get(index);
                out.println("Note " + (index + 1) + " selected.");
                noteActions(selectedNote);
            } else {
                out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input.");
        }
    }

    private void noteActions(Note note) throws IOException {
        boolean back = false;
        while (!back) {
            out.println("Note Title: " + note.getTitle());
            out.println("Note Content: " + note.getContent());
            out.println("Choose an action: ");
            if (note.getOwnerUsername().equals(currentUser.getUsername()) || note.isSharedWith(currentUser.getUsername())) {
                out.println(" 1 - EDIT_NOTE\n 2 - DELETE_NOTE\n 3 - SHARE_NOTE\n 4 - BACK");
            } else {
                out.println("You do not have access to this note.");
                return;
            }
            String choice = in.readLine();
            if (note.getOwnerUsername().equals(currentUser.getUsername()) || note.isSharedWith(currentUser.getUsername())) {
                switch (choice) {
                    case "1":
                        editNote(note);
                        break;
                    case "2":
                        deleteNote(note);
                        back = true;
                        break;
                    case "3":
                        shareNote(note);
                        break;
                    case "4":
                        back = true;
                        break;
                    default:
                        out.println("Invalid command.");
                        break;
                }
            } else {
                back = true;
                break;
            }

        }
    }


    private void editNote(Note note) throws IOException {
        out.println("Enter new content:");
        String content = in.readLine();
        note.setContent(content);
        NoteServer.saveUserData();
        out.println("Note updated.");
        logger.info("User " + currentUser.getUsername() + " edited a note.");
    }

    private void deleteNote(Note note) {
        currentUser.deleteNote(note);
        for (User user : users.values()) {
            user.getSharedNotes().remove(note);
        }
        NoteServer.saveUserData();
        out.println("Note deleted.");
        logger.info("User " + currentUser.getUsername() + " deleted a note.");
    }

    private void shareNote(Note note) throws IOException {
        out.println("Enter username to share with:");
        String username = in.readLine();
        User user = users.get(username);
        if (user != null) {
            note.shareWith(username);
            user.addSharedNote(note);
            NoteServer.saveUserData();
            out.println("Note shared with " + username + ".");
            logger.info("User " + currentUser.getUsername() + " shared a note with " + username + ".");
        } else {
            out.println("User not found.");
        }
    }
}
