SMAUG Android Mobile Application
================================

This is the source code for the SMAUG android mobile application.

**IMPORTANT** This is a **PROTOTYPE** / **PROOF OF CONCEPT** and not
in any way of any "production" quality. This is intended solely as a
complement to allow testing and demonstrations of the SMAUG
marketplace and physical hardware, and hopefully will be helpful for
others as a *starting point* or for *learning*. Please also check the
`TODO` markings in the code as well as the [TODO](TODO.md) file.


# Prerequisites

Since this is an Android application, you will need Android SDK or
an IDE that contains the required SDK tools.

# Building

## Quickstart

Probably this could be automated. The main build --- generating an APK
--- is done with (results in `app/build/outputs/apk/debug`):

	$ ./gradlew assembleDebug

If you have a phone attached, you can compile and install debug
version of the application with:

	$ ./gradlew installDebug

This repository is also an **Android Studio** project, so you should
be able to open it in Android Studio directly as a project.

## Third party license update

To generate an up-to-date 3rd party license information, run the
`licenseDebugReport` or `licenseReleaseReport` task:

    $ ./gradlew licenseDebugReport

# License

## General License

Copyright 2020 Ericsson AB

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License.  You may
obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.

## Third-party licenses

Please see
[app/src/main/assets/open_source_licenses.html](app/src/main/assets/open_source_licenses.html)
for list of third-party libraries that are used in the application.

Other assets that are used in the project are:

* The dragon head image `app/src/main/res/drawable/ic_dragon_01.xml`
  is from freepik.com: [Chinese vector created by freepik -
  www.freepik.com](https://www.freepik.com/vectors/chinese), used with
  permission.

* Various icons in payment pending screen are from freepik as well

* Sad face: *Image: Flaticon.com. This cover has been designed using
  resources from Flaticon.com*
