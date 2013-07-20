package mediareader;

import mediareader.data.gnrl.Drive;
import mediareader.data.gnrl.File;
import mediareader.data.gnrl.Path;
import mediareader.data.gnrl.Host;
import ch.jamesclonk.Cfg;
import ch.jamesclonk.Log;
import ch.jamesclonk.Db;
import ch.jamesclonk.Version;
import ch.jamesclonk.db.DbStatement;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.ICodec.Type;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import mediareader.data.gnrl.AudioStream;
import mediareader.data.gnrl.MediaCodec;
import mediareader.data.gnrl.Language;
import mediareader.data.gnrl.MediaFile;
import mediareader.data.gnrl.Picture;
import mediareader.data.gnrl.SubtitleStream;
import mediareader.data.gnrl.VideoStream;
import mediareader.data.music.Album;
import mediareader.data.music.Artist;
import mediareader.data.music.Genre;
import mediareader.data.music.Song;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.cmc.music.common.ID3ReadException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.*;
import org.sqlite.SQLiteJDBCLoader;

public class Main {

    private Cfg cfg = null;
    private Log log = null;
    private Db db = null;
    private DecimalFormat dfFrameRate = null;
    private int commitSize = 1;
    private int commitCounter = 0;

    public static void main(String[] args) {

        Main me = new Main();
        me.letsGo(args);
    }

    private void letsGo(String[] args) {
        this.initJcLibrary();

        try {
            try {
                if (cfg.getCfgValue("SHOW_DIALOG").equals("1")) {
                    final JFileChooser fc = new JFileChooser();
                    fc.setVisible(true);
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int retval = fc.showOpenDialog(null);

                    if (retval == JFileChooser.APPROVE_OPTION) {
                        java.io.File file = fc.getSelectedFile();
                        cfg.setCfgValue("PATH", file.getAbsolutePath());
                    } else {
                        log.error("File/Directory Dialog cancelled");
                    }
                }
            } catch (java.lang.InternalError ie) {
                log.warn("Can't open up an X-client session!");
            } catch (Exception ex) {
                log.warn("Can't open up an X-client session!");
            }

            this.parseParameters(args);

            this.commitSize = Integer.parseInt(cfg.getCfgValue("COMMITSIZE"));

            this.initDatabase();
            this.prepareDatabase();

            dfFrameRate = new DecimalFormat("##.##");
            dfFrameRate.setRoundingMode(RoundingMode.DOWN);

//            com.xuggle.xuggler.Configuration.printSupportedContainerFormats(System.out);
//            com.xuggle.xuggler.GetSupportedCodecs.main(args);
//            if(true) { return; }

            if (cfg.getCfgValue("MODE").equals("DELETE") || cfg.getCfgValue("MODE").equals("UPDATE")) {
                log.info("Let's compare db entries with given path and delete if necessary: " + cfg.getCfgValue("PATH"));
                this.parseDbEntriesforDelete(new java.io.File(cfg.getCfgValue("PATH")));
            }
            if (cfg.getCfgValue("MODE").equals("INSERT") || cfg.getCfgValue("MODE").equals("UPDATE")) {
                log.info("Let's scan this and all it's subdirectories for media files: " + cfg.getCfgValue("PATH"));
                this.goThroughDirectories(new java.io.File(cfg.getCfgValue("PATH")));
            }

            // write to disk
            this.commit();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.closeDatabase();
        }
    }

    private void goThroughDirectories(java.io.File startDir) throws Exception {
        java.io.File[] filesAndDirectories = startDir.listFiles(new ExcludeFileFilter(cfg));
        for (int f = 0; f < filesAndDirectories.length; f++) {
            if (filesAndDirectories[f].isDirectory()) {
                this.goThroughDirectories(filesAndDirectories[f]);
            } else if (filesAndDirectories[f].isFile()) {
                this.parseFileForInsert(new File(filesAndDirectories[f].getAbsolutePath()));
            }
            filesAndDirectories[f] = null;
        }
    }

    private void parseDbEntriesforDelete(java.io.File dir) throws SQLException, IOException, Exception {
        String mountpoint = dir.getAbsolutePath().substring(0, 2); // WIN32 solution
        String cleanPath = dir.getAbsolutePath().replaceAll(mountpoint, "").replaceAll("\\\\", "/");
        ArrayList<HashMap<String, String>> files = db.runQueryStatement("SELECT_ALL_FILES_BY_PATH_AND_DRIVE",
                mountpoint,
                cleanPath + "%");

        for (HashMap<String, String> file : files) {
            java.io.File testFile = new java.io.File(file.get("file"));
            if (!testFile.exists()) {
                log.info("Delete file entry from db: [" + file.get("file") + "]");
                db.runDmlStatement("DELETE_FROM_GNRL_FILE_BY_FILE_ID", file.get("file_id"));
            }

            // write to disk
            this.commitCounter++;
            if (this.commitCounter >= this.commitSize) {
                this.commit();
                this.commitCounter = 0;
            }
        }
    }

    private void parseFileForInsert(File file) throws SQLException, IOException, ID3ReadException, Exception {
        IMediaReader reader = null;
        IContainer container = null;
        try {
            reader = ToolFactory.makeReader(file.getAbsolutePath());
            reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
            //reader.addListener(ToolFactory.makeViewer(false));
            //while (reader.readPacket() == null);
            //reader.readPacket();
            reader.open();
            container = reader.getContainer();
            container.queryStreamMetaData();

        } catch (Exception ex) {
            // abort if file is not parsable by xuggler
            log.warn(file.getAbsolutePath() + " is not a readable media file for MediaReader! May be added to exclude list in configfile..");
            return;
        }

        log.info("parsing file: [" + file.getAbsolutePath() + "]");

        // add file to db
        file.setDuration(String.valueOf(container.getDuration()));
        boolean fileWasNewlyInserted = this.insertFile(file);

        // add host to db
        Host host = new Host(cfg.getCfgValue("LOCATION"));
        this.insertHost(host);

        // add drive to db
        Drive drive = new Drive(file, host);
        boolean driveWasNewlyInserted = this.insertDrive(drive);

        // add path to db
        Path path = new Path(file);
        boolean pathWasNewlyInserted = this.insertPath(path);

        if (fileWasNewlyInserted || driveWasNewlyInserted || pathWasNewlyInserted) {
            // add links to db
            this.insertLinkFilePath(file, path);
            this.insertLinkFileDrive(file, drive);
            this.insertLinkPathDrive(path, drive);
        }

        // if the file was already there it is pointless to do the whole media parsing again (the file didn't change after all!)
        if (fileWasNewlyInserted) {

            // the actual media metadata reading stuff starts below here..
            MediaFile mediaFile = new MediaFile(file);

            for (int s = 0; s < reader.getContainer().getNumStreams(); s++) {
                IStream stream = container.getStream(s);
                IStreamCoder sc = stream.getStreamCoder();

                if (sc != null) {
                    ICodec streamcodec = sc.getCodec();
                    Type codecType = sc.getCodecType();

                    if (codecType.equals(ICodec.Type.CODEC_TYPE_ATTACHMENT)
                            || codecType.equals(ICodec.Type.CODEC_TYPE_DATA)
                            || codecType.equals(ICodec.Type.CODEC_TYPE_UNKNOWN)) {
                        continue;
                    }

                    // add language do db
                    Language language = new Language(stream.getLanguage());
                    this.insertLanguage(language);

                    // add codec to db
                    MediaCodec mediaCodec = new MediaCodec();
                    mediaCodec.setCode(sc.getCodecID().toString());
                    if (streamcodec != null) {
                        mediaCodec.setName(streamcodec.getName());
                        mediaCodec.setDescription(streamcodec.getLongName());
                    }
                    this.insertCodec(mediaCodec);

                    String bitRate = String.valueOf(sc.getBitRate());
                    if (bitRate == null || bitRate.equals("")) {
                        bitRate = String.valueOf(container.getBitRate());
                    }

                    if (codecType.equals(ICodec.Type.CODEC_TYPE_VIDEO)) {
                        // add videostream to db
                        VideoStream videoStream = new VideoStream(file, mediaCodec, language);
                        videoStream.setStreamIndex(String.valueOf(stream.getIndex()));
                        videoStream.setBitrate(bitRate);
                        videoStream.setFramerate(dfFrameRate.format(sc.getFrameRate().getValue()));
                        videoStream.setWidth(String.valueOf(sc.getWidth()));
                        videoStream.setHeight(String.valueOf(sc.getHeight()));
                        this.insertVideoStream(videoStream);

                        mediaFile.addVideoStream(videoStream);

                        if (cfg.getCfgValue("STORE_PICTURES").equals("1")
                                && Integer.parseInt(cfg.getCfgValue("VIDEO_THUMBNAILS")) > 0) {
                            //create thumbnails
                            CreateThumbnails ct = new CreateThumbnails();
                            ct.setVideoStreamIndex(stream.getIndex());
                            ct.setSnapshots(Integer.parseInt(cfg.getCfgValue("VIDEO_THUMBNAILS")));
                            ct.setThumbnailWidth(Integer.parseInt(cfg.getCfgValue("VIDEO_THUMBNAIL_WIDTH")));
                            ct.createThumbnails(file.getAbsolutePath());

                            Iterator<Double> it = ct.getThumbnails().keySet().iterator();
                            while (it.hasNext()) {
                                Double key = it.next();
                                BufferedImage image = ct.getThumbnails().get(key);

                                Picture picture = new Picture();

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(image, "png", baos);
                                baos.flush();

                                picture.setImageData(baos.toByteArray());
                                picture.setMimeType("image/png");
                                picture.setName(key.toString());
                                this.insertPicture(picture);

                                // link file to picture
                                this.insertLinkFilePicture(file, picture);
                                
                                baos.close();
                            }
                        }

                    } else if (codecType.equals(ICodec.Type.CODEC_TYPE_AUDIO)) {
                        // add audiostream to db
                        AudioStream audioStream = new AudioStream(file, mediaCodec, language);
                        audioStream.setStreamIndex(String.valueOf(stream.getIndex()));
                        audioStream.setBitrate(bitRate);
                        this.insertAudioStream(audioStream);

                        mediaFile.addAudioStream(audioStream);

                        Artist artist = new Artist();
                        Genre genre = new Genre();
                        Album album = new Album();
                        Song song = new Song(album, artist, genre, file);

                        if (file.getExtension().equals("mp3") && sc.getCodecID().equals(ICodec.ID.CODEC_ID_MP3)) {
                            MusicMetadataSet src_set = new MyID3().read(file); // read metadata
                            IMusicMetadata metadata = src_set.getSimplified();

                            String pictureId = null;
                            if (cfg.getCfgValue("STORE_PICTURES").equals("1")) {
                                Vector pictures = metadata.getPictures();
                                if (pictures != null && !pictures.isEmpty()) {
                                    for (int i = 0; i < pictures.size(); i++) {
                                        Picture picture = new Picture();

                                        // add picture to db
                                        ImageData image = (ImageData) pictures.get(i);
                                        picture.setImageData(image.imageData);
                                        picture.setMimeType(image.mimeType);
                                        this.insertPicture(picture);

                                        pictureId = picture.getId();

                                        // link file to picture
                                        this.insertLinkFilePicture(file, picture);
                                    }
                                }
                            }

                            // add artist to db
                            artist.setName(metadata.getArtist());
                            this.insertArtist(artist);

                            // add music genre to db
                            genre.setName(metadata.getGenreName());
                            this.insertGenre(genre);

                            // add album to db
                            album.setName(metadata.getAlbum());
                            this.insertAlbum(album);

                            // add song to db
                            song.setName(metadata.getSongTitle());
                            song.setTrackNumber(String.valueOf(metadata.getTrackNumberNumeric()));
                            song.setYear(String.valueOf(metadata.getYear()));
                            this.insertSong(song);
                        }

                    } else if (codecType.equals(ICodec.Type.CODEC_TYPE_SUBTITLE)) {
                        // update codec information to db
                        mediaCodec.setName(sc.getCodecID().toString().substring(9).toLowerCase());
                        mediaCodec.setDescription("Subtitle");
                        this.updateCodec(mediaCodec);

                        // add subtitlestream to db
                        SubtitleStream subtitleStream = new SubtitleStream(file, mediaCodec, language);
                        subtitleStream.setStreamIndex(String.valueOf(stream.getIndex()));
                        this.insertSubtitleStream(subtitleStream);

                        mediaFile.addSubtitleStream(subtitleStream);
                    }

                    // release some stuff
                    mediaCodec = null;
                }
            }

            // flag "video" files as video..
            if (mediaFile.isVideo()) {
                this.updateFile(file);
            }
        }

        // write to disk
        this.commitCounter++;
        if (this.commitCounter >= this.commitSize) {
            this.commit();
            this.commitCounter = 0;
        }

        reader.close();
    }

    private String insertPicture(Picture picture) throws SQLException, Exception {
        // check for image data
        if (picture.getImageData() != null && picture.getImageData().length > 0) {
            ArrayList<HashMap<String, String>> pictureData = db.runQueryStatement("SELECT_FROM_GNRL_PICTURE", picture.getFileHash());

            if (pictureData == null || pictureData.isEmpty()) {
//                db.runDmlStatement("INSERT_INTO_GNRL_PICTURE",
//                        picture.getImageData(),
//                        picture.getMimeType(),
//                        picture.getName(),
//                        picture.getFileHash(),
//                        picture.getPath().getId());
                PreparedStatement ps = db.getStatement("INSERT_INTO_GNRL_PICTURE");

                ps.setBytes(1, picture.getImageData());
                ps.setString(2, picture.getMimeType());
                ps.setString(3, picture.getName());
                ps.setString(4, picture.getFileHash());
                ps.executeUpdate();

                pictureData.clear();
                pictureData = db.runQueryStatement("SELECT_FROM_GNRL_PICTURE", picture.getFileHash());
            }
            picture.setId(pictureData.get(0).get("id"));
        }

        return picture.getId();
    }

    private String insertArtist(Artist artist) throws SQLException, Exception {
        // check for artist
        if (artist.getName() != null && !artist.getName().isEmpty()) {
            ArrayList<HashMap<String, String>> artistData = db.runQueryStatement("SELECT_FROM_MUSIC_ARTIST", artist.getName());

            if (artistData == null || artistData.isEmpty()) {
                db.runDmlStatement("INSERT_INTO_MUSIC_ARTIST",
                        artist.getName());

                artistData.clear();
                artistData = db.runQueryStatement("SELECT_FROM_MUSIC_ARTIST", artist.getName());
            }
            artist.setId(artistData.get(0).get("id"));
        }

        return artist.getId();
    }

    private String insertGenre(Genre genre) throws SQLException, Exception {
        // check for artist
        if (genre.getName() != null && !genre.getName().isEmpty()) {
            ArrayList<HashMap<String, String>> genreData = db.runQueryStatement("SELECT_FROM_MUSIC_GENRE", genre.getName());

            if (genreData == null || genreData.isEmpty()) {
                db.runDmlStatement("INSERT_INTO_MUSIC_GENRE",
                        genre.getName());

                genreData.clear();
                genreData = db.runQueryStatement("SELECT_FROM_MUSIC_GENRE", genre.getName());
            }
            genre.setId(genreData.get(0).get("id"));
        }

        return genre.getId();
    }

    private String insertSong(Song song) throws SQLException, Exception {
        ArrayList<HashMap<String, String>> songData = db.runQueryStatement("SELECT_FROM_MUSIC_SONG",
                song.getName(),
                song.getAlbum().getId(),
                song.getArtist().getId());

        if (songData != null && !songData.isEmpty()) {
            song.setId(songData.get(0).get("id"));

        } else {
            db.runDmlStatement("INSERT_INTO_MUSIC_SONG",
                    song.getName(),
                    song.getAlbum().getId(),
                    song.getArtist().getId(),
                    song.getGenre().getId(),
                    song.getTrackNumber(),
                    song.getYear(),
                    song.getRating(),
                    song.getFile().getId());
            try {
                song.setId(db.runQueryStatement("SELECT_FROM_MUSIC_SONG",
                        song.getName(),
                        song.getAlbum().getId(),
                        song.getArtist().getId()).get(0).get("id"));
            } catch (Exception ex) {
                song.setId("-1");
            }
        }

        return song.getId();
    }

    private String insertAlbum(Album album) throws SQLException, Exception {
        if (album.getName() != null && !album.getName().isEmpty()) {
            ArrayList<HashMap<String, String>> albumData = db.runQueryStatement("SELECT_FROM_MUSIC_ALBUM",
                    album.getName());

            if (albumData != null && !albumData.isEmpty()) {
                album.setId(albumData.get(0).get("id"));

            } else {
                db.runDmlStatement("INSERT_INTO_MUSIC_ALBUM",
                        album.getName(),
                        album.getRating());

                album.setId(db.runQueryStatement("SELECT_FROM_MUSIC_ALBUM",
                        album.getName()).get(0).get("id"));
            }
        }

        return album.getId();
    }

    private void updateFile(File file) throws SQLException, Exception {
        db.runDmlStatement("UPDATE_GNRL_FILE_BY_ID",
                file.getFileName(),
                file.getFileSize(),
                file.getFileHash(),
                file.getNameCRC(),
                file.getFileType().getId(),
                file.getDuration(),
                String.valueOf(file.isVideo()),
                file.getModifiedDate(),
                file.getId());
    }

    private void updateCodec(MediaCodec codec) throws SQLException, Exception {
        db.runDmlStatement("UPDATE_GNRL_CODEC_BY_ID",
                codec.getName(),
                codec.getCode(),
                codec.getDescription(),
                codec.getId());
    }

    private String insertAudioStream(AudioStream audioStream) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_AUDIO_STREAM",
                audioStream.getFileId(),
                audioStream.getStreamIndex());

        if (data != null && !data.isEmpty()) {
            audioStream.setId(data.get(0).get("id"));

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_AUDIO_STREAM",
                    audioStream.getFileId(),
                    audioStream.getStreamIndex(),
                    audioStream.getCodec().getId(),
                    audioStream.getLanguage().getId(),
                    audioStream.getBitrate());

            audioStream.setId(db.runQueryStatement("SELECT_FROM_GNRL_AUDIO_STREAM",
                    audioStream.getFileId(),
                    audioStream.getStreamIndex()).get(0).get("id"));
        }

        return audioStream.getId();
    }

    private String insertVideoStream(VideoStream videoStream) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_VIDEO_STREAM",
                videoStream.getFileId(),
                videoStream.getStreamIndex());

        if (data != null && !data.isEmpty()) {
            videoStream.setId(data.get(0).get("id"));

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_VIDEO_STREAM",
                    videoStream.getFileId(),
                    videoStream.getStreamIndex(),
                    videoStream.getCodec().getId(),
                    videoStream.getLanguage().getId(),
                    videoStream.getBitrate(),
                    videoStream.getWidth(),
                    videoStream.getHeight(),
                    videoStream.getFramerate());

            videoStream.setId(db.runQueryStatement("SELECT_FROM_GNRL_VIDEO_STREAM",
                    videoStream.getFileId(),
                    videoStream.getStreamIndex()).get(0).get("id"));
        }

        return videoStream.getId();
    }

    private String insertSubtitleStream(SubtitleStream subtitleStream) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_SUBTITLE_STREAM",
                subtitleStream.getFileId(),
                subtitleStream.getStreamIndex());

        if (data != null && !data.isEmpty()) {
            subtitleStream.setId(data.get(0).get("id"));

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_SUBTITLE_STREAM",
                    subtitleStream.getFileId(),
                    subtitleStream.getStreamIndex(),
                    subtitleStream.getCodec().getId(),
                    subtitleStream.getLanguage().getId());

            subtitleStream.setId(db.runQueryStatement("SELECT_FROM_GNRL_SUBTITLE_STREAM",
                    subtitleStream.getFileId(),
                    subtitleStream.getStreamIndex()).get(0).get("id"));
        }

        return subtitleStream.getId();
    }

    private String insertCodec(MediaCodec codec) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_CODEC",
                codec.getCode());

        if (data != null && !data.isEmpty()) {
            codec.setId(data.get(0).get("id"));

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_CODEC",
                    codec.getName(),
                    codec.getCode(),
                    codec.getDescription());

            codec.setId(db.runQueryStatement("SELECT_FROM_GNRL_CODEC",
                    codec.getCode()).get(0).get("id"));
        }

        return codec.getId();
    }

    private void insertLinkFilePicture(File file, Picture picture) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_LINK_FILE_PICTURE",
                file.getId(),
                picture.getId());

        if (data == null || data.isEmpty()) {
            db.runDmlStatement("INSERT_INTO_GNRL_LINK_FILE_PICTURE",
                    file.getId(),
                    picture.getId());
        }
    }

    private void insertLinkFileDrive(File file, Drive drive) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_LINK_FILE_DRIVE",
                file.getId(),
                drive.getId());

        if (data == null || data.isEmpty()) {
            db.runDmlStatement("INSERT_INTO_GNRL_LINK_FILE_DRIVE",
                    file.getId(),
                    drive.getId());
        }
    }

    private void insertLinkFilePath(File file, Path path) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_LINK_FILE_PATH",
                file.getId(),
                path.getId());

        if (data == null || data.isEmpty()) {
            db.runDmlStatement("INSERT_INTO_GNRL_LINK_FILE_PATH",
                    file.getId(),
                    path.getId());
        }
    }

    private void insertLinkPathDrive(Path path, Drive drive) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_LINK_PATH_DRIVE",
                path.getId(),
                drive.getId());

        if (data == null || data.isEmpty()) {
            db.runDmlStatement("INSERT_INTO_GNRL_LINK_PATH_DRIVE",
                    path.getId(),
                    drive.getId());
        }
    }

    private boolean insertFile(File file) throws SQLException, Exception {
        // check for filetype first
        ArrayList<HashMap<String, String>> ftData = db.runQueryStatement("SELECT_FROM_GNRL_FILE_TYPE", file.getFileType().getExtension());

        if (ftData == null || ftData.isEmpty()) {
            db.runDmlStatement("INSERT_INTO_GNRL_FILE_TYPE",
                    file.getFileType().getExtension().toUpperCase(),
                    file.getFileType().getExtension(),
                    file.getFileType().getDescription());

            ftData.clear();
            ftData = db.runQueryStatement("SELECT_FROM_GNRL_FILE_TYPE", file.getFileType().getExtension());
        }

        file.getFileType().setId(ftData.get(0).get("id"));
        file.getFileType().setName(ftData.get(0).get("name"));
        file.getFileType().setDescription(ftData.get(0).get("description"));

        // do the file stuff now
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_FILE",
                file.getFileName(),
                file.getFileHash());

        if (data != null && !data.isEmpty()) {
            file.setId(data.get(0).get("id"));

            // update last modified date if different
            if (!file.getModifiedDate().equals(data.get(0).get("modified_date"))) {
                this.updateFile(file);
            }

            // return false if file was already there and didn't really change (md5 hash was still the same)
            return false;

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_FILE",
                    file.getFileName(),
                    file.getFileSize(),
                    file.getFileHash(),
                    file.getNameCRC(),
                    file.getFileType().getId(),
                    file.getDuration(),
                    String.valueOf(file.isVideo()),
                    file.getModifiedDate());

            file.setId(db.runQueryStatement("SELECT_FROM_GNRL_FILE",
                    file.getFileName(),
                    file.getFileHash()).get(0).get("id"));
        }

        return true;
    }

    private String insertLanguage(Language language) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_LANGUAGE",
                language.getIsocode());

        if (data != null && !data.isEmpty()) {
            language.setId(data.get(0).get("id"));

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_LANGUAGE",
                    language.getName(),
                    language.getIsocode());

            language.setId(db.runQueryStatement("SELECT_FROM_GNRL_LANGUAGE",
                    language.getIsocode()).get(0).get("id"));
        }

        return language.getId();
    }

    private String insertHost(Host host) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_HOST",
                host.getName(),
                host.getLocation());

        if (data != null && !data.isEmpty()) {
            host.setId(data.get(0).get("id"));
            db.runDmlStatement("UPDATE_GNRL_HOST_BY_NAME_AND_LOCATION",
                    host.getIp(),
                    host.getCpu(),
                    host.getRam(),
                    host.getOs(),
                    host.getName(),
                    host.getLocation());

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_HOST",
                    host.getName(),
                    host.getIp(),
                    host.getLocation(),
                    host.getCpu(),
                    host.getRam(),
                    host.getOs());

            host.setId(db.runQueryStatement("SELECT_FROM_GNRL_HOST",
                    host.getName(),
                    host.getLocation()).get(0).get("id"));
        }

        return host.getId();
    }

    private boolean insertPath(Path path) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_PATH",
                path.getPath());

        if (data != null && !data.isEmpty()) {
            path.setId(data.get(0).get("id"));

            // return false if path was already there
            return false;

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_PATH",
                    path.getPath(),
                    path.getCRC());

            path.setId(db.runQueryStatement("SELECT_FROM_GNRL_PATH",
                    path.getPath()).get(0).get("id"));
        }

        return true;
    }

    private boolean insertDrive(Drive drive) throws SQLException {
        ArrayList<HashMap<String, String>> data = db.runQueryStatement("SELECT_FROM_GNRL_DRIVE",
                drive.getUuid());

        if (data != null && !data.isEmpty()) {
            drive.setId(data.get(0).get("id"));
            db.runDmlStatement("UPDATE_GNRL_DRIVE_BY_UUID",
                    drive.getName(),
                    drive.getLocation(),
                    drive.getMountPoint(),
                    drive.getPartition(),
                    drive.getTotalSpace(),
                    drive.getFreeSpace(),
                    drive.getHostId(),
                    drive.getUuid());

            // return false if drive was already there
            return false;

        } else {
            db.runDmlStatement("INSERT_INTO_GNRL_DRIVE",
                    drive.getName(),
                    drive.getUuid(),
                    drive.getLocation(),
                    drive.getMountPoint(),
                    drive.getPartition(),
                    drive.getTotalSpace(),
                    drive.getFreeSpace(),
                    drive.getHostId());

            drive.setId(db.runQueryStatement("SELECT_FROM_GNRL_DRIVE",
                    drive.getUuid()).get(0).get("id"));
        }

        return true;
    }

    private void parseParameters(String[] args) throws ParseException, Exception {
        // setup commandline options
        Options opt = new Options();
        opt.addOption("l", "location", true, "current location. Defaults to Bern");
        opt.addOption("m", "mode", true, "which mode to run: INSERT, UPDATE or DELETE. Defaults to INSERT");
        opt.addOption("p", "path", true, "which path to scan for files.");
        opt.addOption("h", "help", false, "displays this help message");

        // parse commandline options
        CommandLine cmd = null;
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLineParser parser = new PosixParser();
            cmd = parser.parse(opt, args);
        } catch (UnrecognizedOptionException uoex) {
            // automatically generate & print the help statement
            formatter.printHelp("MediaReader", opt, true);
            throw new Exception("Help!");
        }

        if (cmd.hasOption("h")) {
            formatter.printHelp("MediaReader", opt, true);
            throw new Exception("Help!");
        }

        if (cmd.hasOption("l")) {
            cfg.setCfgValue("LOCATION", cmd.getOptionValue("l"));
        } else {
            log.info("missing option: -l");
            log.info("will assume default location: Bern");
            cfg.setCfgValue("LOCATION", "Bern");
        }

        if (cmd.hasOption("m")) {
            cfg.setCfgValue("MODE", cmd.getOptionValue("m").toUpperCase());
        } else {
            log.info("missing option: -m");
            log.info("will assume default mode: INSERT");
            cfg.setCfgValue("MODE", "INSERT");
        }
        if (!cfg.getCfgValue("MODE").matches("(INSERT|UPDATE|DELETE)")) {
            log.error("Invalid mode: " + cfg.getCfgValue("MODE"));
            throw new Exception("Invalid mode: " + cfg.getCfgValue("MODE"));
        }

        // overwrite only if path is not already set by filechooser
        if (cfg.getCfgValue("PATH").equals("")) {
            if (cmd.hasOption("p")) {
                cfg.setCfgValue("PATH", cmd.getOptionValue("p"));
            } else {
                log.error("missing option: -p");
                throw new Exception("missing option: -p");
            }
        }
    }

    private void prepareDatabase() throws IOException, SQLException, Exception {
        // create table structure if not exists already
        db.prepareStatements(cfg.getCfgValue("CREATE_TABLE_STATEMENTS"));
        DbStatement[] statements = db.getDbStatements();
        for (DbStatement statement : statements) {
            db.runDmlStatement(statement.getStatementName());
        }

        // create indexes if not exists already
        db.prepareStatements(cfg.getCfgValue("CREATE_INDEX_STATEMENTS"));
        statements = db.getDbStatements();
        for (DbStatement statement : statements) {
            if (statement.getStatementName().startsWith("CREATE_INDEX_")) {
                db.runDmlStatement(statement.getStatementName());
            }
        }

        this.commit();

        // prepare the rest
        db.prepareStatements(cfg.getCfgValue("INSERT_STATEMENTS"));
        db.prepareStatements(cfg.getCfgValue("DELETE_STATEMENTS"));
    }

    private void commit() throws SQLException {
        // commit manually only if autocommit is off
        if (!db.getConnection("MEDIADB").getAutoCommit()) {
            db.commit("MEDIADB");
        }
    }

    private void rollback() throws SQLException {
        // rollback manually only if autocommit is off
        if (!db.getConnection("MEDIADB").getAutoCommit()) {
            db.rollback("MEDIADB");
        }
    }

    private void initDatabase() {
        try {
            log.info(String.format("sqlite jdbc driver is running in %s mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java"));

            db = new Db(cfg, log);
            db.initConnections();

            // enforce foreign-key relationships
            db.getConnection("MEDIADB").prepareStatement("PRAGMA foreign_keys = ON;").execute();

            // turn off autocommit
            db.getConnection("MEDIADB").setAutoCommit(false);

        } catch (Exception ex) {
            log.fatal(ex);
        }
    }

    private void closeDatabase() {
        try {
            // analyze all tables & indexes before closing db
            db.getConnection("MEDIADB").prepareStatement("ANALYZE;").execute();
            this.commit();

            db.disconnectAll();

        } catch (Exception ex) {
            log.fatal(ex);
        }
    }

    private void initJcLibrary() {
        try {
            if (!Version.isSuitable(3, 11, 1)) {
                throw new Exception("Requirements check for JamesClonk Library version failed!");
            }

            cfg = new Cfg("cfg/MediaReader.cfg");
            log = cfg.getLog();

        } catch (Exception ex) {
            System.err.println("Could not properly initialize the JamesClonk library!");
            System.err.println(ex);
            System.exit(-1);
        }
    }
}
