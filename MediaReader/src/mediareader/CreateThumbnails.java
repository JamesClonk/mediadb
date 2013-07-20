package mediareader;

import java.awt.image.BufferedImage;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IContainer;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;

public class CreateThumbnails extends MediaListenerAdapter {

    public static final double SECONDS_BETWEEN_FRAMES = 15;
    public static final long MICRO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
    private int videoStreamIndex = -1;
    private boolean fileWritten = false;
    private String filename = null;
    private int snapshots = 4;
    private int thumbnailWidth = 250;
    private LinkedHashMap<Double, BufferedImage> thumbnails = new LinkedHashMap<Double, BufferedImage>();

    public int getVideoStreamIndex() {
        return videoStreamIndex;
    }

    public void setVideoStreamIndex(int videoStreamIndex) {
        this.videoStreamIndex = videoStreamIndex;
    }

    public void createThumbnails(String filename) {
        try {
            this.setFilename(filename);
            IMediaReader reader = ToolFactory.makeReader(filename);

            reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
            reader.addListener(this);

            reader.open();
            long numFrames = reader.getContainer().getStream(getVideoStreamIndex()).getNumFrames();
            int numIndexEntries = reader.getContainer().getStream(getVideoStreamIndex()).getNumIndexEntries();
            reader.close();

            if (numIndexEntries > (this.getSnapshots() + 1)) {
                int sliceSize = numIndexEntries / (this.getSnapshots() + 1);
                for (int i = 1; i <= this.getSnapshots(); i++) {
                    reader.open();
                    long timeStamp = reader.getContainer().getStream(getVideoStreamIndex()).getIndexEntry((i * sliceSize)).getTimeStamp();
                    reader.getContainer().seekKeyFrame(getVideoStreamIndex(), timeStamp - 1000, timeStamp, timeStamp + 1000, IContainer.SEEK_FLAG_FRAME);
                    this.fileWritten = false;
                    while (!this.fileWritten) {
                        reader.readPacket();
                    }
                    reader.close();
                }
            } else {
                reader.open();
                long timeStamp = reader.getContainer().getStream(getVideoStreamIndex()).getIndexEntry(1).getTimeStamp();
                reader.getContainer().seekKeyFrame(getVideoStreamIndex(), timeStamp - 100, timeStamp, timeStamp + 100, IContainer.SEEK_FLAG_FRAME);
                this.fileWritten = false;
                while (!this.fileWritten) {
                    reader.readPacket();
                }
                reader.close();
            }
        } catch (Exception ex) {
            // do nothing
        }
    }

    public void onVideoPicture(IVideoPictureEvent event) {
        try {
            if (event.getStreamIndex() != getVideoStreamIndex()) {
                return;
            }

            double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;

            // resize image to thumbnail size
            double w = event.getImage().getWidth();
            double h = event.getImage().getHeight();
            double newW = this.getThumbnailWidth();
            double newH = (newW / w) * h;
            BufferedImage resizedImage = new BufferedImage((int) newW, (int) newH, event.getImage().getType());
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(event.getImage(), 0, 0, (int) newW, (int) newH, 0, 0, (int) w, (int) h, null);
            g.dispose();

            // store PNG
            this.getThumbnails().put(seconds, resizedImage);
            this.fileWritten = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilename() {
        return filename;
    }

    private void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(int snapshots) {
        this.snapshots = snapshots;
    }

    public LinkedHashMap<Double, BufferedImage> getThumbnails() {
        return thumbnails;
    }

    private int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }
}
