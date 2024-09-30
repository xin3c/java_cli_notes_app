import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class NoteClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;

    public NoteClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);
        } catch (IOException e) {
            System.out.println("Unable to connect to the server.");
            System.exit(1);
        }
    }

    public String readUntilNull(BufferedReader in) {
        StringBuilder response = new StringBuilder();
        int ch;
        try {
            while ((ch = in.read()) != -1) {
                //System.out.print((char)ch);
                if (ch == '\0') {
                    break;
                }
                response.append((char) ch);
            }
        } catch (IOException e) {
            System.out.println("Error receiving message: " + e.getMessage());
        }
        return response.toString();
    }


    public void start() {
        System.out.println("Welcome to the Note Manager");
        String command = null;
        while (!Objects.equals(command, "3")) {
            System.out.println("Enter a command: \n 1 - LOGIN\n 2 - REGISTER\n 3 - EXIT");
            command = scanner.nextLine();
            switch (command) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegistration();
                    break;
                case "3":
                    System.out.println("Goodbye.");
                    closeConnection();
                    break;
                default:
                    System.out.println("Invalid command.");
            }
        }
    }


    private void handleRegistration() {
        System.out.println("Enter a username: ");
        String username = scanner.nextLine();
        System.out.println("Enter a password: ");
        String password = scanner.nextLine();
        out.println("REGISTER " + username + " " + password);
        try {
            String response = in.readLine();
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("Error during registration.");
        }
    }

    private void handleLogin() {
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        out.println("LOGIN " + username + " " + password);
        String response = null;
        try {
            response = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response);
        if (response.contains("Login successful")) {
            showMainMenu();
        }
    }

    private void showMainMenu() {
        String command = null;
        while (!Objects.equals(command, "3")) {
            System.out.println("Main Menu:");
            System.out.println("Enter a command:\n 1 - CREATE_NOTE\n 2 - VIEW_NOTES\n 3 - LOGOUT");
            command = scanner.nextLine();
            switch (command) {
                case "1":
                    createNote();
                    break;
                case "2":
                    viewNotes();
                    break;
                case "3":
                    System.out.println("Goodbye.");
                    closeConnection();
                    break;
                default:
                    System.out.println("Invalid command.");
            }
        }
    }

    private void createNote() {
        System.out.println("Enter the note title: ");
        String title = scanner.nextLine();
        System.out.println("Enter the note content: ");
        String content = scanner.nextLine();
        out.println("CREATE_NOTE\n" + title + "\n" + content);
        System.out.println(readUntilNull(in));
    }

    private void viewNotes() {
        out.println("VIEW_NOTES");
        try {
            System.out.println(readUntilNull(in));
            out.println(new Scanner(System.in).nextLine());
            viewSingleNote();
        } catch (NumberFormatException e) {
            System.out.println("Error viewing notes.");
        }
    }

    private void viewSingleNote() {
        try {
            System.out.println(in.readLine());
            System.out.println("Choose an action: \n 1 - EDIT_NOTE\n 2 - DELETE_NOTE\n 3 - SHARE_NOTE\n 4 - BACK");
            String command = scanner.nextLine();
            switch (command) {
                case "1":
                    editNote();
                    break;
                case "3":
                    shareNote();
                    break;
                default:
                    out.println(command);
                    System.out.println(in.readLine());
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void editNote() {
        try {
            System.out.println(in.readLine());
            String newTitle = scanner.nextLine();
            System.out.println(in.readLine());
            String newContent = scanner.nextLine();
            out.println(newTitle);
            out.println(newContent);
            System.out.println(in.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void shareNote() {
        System.out.println("Enter the username to share the note with: ");
        String usernameToShare = scanner.nextLine();
        out.println(usernameToShare);

        try {
            String response = in.readLine();
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("Error sharing note.");
        }
    }

    private void closeConnection() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("Error closing connection.");
        }
    }

    public static void main(String[] args) {
        NoteClient client = new NoteClient("localhost", 12345);
        client.start();
    }
}
