#!/bin/bash
##PBS -l nodes=centurion011+centurion012
#path=/localtmp/storage@desk
path=/home/hh4z/SD-linux
for i in `cat hosts`
do
  echo $i
  ssh $i "$path/storagemachine.sh" restart &
done
