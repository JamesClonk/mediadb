package mediareader.data.gnrl;

import mediareader.md5Checksum;

public class Picture {

    private String id = null;
    private String name = null;
    private String fileHash = null;
    private byte[] imageData = null;
    private String mimeType = null;

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

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) throws Exception {
        this.imageData = imageData;
        this.setFileHash(md5Checksum.getChecksum(imageData));
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
