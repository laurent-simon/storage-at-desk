-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.41-community-nt


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema `storage@desk`
--

CREATE DATABASE IF NOT EXISTS `storage@desk`;
USE `storage@desk`;

--
-- Definition of table `machine`
--

DROP TABLE IF EXISTS `machine`;
CREATE TABLE `machine` (
  `id` varchar(512) NOT NULL,
  `name` varchar(512) NOT NULL,
  `ip` varchar(512) NOT NULL,
  `path` varchar(512) NOT NULL,
  `numchunks` int(11) NOT NULL,
  `chunksize` bigint(20) unsigned NOT NULL,
  `lastbeat` datetime default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `hostname` (`name`),
  UNIQUE KEY `ip` (`ip`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `machine`
--

/*!40000 ALTER TABLE `machine` DISABLE KEYS */;
/*!40000 ALTER TABLE `machine` ENABLE KEYS */;


--
-- Definition of table `mapping`
--

DROP TABLE IF EXISTS `mapping`;
CREATE TABLE `mapping` (
  `volume` bigint(20) NOT NULL,
  `replica` int(11) NOT NULL,
  `virtualchunk` int(11) NOT NULL,
  `machine` varchar(512) NOT NULL,
  `physicalchunk` int(11) NOT NULL,
  PRIMARY KEY  (`volume`,`replica`,`virtualchunk`),
  UNIQUE KEY `machine` (`machine`,`physicalchunk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `mapping`
--

/*!40000 ALTER TABLE `mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `mapping` ENABLE KEYS */;


--
-- Definition of table `volume`
--

DROP TABLE IF EXISTS `volume`;
CREATE TABLE `volume` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(1024) NOT NULL,
  `numcopies` int(11) NOT NULL,
  `numchunks` int(11) NOT NULL,
  `chunksize` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `volume`
--

/*!40000 ALTER TABLE `volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `volume` ENABLE KEYS */;


--
-- Definition of procedure `sp_availablechunk`
--

DROP PROCEDURE IF EXISTS `sp_availablechunk`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_availablechunk`(IN mi VARCHAR(512))
BEGIN
  DECLARE total INT;
  DECLARE ci INT;
  DECLARE c INT;
  DECLARE r INT;
  SET ci = 0;
  SET c = 0;
  SET r = -1;

  SELECT numchunks INTO total
  FROM machine
  WHERE id = mi;

  L1: LOOP
    SELECT COUNT(*) INTO c
    FROM mapping
    WHERE machine = mi and physicalchunk = ci;

    IF c = 0 THEN
      SET r = ci;
      LEAVE L1;
    ELSE
      SET ci = ci + 1;
      IF ci = total THEN
        LEAVE L1;
      ELSE
        ITERATE L1;
      END IF;
    END IF;
  END LOOP L1;

  SELECT r as availablechunk;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_availablemachines`
--

DROP PROCEDURE IF EXISTS `sp_availablemachines`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_availablemachines`(IN size BIGINT)
BEGIN
  SELECT id, name, ip, numchunks, chunksize
  FROM machine
  WHERE chunksize = size and numchunks > (SELECT count(*)
                                          FROM mapping
                                          WHERE machine = id);
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_insertmachine`
--

DROP PROCEDURE IF EXISTS `sp_insertmachine`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_insertmachine`(IN i VARCHAR(512),
                                                       IN name VARCHAR(512),
                                                       IN mip VARCHAR(512),
                                                       IN p VARCHAR(512),
                                                       IN num INT,
                                                       IN size BIGINT)
BEGIN
  INSERT machine(id, name, ip, path, numchunks, chunksize)
  VALUES(i, name, mip, p, num, size);
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_insertmapping`
--

DROP PROCEDURE IF EXISTS `sp_insertmapping`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_insertmapping`(IN v BIGINT(20),
                                                       IN r INT,
                                                       IN vc INT,
                                                       IN m VARCHAR(512),
                                                       IN pc INT)
BEGIN
  INSERT mapping(volume, replica, virtualchunk, machine, physicalchunk)
  VALUES(v, r, vc, m, pc);
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_insertvolume`
--

DROP PROCEDURE IF EXISTS `sp_insertvolume`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_insertvolume`(IN n VARCHAR(512),
                                                      IN nc INT,
                                                      IN num INT,
                                                      IN size BIGINT)
BEGIN
  INSERT volume(name, numcopies, numchunks, chunksize)
  VALUES(n, nc, num, size);
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_machinebyid`
--

DROP PROCEDURE IF EXISTS `sp_machinebyid`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_machinebyid`(IN i VARCHAR(512))
BEGIN
  SELECT id, name, ip, numchunks, chunksize
  FROM machine
  WHERE id = i;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_machineheartbeat`
--

DROP PROCEDURE IF EXISTS `sp_machineheartbeat`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_machineheartbeat`(IN i VARCHAR(512),
                                                          IN mip VARCHAR(512))
BEGIN
  UPDATE machine
  SET lastbeat = NOW(), ip = mip
  WHERE id = i;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_machinenumavailablechunks`
--

DROP PROCEDURE IF EXISTS `sp_machinenumavailablechunks`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_machinenumavailablechunks`(IN i VARCHAR(512))
BEGIN
  DECLARE total INT;
  DECLARE used  INT;

  SELECT numchunks INTO total
  FROM machine
  WHERE id = i;

  SELECT count(*) INTO used
  FROM mapping
  WHERE machine = i;

  SELECT (total - used) AS numchunks;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_machinestatus`
--

DROP PROCEDURE IF EXISTS `sp_machinestatus`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_machinestatus`(IN i VARCHAR(512),
                                                          IN length BIGINT)
BEGIN
  DECLARE beat DATETIME;
  DECLARE machinestatus BOOLEAN;
  SET machinestatus = false;

  SELECT lastbeat into beat
  FROM machine
  WHERE id = i;

  IF (UNIX_TIMESTAMP() - UNIX_TIMESTATMP(beat) <= length) THEN
    SET machinestatus = true;
  END IF;

  SELECT machinestatus;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_mappingbyvolume`
--

DROP PROCEDURE IF EXISTS `sp_mappingbyvolume`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_mappingbyvolume`(IN v INT)
BEGIN
  SELECT mapping.volume, mapping.replica, mapping.virtualchunk,
         mapping.machine, mapping.physicalchunk, machine.ip
  FROM mapping AS mapping, machine as machine
  WHERE mapping.volume = v and mapping.machine = machine.id;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_removemachine`
--

DROP PROCEDURE IF EXISTS `sp_removemachine`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_removemachine`(IN n VARCHAR(512))
BEGIN
  DECLARE machineid VARCHAR(512);
  DECLARE volumeid BIGINT;

  SELECT id INTO machineid
  FROM machine
  WHERE name = n;

  SELECT DISTINCT volume INTO volumeid
  FROM mapping
  WHERE machine = machined;

  DELETE FROM volume
  WHERE id = volumeid;

  DELETE FROM mapping
  WHERE machine = machineid;

  DELETE FROM machine
  WHERE name = n;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_truncateall`
--

DROP PROCEDURE IF EXISTS `sp_truncateall`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_truncateall`()
BEGIN
  TRUNCATE machine;
  TRUNCATE volume;
  TRUNCATE mapping;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `sp_volumebyname`
--

DROP PROCEDURE IF EXISTS `sp_volumebyname`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_volumebyname`(IN n VARCHAR(512))
BEGIN
  SELECT id, name, numcopies, numchunks, chunksize
  FROM volume
  WHERE name = n;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
