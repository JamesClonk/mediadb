package mediareader.data.gnrl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import mediareader.md5Checksum;

public class File extends java.io.File {

    private String id = null;
    private boolean isVideo = false;
    private String mountPoint = null;
    private String extension = null;
    private String fileHash = null;
    private String nameCRC = null;
    private String fileSize = null;
    private String duration = null;
    private String modifiedDate = null;
    private FileType fileType = null;

    public File(String file) throws Exception {
        super(file);
        this.setFileHash(md5Checksum.getChecksum(this.getAbsolutePath()));
        this.setMountPoint(this.getAbsolutePath().substring(0, 2)); // WIN32 solution
        this.setExtension(this.getName().substring(this.getName().lastIndexOf(".") + 1));
        this.setFileSize(String.valueOf(this.length()));

	Date date = new Date(this.lastModified());
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        this.setModifiedDate(sdf.format(date));

        CRC32 crc = new CRC32();
        crc.update(this.getFileName().getBytes());
        this.setNameCRC(String.valueOf(crc.getValue()));

        this.setFileType(new FileType(this));
    }

    public String getFileName() {
        return this.getName();
    }

    public String getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileHash() throws Exception {
        return this.fileHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getNameCRC() {
        return nameCRC;
    }

    public void setNameCRC(String nameCRC) {
        this.nameCRC = nameCRC;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
