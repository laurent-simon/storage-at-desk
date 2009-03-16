#!/bin/bash
for i in `cat hosts.all`
do
  echo $i
  ssh $i mkdir /localtmp/storage@desk/
done

