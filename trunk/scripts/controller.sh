#!/bin/bash
nohup java -classpath ./lib/log4j.jar:./lib/mysql-connector.jar:. -Djava.rmi.server.codebase=file:///home/hh4z/SD-linux edu.virginia.cs.storagedesk.volumecontroller.VolumeController > controller.out &
