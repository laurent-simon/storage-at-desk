<?php
include_once('_config.php');
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php echo $sd['styles'] ?>
<?php echo $sd['scripts'] ?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Storage@Desk WebAdmin</title>
</head>
<body>
<div id="top"> <a href="index.php"><img src="images/sad.png" style="float:left" /></a> <br />
  <div class="topTitle">Storage@Desk Administration Console</div>
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
          <h1>Introduction:</h1>
          <p>This site is intended for use my administrators of the Storage@Desk (S@D) system.  It will allow for
            users to:</p>
          <ul style="margin: 10px 25px 0">
            <li>Request a new storage volume</li>
            <li>Check storage node status</li>
            <li>Manually create a replica</li>
            <li>Set disk replication levels</li>
            <li>Display free-space/locations</li>
          </ul>
          <br />
          <p>All of these options may be navigated too by clicking on one of the links provided in the one of the 
          adjacent toolbars, entitled common tasks and statistics.</p>
        </div></td>
    </tr>
  </table>
</div>
</body>
</html>