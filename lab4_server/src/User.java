import java.io.Serializable;
import java.util.*;

class User implements Serializable {
    private static final long serialVersionUID = 1L; 

    private String username;
    private String password;
    private List<Note> notes;
    private Set<Note> sharedNotes;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.notes = Collections.synchronizedList(new ArrayList<>());
        this.sharedNotes = Collections.synchronizedSet(new HashSet<>());
    }

    
    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void addNote(Note note) { notes.add(note); }

    public void deleteNote(Note note) { notes.remove(note); }

    public void addSharedNote(Note note) { sharedNotes.add(note); }

    public Set<Note> getSharedNotes() { return sharedNotes; }

    public List<Note> getNotes() { return notes; }

    public List<Note> getAllNotes() {
        List<Note> accessibleNotes = new ArrayList<>(notes);
        accessibleNotes.addAll(sharedNotes);
        return accessibleNotes;
    }
}
