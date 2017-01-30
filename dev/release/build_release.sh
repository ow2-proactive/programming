#! /bin/sh

function die {
    echo "BUILD FAILED: $1" 1>&2
    exit 2
}

function warn_and_exit {
    echo "$1" 1>&2
    exit 1
}

function warn_print_usage_and_exit {
    echo "$1" 1>&2
    echo "" 1>&2
    echo "Usage: $0 PROACTIVE_DIR VERSION JAVA_HOME [TMP]" 1>&2
    exit 1
}

##########
# Fetch arguments

# Source directory (should be a clean checkout)
ORIG_DIR=$1
# Version to release (can be x.y.z ot x.y.z-tag)
VERSION=$2
# The JDK to use to build the release
JAVA_HOME=$3
# Where to put the artifacts
ARTIFACT_DIR=$4
# All work happen in this directory
WORK_DIR=$(mktemp -d)
# Eeach artifact use this directory as a master then strip or enhance it
MASTER_DIR=${WORK_DIR}/master

if [ -z "$ORIG_DIR" ] ; then
    warn_print_usage_and_exit "ORIG_DIR is not defined"
fi

if [ -z "$VERSION" ] ; then
    warn_print_usage_and_exit "VERSION is not defined"
fi

if [ -z "$JAVA_HOME" ] ; then
    warn_print_usage_and_exit "JAVA_HOME is not defined"
fi
if [ -z "$ARTIFACT_DIR" ] ; then
    warn_print_usage_and_exit "ARTIFACT_DIR is not defined"
fi
export JAVA_HOME=${JAVA_HOME}


###########
# Prepare MASTER_DIR

cp -Rf ${ORIG_DIR} ${MASTER_DIR} || die "Failed to create MASTER_DIR"

# Check that serial version UIDs match the version. Missing serial version UID are not checked
cd ${TMP_DIR} || warn_and_exit "Cannot move in ${TMP_DIR}"
if [ "$(find ${MASTER_DIR}/src/ -name "*.java" | xargs grep serialVersionUID | grep -v `echo $VERSION | cut -d'.' -f1,2 --output-delimiter ""` | wc -l)" -gt 0 ] ; then
    if [ -z "${RELAX}" ] ; then
        warn_and_exit " [E] serialVersionUID are NOT defined"
    fi
fi

# Check Main.PA_VERSION
echo ${VERSION} | grep -q $(cat ${MASTER_DIR}/src/Core/org/objectweb/proactive/Main.java | grep 'static final private String PA_VERSION =' | cut -d '"' -f2)
if [ "$?" != 0 ] ; then
    warn_and_exit " [E] Bad version in Main.PA_VERSION should match ${VERSION}"
fi

# Update Main.PA_VERSION with the version tag if needed
sed -i  "s%\(static final private String PA_VERSION = \"\).\+\"%\1${VERSION}\"%" ${MASTER_DIR}/src/Core/org/objectweb/proactive/Main.java
(cd ${MASTER_DIR} ; ./compile/build deploy.all)
OUTPUT=$(${JAVA_HOME}/bin/java -cp ${MASTER_DIR}/dist/lib/ProActive.jar org.objectweb.proactive.api.PAVersion)
if [ "${OUTPUT}" != "${VERSION}" ] ; then
    warn_and_exit " [E] bad release version number: $OUTPUT"
fi


# Clean extra files
(
    cd ${MASTER_DIR}
    ./compile/build clean
    # Subversion
    find . -type d -a -name ".svn" -exec rm -Rf {} \;
    # Git
    rm -Rf .git
    # Remove non G/P/L stuff
    rm -Rf ./compile/lib/clover.*
    # Remove useless parts of ProActive
    rm ./doc/src/ProActiveRefBook.doc
    find . -type f -a -name "*.svg" -exec rm {} \; # svg are converted in png by hands
)

###########
# Build Source artifact

SRC_NAME=ProActiveProgramming-${VERSION}_core_src
SRC_DIR=${WORK_DIR}/${SRC_NAME}

cp -r ${MASTER_DIR} ${SRC_DIR} || die "Failed to create SRC_DIR"
(
    cd ${SRC_DIR} || die
    rm -Rf ./classes
    rm -Rf ./dist
)

# Publish artifacts
(
    cd ${WORK_DIR}
    tar cvfz ${ARTIFACT_DIR}/${SRC_NAME}.tar.gz ${SRC_NAME}
    zip -r   ${ARTIFACT_DIR}/${SRC_NAME}.zip    ${SRC_NAME}
)


###########
# Build Bin artifact

BIN_NAME=ProActiveProgramming-${VERSION}_core_bin
BIN_DIR=${WORK_DIR}/${BIN_NAME}

cp -r ${MASTER_DIR} ${BIN_DIR} || die "Failed to create BIN_DIR"
(
    cd ${BIN_DIR} || die
    ./compile/build -Dversion="${VERSION}" deploy.all              || die "build failed (code) "
    ./compile/build -Dversion="${VERSION}" doc.ProActive.manualPdf || die "build failed (documentation)"
    rm -Rf ./classes
    rm -Rf ./compile
    rm -Rf ./dev
    rm -Rf ./doc/toolchain ./doc/src
    rm -Rf ./lib
    rm -Rf ./scripts
    rm -Rf ./src
)

# Publish artifacts
(
    cd ${WORK_DIR}
    tar cvfz ${ARTIFACT_DIR}/${BIN_NAME}.tar.gz ${BIN_NAME}
    zip -r   ${ARTIFACT_DIR}/${BIN_NAME}.zip    ${BIN_NAME}
)



###########
# Cleanup

rm -Rf ${WORK_DIR}
