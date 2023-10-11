How to start:  
* add local.properties with sdk.dir property
* add secret/google_auth_cred.json with key from
https://console.cloud.google.com/iam-admin/serviceaccounts/details/100250023380110836798/keys?project=ultrasonicdeeper&supportedpurview=project  
* add ALIAS, KEY_PASSWORD, KEY_STORE_PASSWORD env vars  
* add SONAR_TOKEN wit value from https://sonarcloud.io/project/configuration/GitHubOtherCI?id=alexgolovko_Android_DeepMapper
* use ./gradlew release to release apk to firebase