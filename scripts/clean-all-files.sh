#!/bin/bash
rm *.log
rm *.out
rm *.log.*
for i in `cat hosts`
do
  echo $i
  ssh $i killall java
  ssh $i rm /localtmp/storage@desk/StorageDesk*
done

