package mediareader.data.gnrl;

import java.util.ArrayList;

public class MediaFile {

    private File file = null;
    private ArrayList<AudioStream> audioStreams = null;
    private ArrayList<VideoStream> videoStreams = null;
    private ArrayList<SubtitleStream> subtitleStreams = null;
    private boolean isVideo = false;

    public MediaFile(File file) {
        this.setFile(file);
        this.audioStreams = new ArrayList<AudioStream>();
        this.videoStreams = new ArrayList<VideoStream>();
        this.subtitleStreams = new ArrayList<SubtitleStream>();
    }

    public void addAudioStream(AudioStream audioStream) {
        this.audioStreams.add(audioStream);
    }

    public void addVideoStream(VideoStream videoStreams) {
        this.videoStreams.add(videoStreams);
        this.isVideo = true;
        file.setVideo(true);
    }

    public void addSubtitleStream(SubtitleStream subtitleStreams) {
        this.subtitleStreams.add(subtitleStreams);
        this.isVideo = true;
        file.setVideo(true);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ArrayList<AudioStream> getAudioStreams() {
        return this.audioStreams;
    }

    public ArrayList<VideoStream> getVideoStreams() {
        return this.videoStreams;
    }

    public ArrayList<SubtitleStream> getSubtitleStreams() {
        return this.subtitleStreams;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }
}
