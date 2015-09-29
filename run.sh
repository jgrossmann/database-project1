#!/bin/sh

LIB=lib
CLASSPATH=$LIB/*:.:class

# Usage: run.sh <precision> <query>
/usr/bin/java -classpath $CLASSPATH App $1 $2


