---
layout: docs
title: Install Client
section: docs
---

# Installation

Prerequisites:

## SBT

* [Download](http://www.scala-sbt.org/download.html) and install sbt

## Android SDK

* [Download](https://developer.android.com/studio/index.html#downloads). You only need the command line tools.
* Set the `ANDROID_HOME` environment variable to point to the root folder.

## Android Device

You need an Android device and must [enable USB debugging](https://www.google.es/search?q=android+activate+developer+mode&oq=android+active+developer).

## Google Project

9 Cards needs the following Google APIs:

* Google Drive API: for storing a user's devices in the cloud
* Google Plus API: for authenticating the user requests

You need to create a project in the Google API Console with these two APIs enabled.

For that, you have two choices: 

* Normal Mode (Recommended): You must create the keys in Google Developer Console. You only need 10 minutes to complete this.
* Easy Mode: We give you the keys, and you don't have to create the project in the Google Developer Console.


## Normal Mode: Google Project

1. Go to [Google Developer Console](https://console.developers.google.com/apis/library?project=_)
2. From the Project drop-down, select a [project](https://support.google.com/cloud/answer/6158853), or create a new one.

### Google Drive API

1. Enable the Google Drive API service:
    1. In the sidebar under "API Manager," select *Library*.
    2. In the list of Google APIs, search for the Google Drive API service.
    3. Select Google Drive API from the results list.
    4. Press the Enable API button.
2. In the sidebar under "API Manager," select Credentials.
3. In the Credentials tab, select the *Create credentials* drop-down list, and choose OAuth client ID.
4. Select *Android* as the *Application type*.
5. Enter a key Name.
6. [Find your certificate SHA1 fingerprint](https://developers.google.com/android/guides/client-auth) and paste it in the form where requested.
7. Enter `com.fortysevendeg.ninecardslauncher` in the package name field.
8. Click on "Create."

[More info](https://developers.google.com/drive/android/auth)

### Google Plus API

1. Enable the Google Plus API service:
    1. In the sidebar under "API Manager," select *Library*.
    2. In the list of Google APIs, search for the Google+ API service.
    3. Select Google+ API from the results list.
    4. Press the Enable API button.
2. In the sidebar under "API Manager," select *Credentials*.
3. In the Credentials tab, select the *Create credentials* drop-down list, and choose OAuth client ID.
4. Select *Web application* as the *Application type*.
5. Enter a key Name then select Create.
6. Then copy the *client ID* of the newly generated credential.


## Easy Mode: Google Project

The only thing you need to do is add the following content to `ninecards.properties` file:

```
# Backend V2
backend.v2.url=https://nine-cards-stage.herokuapp.com
backend.v2.clientid=411191100294-sjhinp1i2gkp46u36ii7m16v9hog64nn.apps.googleusercontent.com
```

and you must launch SBT with the following command:

```
$ sbt -mem 2048 -Ddebug
```

At the end of the compilation, previously used for installing on a cellphone, you must put the password of the keystore:

```
Enter keystore password:
```

The password is `android`

Note: If you're interested in working on this project, please consider using the `Default Mode`.

# Compile and Run

To compile the project:

* Clone this GitHub project to your computer:

```
$ git clone git@github.com:47deg/nine-cards-v2.git
```

* Add a `ninecards.properties` file (See [Add Debug Keys](#properties-file) section)

* You need to set the heap size to at least 2M:

```
$ sbt -mem 2048
```

* Verify that your device is attached:

```
> devices
```

The output should look like:

```
[info]  Serial                 Model            Battery % Android Version
[info]  ---------------------- ---------------- --------- ---------------
[info]  XXXXXXXXXX             Nexus 6                66% 6.0.1  API 23
```

* Now you're ready to run the project, just execute:

```
> run
```

# Properties File

You need to add a `ninecards.properties` file in the project root folder. 

This file provides various keys for different third-party services. We'll see this below:

To begin with, you can use the template provided in the root folder:

```
$ cp ninecards.properties.default ninecards.properties
```

This is the content:

```
# Backend V2
backend.v2.url=
backend.v2.clientid=

# Third Parties
crashlytics.enabled=false
crashlytics.apikey=
crashlytics.apisecret=
strictmode.enabled=false
analytics.enabled=false
analytics.trackid=

# Firebase
firebase.enabled=false
firebase.url=
firebase.google.appid=
firebase.google.apikey=
firebase.gcm.senderid=
firebase.clientid=
```

## Backend V2 (Mandatory)

* `backend.v2.url`: Defines the URL for the Backend. Visit the [GitHub project](https://github.com/47deg/nine-cards-backend) for more information.
* `backend.v2.clientid`: This value is used for requesting a token id that will be used by the Backend to authenticate the user. It's the *client id* obtained in the [Google Plus API section](#google-plus-api). 

## Third-Parties (Optional)

**[Crashlytics](https://try.crashlytics.com/)**

* `crashlytics.enabled`: Enables or disables the Crashlytics service
* `crashlytics.apikey` & `crashlytics.apisecret`: These values are fetched from your [Crashlytics organization page](https://www.fabric.io/settings/organizations)

**[Strict Mode](https://developer.android.com/reference/android/os/StrictMode.html)**

* `strictmode.enabled`: Enables or disables the Strict Mode

**[Google Analytics](https://developers.google.com/analytics/)**

* `analytics.enabled`: Enables or disables the Google Analytics service
* `analytics.trackid`: You can use your own tracking ID. See how to [find your tracking code, tracking ID, and property number](https://support.google.com/analytics/answer/1032385)

## Google Firebase (Optional)

Google Firebase is used for push notifications.

**[Google Firebase](https://firebase.google.com/)**

1. Create a Firebase project in the [Firebase console](https://firebase.google.com/console/), if you don't already have one. If you already have an existing Google project associated with your mobile app, click Import Google Project. Otherwise, click Create New Project.
2. Add a new app in *Project Settings* -> *General*
3. Select the newly created app and download the `google-services.json`
4. Open the file in a text editor. All properties listed below are taken from this file: 

* `firebase.enabled`: Enables or disables the Google Firebase service
* `firebase.url`: Property `project_info.firebase_url`
* `firebase.google.appid`: Property `client[0].client_info.mobilesdk_app_id`
* `firebase.google.apikey`: Property `client[0].api_key[0].current_key`
* `firebase.gcm.senderid`: Property `project_info.project_number`
* `firebase.clientid`: Property `client[0].oauth_client[x].client_id` where x is the index of one element with `client_type` == 3
