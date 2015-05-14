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

# simple URL encode function
urlencode() {
 echo "${1}" | sed s/' '/'%20'/g | sed s/':'/'%3A'/g | sed s/"'"/'%27'/g
}

# calc duration
duration() {
  DURATION=$1

  SEC=$((DURATION%60))
  MIN=$(((DURATION/60)%60))
  HOR=$(((DURATION/(60*60))%24))
  DYS=$(((DURATION/(60*60*24))%31))
  MTS=$(((DURATION/(60*60*24*31))%12))

    echo $MTS Months
    echo $DYS Days
    echo $HOR Hours
    echo $MIN Minutes
    echo $SEC Seconds
}

ALEPH_FILE="${1}"
PROXY_FILE=$(basename "${2}")
PORTAL_SSHPUBKEY=$(basename "${3}")
PORTAL_HOST="${4}"
PORTAL_USER="${5}"
PORTAL_USERFIRSTNAME="${6}"
PORTAL_USERLASTNAME="${7}"
PORTAL_USRMAIL="${8}"
CLOUDMGR_URL="${9}"
VMUUID="${10}"
ALEPH_ALG="${11}"
PORTAL_NAME="${12}"
VMDURATION="${13}"
JOB_ID="${14}"
VMIPADDR=$(ifconfig | grep 'inet addr:' | head -n 1 | awk '{ print $2 }' | awk -F':' '{ print $2 }')
EXECUSR=alephusr
EXECHST=localhost
VM_OUTPUT=aleph.output
VMLOOPCTRL=/tmp/vmloop.ctrl
VMLOOPCOUNT=/tmp/vmloop.count
VMDURATIONSTR=$(duration $VMDURATION)
echo "--------------------------------------------"
echo "This is the execution of ALEPH job execution"
echo "--------------------------------------------"
echo "ALEPH_FILE       = ${ALEPH_FILE}"
echo "PROXY_FILE       = ${PROXY_FILE}"
echo "PORTAL_SSHPUBKEY = ${PORTAL_SSHPUBKEY}"
echo "PORTAL_HOST      = ${PORTAL_HOST}"
echo "VM IP ADDR       = ${VMIPADDR}"
echo "PORTAL_USER      = ${PORTAL_USER}"
echo "PORTAL_USRFNAME  = ${PORTAL_USERFIRSTNAME}"
echo "PORTAL_USRLNAME  = ${PORTAL_USERLASTNAME}"
echo "PORTAL_USRMAIL   = ${PORTAL_USRMAIL}"
echo "PORTAL_NAME      = ${PORTAL_NAME}"
echo "CLOUDMGR         = ${CLOUDMGR_URL}"
echo "VMUUID           = ${VMUUID}"
echo "ALEPH_ALG        = ${ALEPH_ALG}"
echo "VMDURATION       = ${VMDURATION}"
echo "JOB_ID           = ${JOB_ID}"
echo "EXECUSR          = ${EXECUSR}"
echo "EXECHST          = ${EXECHST}"
echo

#
# Setup PORTAL_SSHPUBKEY
#
cat $PORTAL_SSHPUBKEY >> $HOME/.ssh/authorized_keys
cat $PORTAL_SSHPUBKEY >> /home/$EXECUSR/.ssh/authorized_keys

CURL_CMD="curl"
CURL_OPT="-f"
CURL_CRT="--cert $PROXY_FILE --cacert $PROXY_FILE --capath $X509_CERT_DIR"

#
# Notify portlet info to cloudmgr service (only user VM case)
#
if [ "${VMUUID}" != "" ]; then
  for ((i=0; i<5; i++)); do
    echo "Notifying to cloudmgr service ... (Attempt #$((i+1)))"
    CLOUDMGR_QUERY="$CLOUDMGR_URL/register?portal_host=$PORTAL_HOST&portal_name=$(urlencode $PORTAL_NAME)&portal_user=$PORTAL_USER&vm_name=ALEPH2K&vm_ipaddr=$VMIPADDR&vm_uuid=$VMUUID&portal_jobid=$(urlencode $JOB_ID)"
    $CURL_CMD $CURL_OPT $CURL_CRT $CLOUDMGR_QUERY
    RES=$?
    if [ $RES -eq 0 ]; then
        echo "Notify to cloudmgr service successfully accomplished"
        break;
    else
        sleep 10 # Wait a while before to do a new attempt
    fi
  done
  if [ $RES -ne 0 ]; then
    echo "An error occurred contacting cloudmgr service"
    echo "QUERY:"
    echo "$CURL_CMD  $CURL_OPT $CURL_CRT $CLOUDMGR_QUERY"
    # The job will terminate; prepare the output
    tar cvf aleph_output.tar .
    exit 0 
  else
    echo "Notification successfully accomplished"
  fi
fi

#
# Updating CRLs 
#
printf  "Updating CRLs ... "
/usr/sbin/fetch-crl >/dev/null
echo "done"

#
# Downloading ALEPH file (if given)
#
if [ "${ALEPH_FILE}" != "" ]; then
  # Prepare proxy and assign to it the proper rights
  cp $PROXY_FILE /tmp/x509up_u$(id -u)
  chmod 600 /tmp/x509up_u$(id -u)
  # Starting download of the given file
  URL=$(irods2gsiftp $ALEPH_FILE)
  ALEPH_FILE_NAME=$(basename $URL)
  echo "Executing: globus-url-copy  $URL file://$PWD/$ALEPH_FILE_NAME"
  globus-url-copy  $URL file://$PWD/$ALEPH_FILE_NAME
  if [ $? -eq 0 ]; then
    echo "Copy successfully accomplished"
  else
    echo "Sorry, the copy of file $URL was not successful"
    touch aleph_output.tar # The empty file avoids the job to fail
    exit 0
  fi
fi

echo "Listing $PWD directory"
ls -lrt

VMLIFET=$VMDURATION
if [ "${ALEPH_ALG}" = "" ]; then
  #
  # If no algorithm is specified only ALEPHVM will be instantiated
  #

  # Create bash_history and the output directory
  ssh $EXECUSR@$EXECHST "touch .bash_history"
  ssh $EXECUSR@$EXECHST "mkdir ${VM_OUTPUT}"

  # Sending information to the user
  echo "VM instantiated; sending login information to the user"
  RANDPASS=$(date +%s | md5sum | base64 | head -c 12 ; echo)
  echo "Generated random password for user $EXECUSR is $RANDPASS"
  echo $RANDPASS | passwd --stdin $EXECUSR 
  printf "%s\n%s\n" "$RANDPASS" "$RANDPASS" | vncpasswd /home/alephusr/.vnc/passwd

  # Store VM access information (SSH)
  CLOUDMGR_QUERY="$CLOUDMGR_URL/access?vm_uuid=$VMUUID&proto=ssh&port=22&workgroup=&username=alephusr&password=$RANDPASS"
  $CURL_CMD $CURL_OPT $CURL_CRT $CLOUDMGR_QUERY 
  RES=$?
  if [ $RES -eq 0 ]; then
      echo "Access information to cloudmgr service, successfully sent"
  else
      echo "WARNING: Could not add SSH access information to cloudmgr service"
      echo "$CURL_CMD $CURL_OPT $CURL_CRT $CLOUDMGR_QUERY"
  fi

  # Store VM access infotmation (VNC)
  CLOUDMGR_QUERY="$CLOUDMGR_URL/access?vm_uuid=$VMUUID&proto=vnc&port=5901&workgroup=&username=alephusr&password=$RANDPASS"
  $CURL_CMD $CURL_OPT $CURL_CRT $CLOUDMGR_QUERY
  RES=$?
  if [ $RES -eq 0 ]; then
      echo "Access information to cloudmgr service, successfully sent"
  else
      echo "WARNING: Could not add VNC access information to cloudmgr service"
      echo "$CURL_CMD $CURL_OPT $CURL_CRT $CLOUDMGR_QUERY"
  fi

  # Prepare email to send to the user
  cat > VMINFO.txt << EOF
<p>Dear <b>${PORTAL_USERFIRSTNAME} ${PORTAL_USERLASTNAME} (${PORTAL_USER})</b>,</p>

Welcome to the ALEPH Virtual Machine.<br/>
 
<p>You can connect this machine executing:<br/>
    <b>ssh ${EXECUSR}@${VMIPADDR} and the password is: '${RANDPASS}'</b>
</p>
<p>You may also access to its desktop environment through the VNC session:<br/>
    <b>vnc://${VMIPADDR}:5901 and the password is: '${RANDPASS}'</b>
</p>
<p>Please be informed that your virtual service will be available for the next:<br/> ${VMDURATIONSTR}. After this time the resource will be released automatically by the system.
<p>You can save any file you like just putting it or its alias into the ${VM_OUTPUT} folder
</p>
EOF
  # Send generated email to the user
  CLOUDMGR_QUERY="$CLOUDMGR_URL/notify"
  POST_PARAMS="email_from=noreply@cloudmgr.com&email_to=$PORTAL_USRMAIL&email_subj=ALEPH2K($VMUUID)"
  #curl --cert $PROXY_FILE --cacert $PROXY_FILE --capath /etc/grid-security/certificates -d "$POST_PARAMS" -d "email_body=$(cat VMINFO.txt)" "${CLOUDMGR_QUERY}"
  $CURL_CMD $CURL_OPT $CURL_CRT -d "$POST_PARAMS" -d "email_body=$(cat VMINFO.txt)" "${CLOUDMGR_QUERY}"
  RES=$?
  if [ $RES -ne 0 ]; then
      echo "WARNING: Could not notify access credentials to the user"
      echo "$CURL_CMD $CURL_OPT $CURL_CRT -d \"$POST_PARAMS\" \"email_body=$(cat VMINFO.txt)\" \"${CLOUDMGR_QUERY}\""
  else
      echo "The VM has been successfully notified to the user ($PORTAL_USRMAIL)"
  fi   

  # Wait for VM expiration
  echo "Entering in sleep mode for $VMLIFET seconds ..."
  touch $VMLOOPCTRL
  count=0
  while [ -f $VMLOOPCTRL ]; do
      sleep 1
      count=$((count+1))
      if [ $count -ge $VMLIFET ]; then
          rm -f $VMLOOPCTRL
      fi 
      echo $count"/"$VMLIFET > $VMLOOPCOUNT
  done
  echo "Sleep time expired; closing the VM ..."
  # The use of timer may be used for a second expiration and 
  # a possible new notification before to kill the VM

  # Further notification cannot be done because the proxy could be expired; anyhow
  # cloudmgr host could retrieve a newer

  # Listing ...
  echo
  echo "Listing again $PWD directory"
  ls -lrt
  echo
  echo "Listing the $EXECUSR home directory"
  ssh $EXECUSR@$EXECHST "/bin/ls -lrt"

  # At the end of the job the bash history will be send back to the user
  ssh $EXECUSR@$EXECHST "cat <<EOF > README.txt
#---------------------------------
# Aleph VM output (README.txt)
#---------------------------------
This is the description about the output of the aleph virtual machine.
The output just consists of the folder ${VM_OUTPUT} that users may use to save their own files before the VM expiration.
EOF"
  ssh $EXECUSR@$EXECHST "tar cvf aleph_output.tar README.txt ${VM_OUTPUT}"  
  scp $EXECUSR@$EXECHST:aleph_output.tar .
else
  echo "---------------------------------------------"
  echo " Executing file analisys                     "
  echo "---------------------------------------------"
  # Now start aleph file analisys
  # 1) File must be placed under $EXECUSR user
  echo "Copying file $ALEPH_FILE_NAME ..."
  scp $PWD/$ALEPH_FILE_NAME $EXECUSR@$EXECHST:
  # 2) Execute the analisys script
  echo "Executing analisys: ./RunGeneric.csh ${ALEPH_FILE_NAME} ${ALEPH_FILE_NAME}.hist ${ALEPH_ALG}"
  ssh $EXECUSR@$EXECHST "./RunGeneric.csh ${ALEPH_FILE_NAME} ${ALEPH_FILE_NAME}.hist ${ALEPH_ALG}"
  echo "Listing output after analisys"
  ssh $EXECUSR@$EXECHST "/bin/ls -lrt"
  # 3) Retrieve the ALEPH_ALG output files
  echo "Preparing output ..."
  ssh $EXECUSR@$EXECHST "tar cvf aleph_output.tar ${ALEPH_ALG}.* ${ALEPH_FILE_NAME}.hist"
  scp $EXECUSR@$EXECHST:aleph_output.tar .
  echo
  echo "Listing again $PWD directory"
  ls -lrt
fi
echo "Done"

