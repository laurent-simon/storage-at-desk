<?php
include_once('_config.php');
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php echo $sd['styles'] ?>
<?php echo $sd['scripts'] ?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Volume Request - Storage@Desk WebAdmin</title>
</head>
<body>
<div id="top"> <a href="index.php"><img src="images/sad.png" style="float:left" /></a> <br />
  <div class="topTitle">Storage@Desk Volume Request</div>
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
          <h1>Enter The Following Information:</h1>
          <?php
				if( isset( $_POST['numCp'] ) ) {
					echo '<p style="color: red">Volume Request Executed - Thank You.</p>';
					$cmd = 'java -jar volumemaker.jar -numCp ' . $_POST['numCp'] . ' -LUN ' . 
							$_POST['LUN'] . ' -numBlocks ' . ceil($_POST['numBlocks']*1024*1024) . 
							' -targetName ' . $_POST['targetName'];
					echo exec( $cmd );
				}
			?>
          <form name="requestVolume" action="VolumeRequest.php" method="post">
            <table>
              <tr>
                <td>Replication Level:</td>
                <td><input type='text' name='numCp' value='2' size="50" /></td>
              </tr>
              <tr>
                <td>LUN:</td>
                <td><input type='text' name='LUN' value='1' size="50" /></td>
              </tr>
              <tr>
                <td>Volume Size (GB):</td>
                <td><input type='text' name='numBlocks' value='16' size="50" /></td>
              </tr>
              <tr>
                <td>Target Name:</td>
                <td><input type='text' name='targetName' value='iqn.edu.virginia.cs.storagedesk:disk3' size="50" /></td>
              </tr>
              <tr>
                <td colspan="2"><input type='submit' /></td>
              </tr>
            </table>
          </form>
        </div>
    </tr>
  </table>
</div>
</body>
</html>