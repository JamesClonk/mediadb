<div id="sidebar"><br />
    <h4>List Data</h4>
    <p class="list"><a href="<?php echo site_url("view/all_files"); ?>">:: all files</a>
        <br/><span class="listitem">:: types</span>
        <br/><a href="<?php echo site_url("view/all_video_files"); ?>" class="listitem2">:: video</a>
        <br/><a href="<?php echo site_url("view/all_music_files"); ?>" class="listitem2">:: music</a>
        <br/><span class="listitem">:: duplicates</span>
        <br/><a href="<?php echo site_url("view/all_duplicate_files_by_hash"); ?>" class="listitem2">:: by hash</a>
        <br/><a href="<?php echo site_url("view/all_duplicate_files_by_drive_and_path"); ?>" class="listitem2">:: by drive and path</a>
        <br/><a href="<?php echo site_url("view/all_duplicate_files_by_path"); ?>" class="listitem2">:: by path</a>
        <br/><a href="<?php echo site_url("view/all_duplicate_files_by_drive"); ?>" class="listitem2">:: by drive</a>
    </p>
    <p class="list"><a href="<?php echo site_url("view/all_drives"); ?>">:: all drives</a></p>
    <p class="list"><a href="<?php echo site_url("view/all_music"); ?>">:: all music</a>
        <br/><span class="listitem">:: criteria</span>
        <br/><a href="<?php echo site_url("view/all_albums"); ?>" class="listitem2">:: all albums</a>
        <br/><a href="<?php echo site_url("view/all_artists"); ?>" class="listitem2">:: all artists</a>
        <br/><a href="<?php echo site_url("view/all_genres"); ?>" class="listitem2">:: all genres</a>
    </p>
    
    <br/>
    <h4>Tables</h4>
    <p class="list">
        <a href="<?php echo site_url("view/erd"); ?>">:: ERD</a>
        <br/><a href="<?php echo site_url("view/gnrl_file"); ?>">:: gnrl_file</a>
        <br/><a href="<?php echo site_url("view/gnrl_file_type"); ?>">:: gnrl_file_type</a>
        <br/><a href="<?php echo site_url("view/gnrl_path"); ?>">:: gnrl_path</a>
        <br/><a href="<?php echo site_url("view/gnrl_drive"); ?>">:: gnrl_drive</a>
        <br/><a href="<?php echo site_url("view/gnrl_host"); ?>">:: gnrl_host</a>
        <br/><a href="<?php echo site_url("view/gnrl_language"); ?>">:: gnrl_language</a>
        <br/><a href="<?php echo site_url("view/gnrl_picture"); ?>">:: gnrl_picture</a>
        <br/><a href="<?php echo site_url("view/gnrl_codec"); ?>">:: gnrl_codec</a>
        <br/><a href="<?php echo site_url("view/gnrl_audio_stream"); ?>">:: gnrl_audio_stream</a>
        <br/><a href="<?php echo site_url("view/gnrl_video_stream"); ?>">:: gnrl_video_stream</a>
        <br/><a href="<?php echo site_url("view/gnrl_subtitle_stream"); ?>">:: gnrl_subtitle_stream</a>
        <br/><a href="<?php echo site_url("view/gnrl_link_file_drive"); ?>">:: gnrl_link_file_drive</a>
        <br/><a href="<?php echo site_url("view/gnrl_link_file_path"); ?>">:: gnrl_link_file_path</a>
        <br/><a href="<?php echo site_url("view/gnrl_link_file_picture"); ?>">:: gnrl_link_file_picture</a>
        <br/><a href="<?php echo site_url("view/gnrl_link_path_drive"); ?>">:: gnrl_link_path_drive</a>
        <br/><a href="<?php echo site_url("view/music_album"); ?>">:: music_album</a>
        <br/><a href="<?php echo site_url("view/music_artist"); ?>">:: music_artist</a>
        <br/><a href="<?php echo site_url("view/music_genre"); ?>">:: music_genre</a>
        <br/><a href="<?php echo site_url("view/music_song"); ?>">:: music_song</a>
        <br/><a href="<?php echo site_url("view/video_genre"); ?>">:: video_genre</a>
        <br/><a href="<?php echo site_url("view/video_link_video_genre"); ?>">:: video_link_video_genre</a>
        <br/><a href="<?php echo site_url("view/video_show"); ?>">:: video_show</a>
        <br/><a href="<?php echo site_url("view/video_video"); ?>">:: video_video</a>
    </p>
</div>
<div id="content">