<?php

if (!defined('BASEPATH')) {
    exit('No direct script access allowed');
}

class MediaDB extends CI_Model {
    
    private $CI = null;

    function __construct() {
        parent::__construct();
        
        $this->CI =& get_instance();
        if(isset($this->CI->pdo_db)) {
            $this->prepare_all_statements();
        } else {
            show_error('Pdo_db library not loaded yet.');
        }
    }

    function prepare_all_statements() {
        $this->CI->pdo_db->prepare_statement("all_files", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash_value
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)");

        $this->CI->pdo_db->prepare_statement("all_music_files", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.file_hash as hash_value
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.is_video = 'false'");

        $this->CI->pdo_db->prepare_statement("all_video_files", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.file_hash as hash_value
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.is_video = 'true'");

        $this->CI->pdo_db->prepare_statement("all_duplicate_files_by_hash", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.file_hash in (select file_hash from gnrl_file group by file_hash having count(*) > 1)
order by f.file_hash");

        $this->CI->pdo_db->prepare_statement("all_duplicate_files_by_drive_and_path", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.id in ( select file_id from gnrl_link_file_drive group by file_id having count(*) > 1
                union
                select file_id from gnrl_link_file_path group by file_id having count(*) > 1)
order by f.id");

        $this->CI->pdo_db->prepare_statement("all_duplicate_files_by_drive", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.id in (select file_id from gnrl_link_file_drive group by file_id having count(*) > 1)
order by f.id");

        $this->CI->pdo_db->prepare_statement("all_duplicate_files_by_path", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.id in (select file_id from gnrl_link_file_path group by file_id having count(*) > 1)
order by f.id");

        $this->CI->pdo_db->prepare_statement("all_drives", "select 
    d.id as drive_id,
    '(' || d.mountpoint || ') ' || d.name as drive,
    round(round(d.total_space) / 1024 / 1024 / 1024,1) || ' GB' as total,
    round(round(d.total_space - d.free_space) / 1024 / 1024 / 1024,1) || ' GB' as used,
    round(round(d.free_space) / 1024 / 1024 / 1024,1) || ' GB'  as free,
    round(
        round(round(d.total_space - d.free_space) / 1024 / 1024 / 1024,1) 
        / round(round(d.total_space) / 1024 / 1024 / 1024,1) * 100,1
        ) || '%' as usage,
    d.uuid as UUID,
    d.location as location,
    h.id as host_id,
    h.name as host_name,
    h.ip as host_ip,
    h.os as host_system
from gnrl_drive d
    join gnrl_host h on (d.host_id = h.id)");

        $this->CI->pdo_db->prepare_statement("all_music", "select
    s.id as song_id,
    s.name as song,
    a.id as album_id,
    a.name as album,
    r.id as artist_id,
    r.name as artist,
    g.name as genre,
    s.track as \"track#\",
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as length,
    (st.bitrate / 1000) || 'kbps' as bitrate,
    s.year as year,
    s.rating as rating
from music_song s
    join gnrl_file f on (f.id = s.file_id)
    left outer join music_album a on (s.music_album_id = a.id)
    left outer join music_artist r on (s.music_artist_id = r.id)
    left outer join music_genre g on (s.music_genre_id = g.id)
    left outer join gnrl_audio_stream st on (f.id = st.file_id and st.stream_index = 0)
order by s.name asc");

        $this->CI->pdo_db->prepare_statement("all_albums", "select
    a.id as album_id,
    a.name as name,
    (select count(*) from music_song where music_album_id = a.id) as tracks,
    (select strftime('%H:%M:%S',datetime((sum(f.duration) / 1000 / 1000), 'unixepoch')) 
        from music_song s join gnrl_file f on (f.id = s.file_id) where music_album_id = a.id
        ) as total_length,
    (select count(distinct music_artist_id) from music_song where music_album_id = a.id) as artists,
    (select count(distinct music_genre_id) from music_song where music_album_id = a.id) as genres,
    (select max(year) from music_song where music_album_id = a.id) as year,
    a.rating as rating
from music_album a
order by a.name asc");

        $this->CI->pdo_db->prepare_statement("all_artists", "select
    a.id as artist_id,
    a.name as name,
    (select count(*) from music_song where music_artist_id = a.id) as songs,
    (select count(distinct music_album_id) from music_song where music_artist_id = a.id) as albums,
    (select count(distinct music_genre_id) from music_song where music_artist_id = a.id) as genres
from music_artist a
order by a.name asc");

        $this->CI->pdo_db->prepare_statement("all_genres", "select
    g.id as genre_id,
    g.name as name,
    (select count(*) from music_song where music_genre_id = g.id) as songs,
    (select count(distinct music_album_id) from music_song where music_genre_id = g.id) as albums,
    (select count(distinct music_artist_id) from music_song where music_genre_id = g.id) as artists
from music_genre g
order by g.name asc");
        
        $this->CI->pdo_db->prepare_statement("artist_by_artist_id", "select
    a.id as artist_id,
    a.name as name,
    (select count(*) from music_song where music_artist_id = a.id) as songs,
    (select count(distinct music_album_id) from music_song where music_artist_id = a.id) as albums,
    (select count(distinct music_genre_id) from music_song where music_artist_id = a.id) as genres
from music_artist a
where a.id = :ARTIST_ID");
        
        $this->CI->pdo_db->prepare_statement("albums_by_artist_id", "select
    distinct
    a.id as album_id,
    a.name as name,
    (select count(*) from music_song where music_album_id = a.id and music_artist_id = r.id) as tracks_by_this_artist,
    (select count(*) from music_song where music_album_id = a.id) as tracks_in_total,
    (select strftime('%H:%M:%S',datetime((sum(f.duration) / 1000 / 1000), 'unixepoch')) 
        from music_song s join gnrl_file f on (f.id = s.file_id) where music_album_id = a.id
        ) as total_length,
    (select count(distinct music_artist_id) from music_song where music_album_id = a.id) as artists,
    (select count(distinct music_genre_id) from music_song where music_album_id = a.id) as genres,
    (select max(year) from music_song where music_album_id = a.id) as year,
    a.rating as rating
from music_artist r
    join music_song s on (r.id = s.music_artist_id)
    join music_album a on (s.music_album_id = a.id)
where r.id = :ARTIST_ID");
        
        $this->CI->pdo_db->prepare_statement("genres_by_artist_id", "select
    distinct
    g.id as genre_id,
    g.name as name,
    (select count(*) from music_song where music_genre_id = g.id and music_artist_id = r.id) as songs_by_this_artist,
    (select count(*) from music_song where music_genre_id = g.id) as songs_in_total,
    (select count(distinct music_album_id) from music_song where music_genre_id = g.id) as albums,
    (select count(distinct music_artist_id) from music_song where music_genre_id = g.id) as artists
from music_artist r
    join music_song s on (r.id = s.music_artist_id)
    join music_genre g on (s.music_genre_id = g.id)
where r.id = :ARTIST_ID
order by g.name asc");
        
        $this->CI->pdo_db->prepare_statement("songs_by_artist_id", "select
    distinct
    s.id as song_id,
    s.name as song,
    a.name as album,
    g.name as genre,
    s.track as \"track#\",
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as length,
    (st.bitrate / 1000) || 'kbps' as bitrate,
    s.year as year,
    s.rating as rating
from music_artist r
    join music_song s on (r.id = s.music_artist_id)
    join gnrl_file f on (f.id = s.file_id)
    left outer join music_album a on (s.music_album_id = a.id)
    left outer join music_genre g on (s.music_genre_id = g.id)
    left outer join gnrl_audio_stream st on (f.id = st.file_id and st.stream_index = 0)
where r.id = :ARTIST_ID
order by s.track asc, s.name asc");
        
        $this->CI->pdo_db->prepare_statement("album_by_album_id", "select
    a.id as album_id,
    a.name as name,
    (select count(*) from music_song where music_album_id = a.id) as tracks,
    (select strftime('%H:%M:%S',datetime((sum(f.duration) / 1000 / 1000), 'unixepoch')) 
        from music_song s join gnrl_file f on (f.id = s.file_id) where music_album_id = a.id
        ) as total_length,
    (select count(distinct music_artist_id) from music_song where music_album_id = a.id) as artists,
    (select count(distinct music_genre_id) from music_song where music_album_id = a.id) as genres,
    (select max(year) from music_song where music_album_id = a.id) as year,
    a.rating as rating
from music_album a
where a.id = :ALBUM_ID");
        
        $this->CI->pdo_db->prepare_statement("pictures_by_album_id", "select
    distinct
    p.id as image_id,
    p.image_data as image_data,
    p.mime_type as mime_type
from music_album a
    join music_song s on (a.id = s.music_album_id)
    join gnrl_file f on (s.file_id = f.id)
    join gnrl_link_file_picture lfp on (f.id = lfp.file_id)
    join gnrl_picture p on (lfp.picture_id = p.id)
where a.id = :ALBUM_ID");
        
        $this->CI->pdo_db->prepare_statement("artists_by_album_id", "select
    distinct
    r.id as artist_id,
    r.name as name,
    (select count(*) from music_song where music_artist_id = r.id and music_album_id = a.id) as songs_in_this_album,
    (select count(*) from music_song where music_artist_id = r.id) as songs_in_total,
    (select count(distinct music_album_id) from music_song where music_artist_id = r.id) as albums,
    (select count(distinct music_genre_id) from music_song where music_artist_id = r.id) as genres
from music_album a
    join music_song s on (a.id = s.music_album_id)
    join music_artist r on (s.music_artist_id = r.id)
where a.id = :ALBUM_ID
order by r.name asc");
        
        $this->CI->pdo_db->prepare_statement("genres_by_album_id", "select
    distinct
    g.id as genre_id,
    g.name as name,
    (select count(*) from music_song where music_genre_id = g.id and music_album_id = a.id) as songs_in_this_album,
    (select count(*) from music_song where music_genre_id = g.id) as songs_in_total,
    (select count(distinct music_album_id) from music_song where music_genre_id = g.id) as albums,
    (select count(distinct music_artist_id) from music_song where music_genre_id = g.id) as artists
from music_album a
    join music_song s on (a.id = s.music_album_id)
    join music_genre g on (s.music_genre_id = g.id)
where a.id = :ALBUM_ID
order by g.name asc");
        
        $this->CI->pdo_db->prepare_statement("songs_by_album_id", "select
    distinct
    s.id as song_id,
    s.name as song,
    r.name as artist,
    g.name as genre,
    s.track as \"track#\",
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as length,
    (st.bitrate / 1000) || 'kbps' as bitrate,
    s.year as year,
    s.rating as rating
from music_album a
    join music_song s on (a.id = s.music_album_id)
    join gnrl_file f on (f.id = s.file_id)
    left outer join music_artist r on (s.music_artist_id = r.id)
    left outer join music_genre g on (s.music_genre_id = g.id)
    left outer join gnrl_audio_stream st on (f.id = st.file_id and st.stream_index = 0)
where a.id = :ALBUM_ID
order by s.track asc, s.name asc");

        $this->CI->pdo_db->prepare_statement("file_by_file_id", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash_value
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where f.id = :FILE_ID");
        
        $this->CI->pdo_db->prepare_statement("song_by_file_id", "select
    s.id as song_id,
    s.name as song,
    a.id as album_id,
    a.name as album,
    r.id as artist_id,
    r.name as artist,
    g.name as genre,
    s.track as \"track#\",
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as length,
    (st.bitrate / 1000) || 'kbps' as bitrate,
    s.year as year,
    s.rating as rating
from music_song s
    join gnrl_file f on (f.id = s.file_id)
    left outer join music_album a on (s.music_album_id = a.id)
    left outer join music_artist r on (s.music_artist_id = r.id)
    left outer join music_genre g on (s.music_genre_id = g.id)
    left outer join gnrl_audio_stream st on (f.id = st.file_id and st.stream_index = 0)
where s.file_id = :FILE_ID");

        $this->CI->pdo_db->prepare_statement("audio_streams_by_file_id", "select
    f.id as file_id,
    st.stream_index as \"stream #\",
    c.name as codec,
    c.description as codec_description,
    l.isocode as language,
    (st.bitrate / 1000) || 'kbps' as bitrate
from gnrl_file f
    join gnrl_audio_stream st on (f.id = st.file_id)
    join gnrl_codec c on (st.codec_id = c.id)
    join gnrl_language l on (st.language_id = l.id)
where f.id = :FILE_ID");

        $this->CI->pdo_db->prepare_statement("video_streams_by_file_id", "select
    f.id as file_id,
    st.stream_index as \"stream #\",
    c.name as codec,
    c.description as codec_description,
    l.isocode as language,
    (st.bitrate / 1000) || 'kbps' as bitrate,
    st.width as width,
    st.height as height,
    st.framerate as framerate
from gnrl_file f
    join gnrl_video_stream st on (f.id = st.file_id)
    join gnrl_codec c on (st.codec_id = c.id)
    join gnrl_language l on (st.language_id = l.id)
where f.id = :FILE_ID");

        $this->CI->pdo_db->prepare_statement("subtitle_streams_by_file_id", "select
    f.id as file_id,
    st.stream_index as \"stream #\",
    c.name as codec,
    c.description as codec_description,
    l.isocode as language
from gnrl_file f
    join gnrl_subtitle_stream st on (f.id = st.file_id)
    join gnrl_codec c on (st.codec_id = c.id)
    join gnrl_language l on (st.language_id = l.id)
where f.id = :FILE_ID");

        $this->CI->pdo_db->prepare_statement("pictures_by_file_id", "select
    f.id as file_id,
    p.id as image_id,
    p.image_data as image_data,
    p.mime_type as mime_type
from gnrl_file f
    join gnrl_link_file_picture lfp on (f.id = lfp.file_id)
    join gnrl_picture p on (lfp.picture_id = p.id)
where f.id = :FILE_ID");

        $this->CI->pdo_db->prepare_statement("picture_by_picture_id", "select
    p.id as image_id,
    p.image_data as image_data,
    p.mime_type as mime_type
from gnrl_picture p
where p.id = :PICTURE_ID");
        
        $this->CI->pdo_db->prepare_statement("files_by_search", "select
    f.id as file_id,
    d.mountpoint || p.path || '/' || f.name as filename,
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as duration,
    round(round(f.size) / 1024 / 1024,2) || ' MB' as size,
    f.modified_date as modified_date,
    f.is_video as video,
    f.file_hash as hash_value
from gnrl_file f
    join gnrl_link_file_path lfp on (f.id = lfp.file_id)
    join gnrl_link_file_drive lfd on (f.id = lfd.file_id)
    join gnrl_path p on (lfp.path_id = p.id)
    join gnrl_drive d on (lfd.drive_id = d.id)
where d.mountpoint || p.path || '/' || f.name like :SEARCH");
        
        $this->CI->pdo_db->prepare_statement("songs_by_search", "select
    s.id as song_id,
    s.name as song,
    a.id as album_id,
    a.name as album,
    r.id as artist_id,
    r.name as artist,
    g.name as genre,
    s.track as \"track#\",
    strftime('%H:%M:%S',datetime((f.duration / 1000 / 1000), 'unixepoch')) as length,
    (st.bitrate / 1000) || 'kbps' as bitrate,
    s.year as year,
    s.rating as rating
from music_song s
    join gnrl_file f on (f.id = s.file_id)
    left outer join music_album a on (s.music_album_id = a.id)
    left outer join music_artist r on (s.music_artist_id = r.id)
    left outer join music_genre g on (s.music_genre_id = g.id)
    left outer join gnrl_audio_stream st on (f.id = st.file_id and st.stream_index = 0)
where s.name like :SEARCH");
        
        $this->CI->pdo_db->prepare_statement("albums_by_search", "select
    a.id as album_id,
    a.name as name,
    (select count(*) from music_song where music_album_id = a.id) as tracks,
    (select strftime('%H:%M:%S',datetime((sum(f.duration) / 1000 / 1000), 'unixepoch')) 
        from music_song s join gnrl_file f on (f.id = s.file_id) where music_album_id = a.id
        ) as total_length,
    (select count(distinct music_artist_id) from music_song where music_album_id = a.id) as artists,
    (select count(distinct music_genre_id) from music_song where music_album_id = a.id) as genres,
    (select max(year) from music_song where music_album_id = a.id) as year,
    a.rating as rating
from music_album a
where a.name like :SEARCH
order by a.name asc");

        $this->CI->pdo_db->prepare_statement("artists_by_search", "select
    a.id as artist_id,
    a.name as name,
    (select count(*) from music_song where music_artist_id = a.id) as songs,
    (select count(distinct music_album_id) from music_song where music_artist_id = a.id) as albums,
    (select count(distinct music_genre_id) from music_song where music_artist_id = a.id) as genres
from music_artist a
where a.name like :SEARCH
order by a.name asc");

        $this->CI->pdo_db->prepare_statement("genres_by_search", "select
    g.id as genre_id,
    g.name as name,
    (select count(*) from music_song where music_genre_id = g.id) as songs,
    (select count(distinct music_album_id) from music_song where music_genre_id = g.id) as albums,
    (select count(distinct music_artist_id) from music_song where music_genre_id = g.id) as artists
from music_genre g
where g.name like :SEARCH
order by g.name asc");
        
        $this->CI->pdo_db->prepare_statement("file_id_by_song_id", "select
    s.file_id as file_id
from music_song s
where s.id = :SONG_ID");
        
        $this->CI->pdo_db->prepare_statement("file_id_by_video_id", "select
    v.file_id as file_id
from video_video v
where v.id = :VIDEO_ID");

        $this->CI->pdo_db->prepare_statement("gnrl_file", "select * 
from gnrl_file");

        $this->CI->pdo_db->prepare_statement("gnrl_file_type", "select * 
from gnrl_file_type");

        $this->CI->pdo_db->prepare_statement("gnrl_path", "select * 
from gnrl_path");

        $this->CI->pdo_db->prepare_statement("gnrl_drive", "select * 
from gnrl_drive");

        $this->CI->pdo_db->prepare_statement("gnrl_host", "select * 
from gnrl_host");

        $this->CI->pdo_db->prepare_statement("gnrl_language", "select * 
from gnrl_language");

        $this->CI->pdo_db->prepare_statement("gnrl_picture", "select id, '[BLOB]' as image_data, mime_type, name, file_hash 
from gnrl_picture");

        $this->CI->pdo_db->prepare_statement("gnrl_codec", "select * 
from gnrl_codec");

        $this->CI->pdo_db->prepare_statement("gnrl_audio_stream", "select * 
from gnrl_audio_stream");

        $this->CI->pdo_db->prepare_statement("gnrl_video_stream", "select * 
from gnrl_video_stream");

        $this->CI->pdo_db->prepare_statement("gnrl_subtitle_stream", "select * 
from gnrl_subtitle_stream");

        $this->CI->pdo_db->prepare_statement("gnrl_link_file_drive", "select * 
from gnrl_link_file_drive");

        $this->CI->pdo_db->prepare_statement("gnrl_link_file_path", "select * 
from gnrl_link_file_path");

        $this->CI->pdo_db->prepare_statement("gnrl_link_file_picture", "select * 
from gnrl_link_file_picture");

        $this->CI->pdo_db->prepare_statement("gnrl_link_path_drive", "select * 
from gnrl_link_path_drive");

        $this->CI->pdo_db->prepare_statement("music_album", "select * 
from music_album");

        $this->CI->pdo_db->prepare_statement("music_artist", "select * 
from music_artist");

        $this->CI->pdo_db->prepare_statement("music_genre", "select * 
from music_genre");

        $this->CI->pdo_db->prepare_statement("music_song", "select * 
from music_song");

        $this->CI->pdo_db->prepare_statement("video_genre", "select * 
from video_genre");

        $this->CI->pdo_db->prepare_statement("video_link_video_genre", "select * 
from video_link_video_genre");

        $this->CI->pdo_db->prepare_statement("video_show", "select * 
from video_show");

        $this->CI->pdo_db->prepare_statement("video_video", "select * 
from video_video");
    }

}

/* End of file MediaDB.php */
/* Location: ./application/models/MediaDB.php */