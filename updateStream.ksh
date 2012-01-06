#!/bin/ksh

export TOOLS_DIR=`dirname $0`

. $TOOLS_DIR/dtlUpdater.ksh $*

java -jar $TOOLS_DIR/dist/UpdateStream.jar --logFile $DTL_LOG_FILE $*

