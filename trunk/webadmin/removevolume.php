<?php
include_once('_config.php');
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php echo $sd['styles'] ?>
<?php echo $sd['scripts'] ?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Remove Volume - Storage@Desk WebAdmin</title>
</head>
<body>
<div id="top"> <a href="index.php"><img src="images/sad.png" style="float:left" /></a> <br />
  <div class="topTitle">Storage@Desk Delete Volume</div>
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
          <h1>Delete an Existing Volume:</h1>
          <?php
				if( isset( $_POST['targetName'] ) ) {
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
					
					$query = sprintf( "DELETE FROM volume WHERE name = '%s';",
						mysql_real_escape_string($_POST["targetName"])
						);
					$result = mysql_query($query);
				}
			?>
          <form name="deleteVolume" action="RemoveVolume.php" method="post">
            <label for="targetName">Target Name:</label>
            <input id="targetName" type='text' name='targetName' value='iqn.edu.virginia.cs.storagedesk:disk' size="40" /><br />
            <input type='submit' />
          </form>
        </div></td>
    </tr>
  </table>
</div>
</body>
</html>
