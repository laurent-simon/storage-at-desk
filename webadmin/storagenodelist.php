<?php
include_once('_config.php');

//  DATABASE
$conn = mysql_connect($dbhost, $dbuser, $dbpassword);
if (!$conn) {
    echo "Unable to connect to DB: " . mysql_error();
    exit;
}
if (!mysql_select_db($database)) {
    echo "Unable to select mydbname: " . mysql_error();
    exit;
}

$sql = "SELECT * FROM `machine`";
$result = mysql_query($sql);
while($row = mysql_fetch_assoc($result)){
  $machine[] = $row; }
  
$sql = "SELECT * FROM `mapping`";
$result = mysql_query($sql);
while($row = mysql_fetch_assoc($result)){
  $mapping[] = $row; }

$sql = "SELECT * FROM `volume`";
$result = mysql_query($sql);
while($row = mysql_fetch_assoc($result)){
  $volume[] = $row; }
  
function array2table($arr,$width,$link=FALSE,$color=FALSE)
   {
   $count = count($arr);
   if($count > 0){
       reset($arr);
       $num = count(current($arr));
       echo "<table class=\"dataTable\" cellpadding=\"2\" cellspacing=\"0\" width=\"$width\">\n";
       echo "  <tr>\n";
       foreach(current($arr) as $key => $value){
           echo "    <th>";
           echo $key."&nbsp;";
           echo "</th>\n";  
           }  
       echo "  </tr>\n";
       while ($curr_row = current($arr)) {
           echo "  <tr>\n";
           $col = 1;
		   $linkid = NULL;
		   $lastbeat = $curr_row[lastbeat];
           while (false !== ($curr_field = current($curr_row))) {
			   if ($col == 1) $linkid = $curr_field;
               if ($color) {
				   if (($timestamp = strtotime($lastbeat)) !== false) {
					   if (time()-$timestamp <= 3600) echo "    <td class=\"green\">";
					   elseif (time()-$timestamp <= 7200) echo "    <td class=\"yellow\">";
					   else {
				           echo "    <td class=\"red\">";
				       }
				   }
				   else {
				       echo "    <td class=\"red\">";
				   }
			   }
			   else {
				   echo "    <td>";
			   }
               if ($col == 1 && $link) echo '<a href="?id='.$linkid.'"><strong>'.$linkid.'</strong></a>&nbsp;';
			   else echo $curr_field."&nbsp;";
               echo "</td>\n";
               next($curr_row);
               $col++;
               }
           while($col <= $num){
               echo "    <td>&nbsp;</td>\n";
               $col++;      
           }
           echo "  </tr>\n";
           next($arr);
           }
       echo "</table>\n<br />\n\n";
       }
   }



if (isset($_GET['id']) && is_numeric($_GET['id'])) {
	$volumeid = mysql_real_escape_string($_GET['id']);
	if (mysql_num_rows(mysql_query("SELECT * FROM `volume` WHERE `id` = ".$volumeid)) == 0) unset($volumeid);
	else {
		$result = mysql_query("SELECT * FROM `mapping` WHERE `volume` = ".$volumeid);
		while($row = mysql_fetch_assoc($result)){
			$idresult[] = $row; }
	}
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php echo $sd['styles'] ?>
<?php echo $sd['scripts'] ?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Storage Node List - Storage@Desk WebAdmin</title>
</head>
<body>
<div id="top"> <a href="index.php"><img src="images/sad.png" style="float:left" /></a> <br />
  <div class="topTitle">Storage@Desk Storage Node List</div>
</div>
<div id="main">
  <table id="mainTable" cellpadding="0" cellspacing="0" border="0">
    <tr>
      <td style="width:230px;"><div id="left">
          <div class="colhead">Common Tasks</div>
          <ul style="margin: 10px 0 0">
            <li><a href="volumerequest.php">Request Storage Volume</a></li>
            <li><a href="storagenodelist.php">Storage Node Status</a></li>
            <li><a href="removevolume.php">Delete Volume</a></li>
          </ul>
        </div></td>
      <td><div id="middle">
          <h1>Database:</h1>
<?php
if (!isset($volumeid)) {
		  echo "Machine:<br />\n";
		  array2table($machine,"100%",FALSE,TRUE);
		  echo "Mapping:<br />\n";
		  array2table($mapping,"100%");
		  echo "Volume:<br />\n";
		  array2table($volume,"100%",TRUE);
}
else {
		  echo "Mapping: Volume ID $volumeid<br />\n";
		  array2table($idresult,"100%");
}
?>
        </div></td>
    </tr>
  </table>
</div>
</body>
</html>
