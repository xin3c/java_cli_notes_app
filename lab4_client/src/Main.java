public class Main {
    public static void main(String[] args) {
        NoteClient noteClient = new NoteClient("localhost", 12345);
        noteClient.start();
    }
}