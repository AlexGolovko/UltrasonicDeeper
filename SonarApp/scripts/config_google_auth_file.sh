#!/bin/bash
mkdir secret
touch ./secret/google_auth_cred.json;
echo $GOOGLE_APPLICATION_CREDENTIALS_VALUE >> ./secret/google_auth_cred.json;
