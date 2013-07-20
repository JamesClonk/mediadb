package mediareader.data.gnrl;

public class AudioStream extends Stream {

    private String bitrate = null;

    public AudioStream(File file, MediaCodec codec, Language language) {
        super(file, codec, language);
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        if(bitrate != null && bitrate.equals("null")) {
            bitrate = null;
        }
        this.bitrate = bitrate;
    }
}
