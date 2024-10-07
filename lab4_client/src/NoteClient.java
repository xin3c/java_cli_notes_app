import java.io.*;
import java.net.*;
import java.util.Scanner;

@SuppressWarnings("ALL")
public class NoteClient {

    private static String SERVER_IP;
    private static final int SERVER_PORT = 12345; 
    private Socket socket;
    private BufferedReader in; 
    private PrintWriter out; 
    private BufferedReader console; 

    public NoteClient() {
        try {
            System.out.println("Type address of the Server:\n");
            console = new BufferedReader(new InputStreamReader(System.in));
            SERVER_IP = console.readLine();
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true); 

            Thread readerThread = new Thread(new ServerReader()); 
            readerThread.start(); 

            String userInput;
            while ((userInput = console.readLine()) != null) { 
                out.println(userInput); 
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage()); 
        } finally {
            try { socket.close(); } catch (IOException e) { } 
        }
    }

    private class ServerReader implements Runnable { 
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = in.readLine()) != null) { 
                    System.out.println(serverMessage); 
                }
            } catch (IOException e) {
                System.err.println("Disconnected from server.");
            }
        }
    }

    public static void main(String[] args) {
        new NoteClient(); 
    }
}
