<?php

if (!defined('BASEPATH')) {
    exit('No direct script access allowed');
}

class View extends CI_Controller {

    function __construct() {
        parent::__construct();
        $this->load->library('session');
        $this->load->library('pagination');
        $this->load->helper('url');

        $this->load->library('pdo_db');
        $this->load->model('MediaDB');

        // if session does not yet contain 'show_query' then set it to 'off' by default
        if (!$this->session->userdata('show_query')) {
            $this->session->set_userdata('show_query', "off");
        }

        // store new value of 'show_query' in the session if defined as a GET value
        if ($this->input->get('show_query')) {
            $this->session->set_userdata('show_query', $this->input->get('show_query'));
        }

        // if a 'custom_sql' is defined, store it in the session
        if ($this->input->post('custom_query')) {
            $this->session->set_userdata('custom_query', $this->input->post('custom_query'));
        }
        
        // if a 'search' is defined, store it in the session
        if ($this->input->post('search')) {
            $this->session->set_userdata('search', $this->input->post('search'));
        }
    }

    public function _remap($method, $parameters = array()) {
        if ($method == "index") {
            redirect(base_url() . "view/all_files"); // default view
        } else if ($method == "image") {
            $this->showPicture($this->uri->segment(3, 0));
        }
        $this->showData();
    }

    private function showPicture($picture_id) {
        $picture = $this->pdo_db->execute_query("picture_by_picture_id", array($picture_id));

        if (!empty($picture[0])) {
            header('Content-Type: ' . $picture[0]['mime_type']);
            print($picture[0]['image_data']);
        }
    }

    private function showData() {
        // default values
        $view = $this->uri->segment(2, "all_files");
        $data = array();
        // values used by pagination library
        $data['base_url'] = base_url() . "view/$view/page/";
        $data['total_rows'] = 1000;
        $data['per_page'] = 30;
        $data['num_links'] = 3;
        $data['uri_segment'] = 4;
        $data['full_tag_open'] = '<div class="tablenav-pages">';
        $data['full_tag_close'] = '</div>';
        $data['first_link'] = 'first';
        $data['last_link'] = 'last';
        $data['next_tag_open'] = '<span class="next">';
        $data['next_tag_close'] = '</span>';
        $data['next_link'] = '&raquo;';
        $data['prev_tag_open'] = '<span class="prev">';
        $data['prev_tag_close'] = '</span>';
        $data['prev_link'] = '&laquo;';
        $data['cur_tag_open'] = '<span class="current">';
        $data['cur_tag_close'] = '</span>';
        $data['anchor_class'] = 'class="page-numbers" ';

        // verify view name to make sure its valid, otherwise show 404 page
        // prepared statement names additionally also serve to verify valid page views..
        if (!preg_match("/^(file|erd|search|video|song|album|artist|custom_query)$/", $view)
                && !$this->pdo_db->is_statement($view)) {
            show_404();
        }

        $data['show_query'] = $this->session->userdata('show_query');
        $this->load->view('header', $data);
        $this->load->view('sidebar');

        switch ($view) {
            case "erd":
                $this->load->view('erd');
                break;
            case "search":
                $search_value = trim($this->session->userdata('search'));
                $search_value = "%" . trim(preg_replace("/(^%+|%+$)/", "", $search_value)) . "%";
                $search_value = preg_replace("/ and /i", "%", $search_value);
                $search_value = preg_replace("/ /", "%", $search_value);
                
                $data['file_data'] = $this->pdo_db->execute_query("files_by_search", array($search_value));
                $data['song_data'] = $this->pdo_db->execute_query("songs_by_search", array($search_value));
                $data['album_data'] = $this->pdo_db->execute_query("albums_by_search", array($search_value));
                $data['artist_data'] = $this->pdo_db->execute_query("artists_by_search", array($search_value));
                $data['genre_data'] = $this->pdo_db->execute_query("genres_by_search", array($search_value));
                //$data['video_data'] = $this->pdo_db->execute_query("videos_by_search", array($search_value));
                $this->load->view('search', $data);
                break;
            case "artist":
                if (!$this->uri->segment(3)) {
                    $this->load->view('id_missing', array('type' => 'artist'));
                    break;
                }
                $data['artist_id'] = $this->uri->segment(3);
                $data['artist_data'] = $this->pdo_db->execute_query("artist_by_artist_id", array($data['artist_id']));
                $data['album_data'] = $this->pdo_db->execute_query("albums_by_artist_id", array($data['artist_id']));
                $data['song_data'] = $this->pdo_db->execute_query("songs_by_artist_id", array($data['artist_id']));
                $data['genre_data'] = $this->pdo_db->execute_query("genres_by_artist_id", array($data['artist_id']));
                $this->load->view('artist', $data);
                break;
            case "album":
                if (!$this->uri->segment(3)) {
                    $this->load->view('id_missing', array('type' => 'album'));
                    break;
                }
                $data['album_id'] = $this->uri->segment(3);
                $data['album_data'] = $this->pdo_db->execute_query("album_by_album_id", array($data['album_id']));
                $data['picture_data'] = $this->pdo_db->execute_query("pictures_by_album_id", array($data['album_id']));
                $data['song_data'] = $this->pdo_db->execute_query("songs_by_album_id", array($data['album_id']));
                $data['artist_data'] = $this->pdo_db->execute_query("artists_by_album_id", array($data['album_id']));
                $data['genre_data'] = $this->pdo_db->execute_query("genres_by_album_id", array($data['album_id']));
                $this->load->view('album', $data);
                break;
            case "video":
                if (!$this->uri->segment(3)) {
                    $this->load->view('id_missing', array('type' => 'video'));
                    break;
                }
                if (!isset($data['file_id'])) {
                    $result = $this->pdo_db->execute_query("file_id_by_video_id", array($this->uri->segment(3)));
                    $data['file_id'] = $result[0]['file_id'];
                }
            case "song":
                if (!$this->uri->segment(3)) {
                    $this->load->view('id_missing', array('type' => 'song'));
                    break;
                }
                if (!isset($data['file_id'])) {
                    $result = $this->pdo_db->execute_query("file_id_by_song_id", array($this->uri->segment(3)));
                    $data['file_id'] = $result[0]['file_id'];
                }
            case "file":
                if (!$this->uri->segment(3)) {
                    $this->load->view('id_missing', array('type' => 'file'));
                    break;
                }
                if (!isset($data['file_id'])) {
                    $data['file_id'] = $this->uri->segment(3);
                }
                $data['file_data'] = $this->pdo_db->execute_query("file_by_file_id", array($data['file_id']));
                $data['song_data'] = $this->pdo_db->execute_query("song_by_file_id", array($data['file_id']));
                $data['picture_data'] = $this->pdo_db->execute_query("pictures_by_file_id", array($data['file_id']));
                $data['audio_stream_data'] = $this->pdo_db->execute_query("audio_streams_by_file_id", array($data['file_id']));
                $data['video_stream_data'] = $this->pdo_db->execute_query("video_streams_by_file_id", array($data['file_id']));
                $data['subtitle_stream_data'] = $this->pdo_db->execute_query("subtitle_streams_by_file_id", array($data['file_id']));
                $this->load->view('file', $data);
                break;
            case "custom_query":
                $this->pdo_db->prepare_statement($view, $this->session->userdata('custom_query'));
            default:
                $data['sql'] = $this->pdo_db->get_sql($view);
                $data['result'] = $this->pdo_db->execute_query($view, null);

                // show query_textarea only if flag is 'on'
                if ($this->session->userdata('show_query') == "on") {
                    $data['per_page'] = '20'; // make table display fewer rows
                    $this->load->view('query_textarea', $data);
                }

                $data['total_rows'] = count($data['result']);
                $this->pagination->initialize($data);
                $this->load->view('paginated_table', $data);
        }

        $this->load->view('footer');
    }

}

/* End of file view.php */
/* Location: ./application/controllers/view.php */