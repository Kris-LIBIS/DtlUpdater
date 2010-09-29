#!/bin/ksh

. /exlibris/lias/tools/DtlUpdater/dtlUpdater.ksh $*

java -cp $TOOLS_DIR/DtlUpdater.jar AddStream --logFile $DTL_LOG_FILE $*

