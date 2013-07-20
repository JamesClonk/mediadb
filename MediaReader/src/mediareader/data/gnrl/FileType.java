
package mediareader.data.gnrl;

public class FileType {

    private String id = null;
    private String name = null;
    private String extension = null;
    private String description = null;

    public FileType(File file) {
        this.setExtension(file.getExtension().toLowerCase());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
