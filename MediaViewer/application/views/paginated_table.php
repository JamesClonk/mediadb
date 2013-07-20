<?php

if (empty($result)) {
    echo('<h2 class="displaying">No data found</h2>');
    return;
}

// get current pagination
$current_page = $this->uri->segment($uri_segment, 0);

echo('<h2 class="displaying">Displaying ' . ($current_page + 1) . '-' . ($current_page + $per_page) . ' of ' . count($result) . '</h2>');

echo($this->pagination->create_links());

echo('<br/>');

$data['current_page'] = $current_page;
$data['pet_page'] = $per_page;
$data['result'] = $result;
$this->load->view('table', $data);

echo($this->pagination->create_links());


/* End of file paginated_table.php */
/* Location: ./application/views/paginated_table.php */