<?php

echo("<h1>File</h1>");
echo('<br/>');

$data['current_page'] = 0;
$data['per_page'] = count($file_data) + 1;
$data['result'] = $file_data;
$this->load->view('table', $data);

if (!empty($song_data)) {
    echo('<br/>');
    echo("<h1>Song</h1>");
    echo('<br/>');

    $data['per_page'] = count($song_data) + 1;
    $data['result'] = $song_data;
    $this->load->view('table', $data);
}

if (!empty($video_data)) {
    echo('<br/>');
    echo("<h1>Video</h1>");
    echo('<br/>');

    $data['per_page'] = count($song_data) + 1;
    $data['result'] = $song_data;
    $this->load->view('table', $data);
}

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

if (!empty($video_stream_data)) {
    echo('<br/>');
    echo("<h1>Video Streams</h1>");
    echo('<br/>');

    $data['per_page'] = count($video_stream_data) + 1;
    $data['result'] = $video_stream_data;
    $this->load->view('table', $data);
}

if (!empty($audio_stream_data)) {
    echo('<br/>');
    echo("<h1>Audio Streams</h1>");
    echo('<br/>');

    $data['per_page'] = count($audio_stream_data) + 1;
    $data['result'] = $audio_stream_data;
    $this->load->view('table', $data);
}

if (!empty($subtitle_stream_data)) {
    echo('<br/>');
    echo("<h1>Subtitle Streams</h1>");
    echo('<br/>');

    $data['per_page'] = count($subtitle_stream_data) + 1;
    $data['result'] = $subtitle_stream_data;
    $this->load->view('table', $data);
}



/* End of file file.php */
/* Location: ./application/views/file.php */