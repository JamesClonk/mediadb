package mediareader.data.gnrl;

import java.util.zip.CRC32;

public class Path {

    private String id = null;
    private String path = null;
    private String crc = null;

    public Path(File file) {
        // WIN32: remove drive letter and swap '\' with '/'
        String cleanPath = file.getParent().replaceAll(file.getMountPoint(), "").replaceAll("\\\\", "/");
        this.setPath(cleanPath);
        
        CRC32 crc = new CRC32();
        crc.update(this.getPath().getBytes());
        this.setCRC(String.valueOf(crc.getValue()));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCRC() {
        return crc;
    }

    public void setCRC(String hash) {
        this.crc = hash;
    }
}
