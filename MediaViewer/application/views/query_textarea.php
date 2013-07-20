<?php
$rows = count(preg_split('/\n/', $sql)) + 1;
if ($rows < 5) {
    $rows = 5;
}
?><form method="POST" action="<?php echo(base_url()); ?>view/custom_query">
    <textarea class="resizable" rows="<?php echo($rows); ?>" cols="140" name="custom_query"><?php echo($sql); ?></textarea>
    <br/><input type="submit" name="Query" value="Query"/>
</form>
