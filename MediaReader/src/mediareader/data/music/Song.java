package mediareader.data.music;

import mediareader.data.gnrl.File;

public class Song {


    private String id = null;
    private String name = null;
    private String trackNumber = null;
    private String year = null;
    private String rating = null;
    private Album album = null;
    private Artist artist = null;
    private Genre genre = null;
    private File file = null;

    public Song(Album album, Artist artist, Genre genre, File file) {
        this.setAlbum(album);
        this.setArtist(artist);
        this.setGenre(genre);
        this.setFile(file);
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
        if(name == null || name.isEmpty()) {
            name = this.getFile().getFileName().substring(0, this.getFile().getFileName().lastIndexOf(this.getFile().getExtension())-1);
        }
        this.name = name;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        if(trackNumber != null && trackNumber.equals("null")) {
            trackNumber = null;
        }
        this.trackNumber = trackNumber;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        if(year != null && year.equals("null")) {
            year = null;
        }
        this.year = year;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}

