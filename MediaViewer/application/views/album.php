<?php

echo("<h1>Album</h1>");
echo('<br/>');

$data['current_page'] = 0;
$data['per_page'] = count($album_data) + 1;
$data['result'] = $album_data;
$this->load->view('table', $data);

if (!empty($picture_data)) {
    echo('<br/>');
    echo("<h1>Pictures</h1>");
    echo('<br/>');

    echo('<table class="widefat"><tbody><tr>');
    foreach($picture_data as $picture) {
        echo('<td class="tbc_odd"><a href="' . base_url() . 'view/image/' . $picture['image_id'] . '">');
        echo('<img src="' . base_url() . 'view/image/' . $picture['image_id'] . '" width="250px"/></a></td>');
    }
    echo('</tr></tbody></table>');
}

if (!empty($song_data)) {
    echo('<br/>');
    echo("<h1>Songs</h1>");
    echo('<br/>');

    $data['per_page'] = count($song_data) + 1;
    $data['result'] = $song_data;
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


/* End of file album.php */
/* Location: ./application/views/album.php */