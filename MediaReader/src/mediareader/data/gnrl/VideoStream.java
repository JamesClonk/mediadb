package mediareader.data.gnrl;

public class VideoStream extends AudioStream {

    private String width = null;
    private String height = null;
    private String framerate = null;

    public VideoStream(File file, MediaCodec codec, Language language) {
        super(file, codec, language);
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        if(width != null && width.equals("null")) {
            width = null;
        }
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        if(height != null && height.equals("null")) {
            height = null;
        }
        this.height = height;
    }

    public String getFramerate() {
        return framerate;
    }

    public void setFramerate(String framerate) {
        if(framerate != null && framerate.equals("null")) {
            framerate = null;
        }
        this.framerate = framerate;
    }
}
