<html>
    <head>
        <title>Media Viewer</title>
        <link rel="stylesheet" type="text/css" href="<?php echo base_url(); ?>files/style.css" />

        <script type="text/javascript" src="<?php echo base_url(); ?>files/jquery-1.6.1.min.js"></script>
        <script type="text/javascript" src="<?php echo base_url(); ?>files/jquery.textarearesizer.compressed.js"></script>
        <script type="text/javascript" src="<?php echo base_url(); ?>files/jquery-ui-1.8.13.custom.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                $('textarea.resizable:not(.processed)').TextAreaResizer();
                
                $("thead").click(function () {
                    $(this).next("tbody").toggle("highlight", {}, 1);
                });
            });
        </script>
    </head>
    <body>
        <div id="header">
            <div id="title">Media Viewer</div>
            <div id="options">
                <form method="POST" action="<?php echo(base_url()); ?>view/search">search:&nbsp;<input type="text" name="search"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;show query: <?php
if ($show_query == "on") {
    echo('<a href="' . current_url() . '?show_query=off">' . $show_query . '</a>');
} else {
    echo('<a href="' . current_url() . '?show_query=on">' . $show_query . '</a>');
}
?></form>
            </div></div>