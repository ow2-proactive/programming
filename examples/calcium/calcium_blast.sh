#!/bin/sh

echo
echo --- Calcium example  ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

DESCRIPTOR=blast_args/LocalDescriptor.xml
QUERY=blast_args/query.nt
DATABASE=blast_args/db.nt
FORMATBD=blast_args/formatdb
BLASTALL=blast_args/blastall

$JAVACMD org.objectweb.proactive.extensions.calcium.examples.blast.Blast $DESCRIPTOR $QUERY $DATABASE $FORMATBD $BLASTALL

echo
echo ------------------------------------------------------------
