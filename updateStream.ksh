#!/bin/ksh

export TOOLS_DIR=`dirname $0`

. $TOOLS_DIR/dtlUpdater.ksh $*

java -cp $TOOLS_DIR/DtlUpdater.jar UpdateStream --logFile $DTL_LOG_FILE $*

