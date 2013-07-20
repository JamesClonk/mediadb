<?php

echo("<h1>Artist</h1>");
echo('<br/>');

$data['current_page'] = 0;
$data['per_page'] = count($artist_data) + 1;
$data['result'] = $artist_data;
$this->load->view('table', $data);

if (!empty($album_data)) {
    echo('<br/>');
    echo("<h1>Albums</h1>");
    echo('<br/>');

    $data['per_page'] = count($album_data) + 1;
    $data['result'] = $album_data;
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

if (!empty($song_data)) {
    echo('<br/>');
    echo("<h1>Songs</h1>");
    echo('<br/>');

    $data['per_page'] = count($song_data) + 1;
    $data['result'] = $song_data;
    $this->load->view('table', $data);
}


/* End of file artist.php */
/* Location: ./application/views/artist.php */