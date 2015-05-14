#!/bin/bash
#
# ALEPH cloud job shell script
#

export MYPROXY_SERVER=myproxy.cineca.it
export X509_CERT_DIR=/etc/grid-security/certificates


# Function that tranfors a given irods URL into the corresponding gsiftp URL
irods2gsiftp() {
  echo $1 | sed s/'irods:'/'gsiftp:'/ | sed s/':1247'/':2811'/
}

ALEPH_FILE="${1}"
PROXY_FILE=$(basename $2)
echo "--------------------------------------------"
echo "This is the execution of ALEPH job execution"
echo "--------------------------------------------"
echo "ALEPH_FILE = ${ALEPH_FILE}"
echo "PROXY_FILE = ${PROXY_FILE}"
echo
# Update the CRLs
printf  "Updating CRLs ... "
/usr/sbin/fetch-crl >/dev/null
echo "done"

# Prepare proxy and assign to it the proper rights
cp $PROXY_FILE /tmp/x509up_u$(id -u)
chmod 600 /tmp/x509up_u0

# Starting download of the given file
URL=$(irods2gsiftp $ALEPH_FILE)
ALEPH_FILE_NAME=$(basename $URL)
echo "Executing: globus-url-copy  $URL file://$PWD/$ALEPH_FILE_NAME"
globus-url-copy  $URL file://$PWD/$ALEPH_FILE_NAME
if [ $? -eq 0 ]; then
  echo "Copy successfully accomplished"
else
  echo "Sorry, the copy of file $URL was not successful"
  exit 10
fi

echo "Listing $PWD directory"
ls -lrt

# Now start aleph file analisys
# ...

echo "Done"
