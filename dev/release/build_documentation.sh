#! /bin/sh

TMP=/tmp

# /mnt/scratch is a tmpfs mount point for faster builds on schubby
if [ -w "/mnt/scratch" ] ; then
    TMP=/mnt/scratch
fi

PROACTIVE_DIR=$1
VERSION=$2
JAVA_HOME=$3
if [ ! -z "$4" ] ; then
    TMP=$4
fi

TMP_DIR=""

echo " [i] PROACTIVE_DIR: $PROACTIVE_DIR"
echo " [i] VERSION:       $VERSION"
echo " [i] JAVA_HOME:     $JAVA_HOME"
echo " [i] TMP:           $TMP"
function warn_and_exit {
    echo "$1" 1>&2
    exit 1
}

function warn_print_usage_and_exit {
    echo "$1" 1>&2
    echo "" 1>&2
    echo "Usage: $0 PROACTIVE_DIR VERSION JAVA_HOME" 1>&2
    exit 1
}


if [ -z "$PROACTIVE_DIR" ] ; then
    warn_print_usage_and_exit "PROACTIVE_DIR is not defined"
fi

if [ -z "$VERSION" ] ; then
    warn_print_usage_and_exit "VERSION is not defined"
fi

if [ -z "$JAVA_HOME" ] ; then
    warn_print_usage_and_exit "JAVA_HOME is not defined"
fi
export JAVA_HOME=${JAVA_HOME}


TMP_DIR="${TMP}/ProActive-doc-${VERSION}"
output=$(mkdir ${TMP_DIR} 2>&1)
if [ "$?" -ne 0 ] ; then
    if [ -e ${TMP_DIR} ] ; then
        echo " [w] ${TMP_DIR} already exists. Delete it !"
        rm -Rf ${TMP_DIR}
        mkdir ${TMP_DIR}
        if [ "$?" -ne 0 ] ; then
            warn_and_exit "Cannot create ${TMP_DIR}: $output"
        fi
    else
        warn_and_exit "Cannot create ${TMP_DIR}"
    fi
fi

cp -Rf ${PROACTIVE_DIR} ${TMP_DIR}

cd ${TMP_DIR} || warn_and_exit "Cannot move in ${TMP_DIR}"


cd compile || warn_and_exit "Cannot move in compile"
./build clean
./build -Dversion="${VERSION}" manualHtml manualSingleHtml manualPdf javadoc.published javadoc.complete

cd ${TMP_DIR} || warn_and_exit "Cannot move in ${TMP_DIR}"
echo " [i] Clean"
