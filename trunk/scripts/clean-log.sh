#!/bin/bash
for i in `cat hosts`
do
  echo $i
  ssh $i rm /localtmp/storage@desk/*.log /localtmp/storage@desk/*.out
done

