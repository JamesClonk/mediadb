<?php

// not to be used directly!
// used by other views to display table data..
// requires $result, $current_page and $per_page values to be set

if (empty($result)) {
    return;
}

echo('<table class="widefat"><thead><tr>');

foreach (array_keys($result[0]) as $title) {
    echo('<th scope="col">' . $title . '</th>');
}

echo('</tr></thead><tbody>');

for ($i = 0; $i < $per_page; $i++) {
    if (empty($result[($i + $current_page)])) {
        break;
    }
    $class = $i % 2 > 0 ? 'class="tbc_even"' : 'class="tbc_odd"';
    echo("<tr>");
    foreach ($result[($i + $current_page)] as $key => $data) {
        if ($key == "file_id") {
            echo('<td ' . $class . '><a href="' . site_url("view/file/" . stripslashes($data)) . '">' . stripslashes($data) . '</a></td>');
        } elseif ($key == "song_id") {
            echo('<td ' . $class . '><a href="' . site_url("view/song/" . stripslashes($data)) . '">' . stripslashes($data) . '</a></td>');
        } elseif ($key == "album_id") {
            echo('<td ' . $class . '><a href="' . site_url("view/album/" . stripslashes($data)) . '">' . stripslashes($data) . '</a></td>');
        } elseif ($key == "artist_id") {
            echo('<td ' . $class . '><a href="' . site_url("view/artist/" . stripslashes($data)) . '">' . stripslashes($data) . '</a></td>');
        } elseif ($key == "filename") {
            echo('<td ' . $class . '><a href="file://' . stripslashes($data) . '" target="_new">' . stripslashes($data) . '</a></td>');
        } else {
            echo('<td ' . $class . '>' . stripslashes($data) . '</td>');
        }
    }
    echo("</tr>");
}

echo('</tbody></table>');


/* End of file table.php */
/* Location: ./application/views/table.php */