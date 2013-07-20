package mediareader.data.gnrl;

public class Stream {

    private String id = null;
    private String fileId = null;
    private String streamIndex = null;
    private MediaCodec codec = null;
    private Language language = null;

    public Stream(File file, MediaCodec codec, Language language) {
        this.setFileId(file.getId());
        this.setCodec(codec);
        this.setLanguage(language);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStreamIndex() {
        return streamIndex;
    }

    public void setStreamIndex(String streamIndex) {
        this.streamIndex = streamIndex;
    }

    public MediaCodec getCodec() {
        return codec;
    }

    public void setCodec(MediaCodec codec) {
        this.codec = codec;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

}
