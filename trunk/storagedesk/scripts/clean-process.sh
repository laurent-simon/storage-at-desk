#!/bin/bash
for i in `cat hosts`
do
  echo $i
  ssh $i killall wrapper
  ssh $i killall java
done

