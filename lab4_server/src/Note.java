import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String content;
    private String ownerUsername; 
    private Set<String> sharedWithUsernames; 

    public Note(String title, String content, String ownerUsername) {
        this.title = title;
        this.content = content;
        this.ownerUsername = ownerUsername;
        this.sharedWithUsernames = Collections.synchronizedSet(new HashSet<>());
    }

    
    public String getTitle() { return title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getOwnerUsername() { return ownerUsername; }

    public void shareWith(String username) {
        sharedWithUsernames.add(username);
    }

    public boolean isSharedWith(String username) {
        return sharedWithUsernames.contains(username);
    }

    public Set<String> getSharedWithUsernames() {
        return sharedWithUsernames;
    }
}
