#!/bin/bash
nohup java -classpath ./lib/log4j.jar:. edu.virginia.cs.storagedesk.storageserver.StorageServer > server.out &
