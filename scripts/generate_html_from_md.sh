#!/bin/bash


SCRIPT_DIR=$(dirname "$0")
BASEDIR=$SCRIPT_DIR/..

cd $BASEDIR/docs
for i in *.md ; do echo "$i" && pandoc -s $i --metadata title="$i" --css="../app/src/main/assets/help/layout.css" --embed-resources -o ../app/src/main/assets/help/$i.html ; done
cd --
