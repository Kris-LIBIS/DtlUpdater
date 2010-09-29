#!/bin/ksh

. /exlibris/lias/tools/DtlUpdater/dtlUpdater.ksh $*

java -cp $TOOLS_DIR/DtlUpdater.jar UpdateStream --logFile $DTL_LOG_FILE $*

