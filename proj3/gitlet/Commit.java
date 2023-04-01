package gitlet;


import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;

public class Commit implements Serializable {

    /** text of commit. */
    private String text;
    /** map of blobs. */
    private HashMap<String, String> mapOfBlobs;
    /** parent of commit. */
    private String commitParentString;
    /** time register of commit1. */
    private Date timeRegister;

    /** method of commit */
    public Commit(String text, Date timeRegister) {
        this.timeRegister = timeRegister;
        this.mapOfBlobs = new HashMap<String, String>();
        this.text = text;
        this.commitParentString = null;

    }

    /** method of commit2. */
    public Commit(String message, Date timeRegister,
                  HashMap mapOfBlobs, String commitParentString) {
        this.text = message;
        this.timeRegister = timeRegister;
        this.mapOfBlobs = mapOfBlobs;
        this.commitParentString = commitParentString;
    }
    /** blob of commit.
     * @return map*/
    public HashMap getFileBlob() {
        return mapOfBlobs;
    }

    /** parent of commit.
     * @return parent*/
    public String getParent() {
        return commitParentString;
    }

    /** timestamp.
     * @return time */
    public Date getTimestamp() {
        return timeRegister;
    }

    /** The message in commit.
     * @return string*/
    public String getMessage() {
        return text;
    }
}
