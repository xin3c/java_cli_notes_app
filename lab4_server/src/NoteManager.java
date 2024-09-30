import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NoteManager {
    private static final String NOTES_DIR = "./notes/";
    private static final Logger logger = Logger.getLogger(NoteManager.class.getName());

    public NoteManager() {

        File notesDir = new File(NOTES_DIR);
        if (!notesDir.exists()) {
            notesDir.mkdirs();
        }
    }

    public void createNote(String user, String title, String content) {
        try {
            File userDir = new File(NOTES_DIR + user);
            if (!userDir.exists()) {
                userDir.mkdirs();
            }

            String noteFileName = "note_" + System.currentTimeMillis() + ".txt"; // Уникальное имя файла
            File noteFile = new File(userDir, noteFileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(noteFile))) {
                writer.write("Title: " + title + "\n");
                writer.write("Content: " + content + "\n");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving note: " + e.getMessage());
        }
    }

    public List<Note> getNotes(String user, int page, int pageSize) {
        File userDir = new File(NOTES_DIR + user);
        if (!userDir.exists()) {
            return Collections.emptyList();
        }

        File[] noteFiles = userDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (noteFiles == null || noteFiles.length == 0) {
            return Collections.emptyList();
        }

        int totalNotes = noteFiles.length;
        int start = (page - 1) * pageSize;
        if (start >= totalNotes) {
            return Collections.emptyList();  // Return an empty list if the page is out of range
        }
        int end = Math.min(start + pageSize, totalNotes);

        // Load only the notes on the requested page
        List<Note> notes = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            notes.add(loadNoteFromFile(noteFiles[i]));
        }

        return notes;
    }
    public List<Note> getAllNotes(String user) {
        File userDir = new File(NOTES_DIR + user);
        if (!userDir.exists()) {
            return Collections.emptyList();
        }

        File[] noteFiles = userDir.listFiles((dir, name) -> name.endsWith(".txt"));

        if (noteFiles == null || noteFiles.length == 0) {
            return Collections.emptyList();
        }

        List<Note> notes = new ArrayList<>(noteFiles.length);
        for (File noteFile : noteFiles) {
            notes.add(loadNoteFromFile(noteFile));
        }

        return notes;
    }

    private Note loadNoteFromFile(File noteFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(noteFile))) {
            String title = reader.readLine().substring(7); // Убираем "Title: "
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return new Note(title, content.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading note: " + e.getMessage());
            return null;
        }
    }

    public void editNote(String user, int index, String newTitle, String newContent) {
        File userDir = new File(NOTES_DIR + user);
        File[] noteFiles = userDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (noteFiles == null || index < 0 || index >= noteFiles.length) {
            return;
        }
        File noteFile = noteFiles[index];
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(noteFile))) {
            writer.write("Title: " + newTitle + "\n");
            writer.write("Content: " + newContent + "\n");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error editing note: " + e.getMessage());
        }
    }

    public void deleteNote(String user, int index) {
        File userDir = new File(NOTES_DIR + user);
        File[] noteFiles = userDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (noteFiles == null || index < 0 || index >= noteFiles.length) {
            return;
        }
        noteFiles[index].delete();
    }

    public void shareNote(String owner, int index, String otherUser) {
        File ownerDir = new File(NOTES_DIR + owner);
        File[] noteFiles = ownerDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (noteFiles == null || index < 0 || index >= noteFiles.length) {
            return;
        }
        File noteFile = noteFiles[index];

        try {
            File otherUserDir = new File(NOTES_DIR + otherUser);
            if (!otherUserDir.exists()) {
                otherUserDir.mkdirs();
            }
            File newNoteFile = new File(otherUserDir, noteFile.getName());
            try (BufferedReader reader = new BufferedReader(new FileReader(noteFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(newNoteFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sharing note: " + e.getMessage());
        }
    }

    public Note getNote(String user, int index) {
        File userDir = new File(NOTES_DIR + user);
        if (!userDir.exists()) {
            return null;
        }

        File[] noteFiles = userDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (noteFiles == null || index < 0 || index >= noteFiles.length) {
            return null;
        }

        return loadNoteFromFile(noteFiles[index]);
    }

    public int getTotalPages(String currentUser, int i) {

        File[] noteFiles = new File(NOTES_DIR + currentUser).listFiles((dir, name) -> name.endsWith(".txt"));
        if (noteFiles == null || noteFiles.length == 0) {
            return 0;
        }
        return 1 + (int) (noteFiles.length / i);
    }
}
