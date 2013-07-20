<?php

echo("<h1>Search Results</h1>");

$data['current_page'] = 0;

if (!empty($file_data)) {
    echo('<br/>');
    echo('<h1>Files</h1>');
    echo('<br/>');
    
    $data['per_page'] = count($file_data) + 1;
    $data['result'] = $file_data;
    $this->load->view('table', $data);
}

if (!empty($song_data)) {
    echo('<br/>');
    echo('<h1>Songs</h1>');
    echo('<br/>');

    $data['per_page'] = count($song_data) + 1;
    $data['result'] = $song_data;
    $this->load->view('table', $data);
}

if (!empty($video_data)) {
    echo('<br/>');
    echo('<h1>Videos</h1>');
    echo('<br/>');

    $data['per_page'] = count($video_data) + 1;
    $data['result'] = $video_data;
    $this->load->view('table', $data);
}

if (!empty($album_data)) {
    echo('<br/>');
    echo("<h1>Albums</h1>");
    echo('<br/>');

    $data['per_page'] = count($album_data) + 1;
    $data['result'] = $album_data;
    $this->load->view('table', $data);
}

if (!empty($artist_data)) {
    echo('<br/>');
    echo("<h1>Artists</h1>");
    echo('<br/>');

    $data['per_page'] = count($artist_data) + 1;
    $data['result'] = $artist_data;
    $this->load->view('table', $data);
}

if (!empty($genre_data)) {
    echo('<br/>');
    echo("<h1>Genres</h1>");
    echo('<br/>');

    $data['per_page'] = count($genre_data) + 1;
    $data['result'] = $genre_data;
    $this->load->view('table', $data);
}


/* End of file search.php */
/* Location: ./application/views/search.php */