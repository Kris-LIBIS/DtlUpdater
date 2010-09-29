#!/bin/ksh

export TOOLS_DIR=`dirname $0`

DTL_LOG_FILE=`echo $* | awk -f $TOOLS_DIR/awk.prog`

NR=`date +%Y%m%d%I%M%S`

[ "$DTL_LOG_FILE" = "" ] && DTL_LOG_FILE=`basename $0 ksh`log.$NR

export DTL_LOG_FILE

echo The log file output will be stored in $DTL_LOG_FILE

