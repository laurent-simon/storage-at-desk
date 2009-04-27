<?php
$sd = array(
	displayErrors => true,
	styles => '<link href="favicon.ico" rel="icon" type="image/x-icon" />
<style type="text/css" media="screen, projection">@import "style.css";</style>
',
	scripts => '
',
	endScripts => '
'
);

//  ERROR REPORTING
if ($sd['displayErrors']) error_reporting(E_ALL ^ E_NOTICE);
else {
	set_error_handler('errorHandler');
	error_reporting(0);
}
function errorHandler($error, $str, $file, $line) {
	@file_put_contents('_errorLog.log', "Line $line in $file (Error $error):\r\n  $str\r\n  ".date('r')." IP: ".$_SERVER['REMOTE_ADDR']." UserAgent: ".$_SERVER['HTTP_USER_AGENT']."\r\n\r\n", FILE_APPEND);
}

//  DATABASE
$dbhost='199.111.199.159';
$dbuser='storagedesk';
$dbpassword='sdisgood';
$database='storage@desk';

?>
