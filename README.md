# TorVPN
{:.no_toc}

*WARNING* This is experimental software, do not rely on it for anything other than testing and development. It may leak information and should not be relied on for anything sensitive!

# Install debug version

A debug version of the app is built in each CI run, you can install it on your phone for debugging/development purposes.

## Downloading the apk

You can download the latest successfully built package from the [Gitlab package archive](https://gitlab.torproject.org/tpo/applications/vpn/-/packages). Once you have downloaded the apk, you will need to install it on your phone, please follow the [process for installation on your phone](#install-package-on-phone) to do so.

## Install

In order to install this unsigned package, you will need to have the command-line tool `adb` installed on your computer. On a Debian, or Debian-derived machine, you can `sudo apt install adb` to install it. If using something else, [follow a tutorial](https://www.xda-developers.com/install-adb-windows-macos-linux/) to get it installed.

## Enable Developer mode
On your Android device, you will need to have `Developer Mode` enabled. To do this, you have to follow a little bit of a secret pathway:

1. Open Settings on your phone
2. Then go to `About phone` (near the bottom of the list)
4. Tap `Build number` (bottom of list) *seven* times in a row.
5. When you have finished you will see a message saying it is enabled.
6. Now go back to the main Settings screen and you should see a new Developer options menu. On Google Pixel phones and some other devices, you might need to navigate to Settings > System to find the Developer options menu.
7. Go in there and enable the USB debugging option.

## Connect to your phone
Attach your phone to your computer via a USB cable. 

You should get a pop-up on your phone asking you if you want the device to connect. You will need to accept it (possibly a few times, unless you tell it to remember your device).

Then go to a shell and type `adb devices`, you should see something similar to the following:

```
List of devices attached
1e778e25        device
```

The number on the left will be unique to your device.

## Install the package
While in the directory that you unzipped the archive, install the application with `adb install app-debug.apk`.

Once you have installed the application, you should be able to find it in your normal list of applications.

You are finished! You do not need to follow the next section, if you are just wanting to install the apk from the CI. The following section is only if you wish to build it locally.

### Installation problems

There are a few edge cases where installation will not work, here are a few possibilities:

You may need to enable installation of apps from 'unknown sources' (unsigned apps):
`adb shell settings put secure install_non_market_apps 1`

If you get this error: `adb: failed to install tor-vpn-debug.apk: Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package org.torproject.vpn signatures do not match newer version; ignoring!]`, in that case you will need to remove the existing app, and then after do the install:

```
$ adb uninstall org.torproject.vpn
```

# Build and run a debug version
If you are only wanting to install the CI built apk, you do not need to follow this step, simply follow the process detailed above instead. This step is only if you wish to build the app locally, independent of the CI build.

To build the app the Android SDK is required and the command line tool ADB is recommended.

For now the build and installation steps are as easy as:
1. Get the repository:
```
    git clone https://gitlab.torproject.org/tpo/applications/vpn.git`
    cd vpn
```
2. create a [personal access token](https://docs.gitlab.com/ee/security/token_overview.html#personal-access-tokens) with the permission `read_api`
3. add the line `gitLabPrivateToken=<YOUR_PRIVATE_ACCESS_TOKEN>` to your `local.properties` file in your projects root directory. (Replace <YOUR_PRIVATE_ACCESS_TOKEN> ;))
4. Build the app: `./gradlew assembleDebug`
5. Install it on your phone: `adb -d install -t app/build/outputs/apk/debug/app-debug.apk`

Have fun!

# Instrumentation Testing

To run UI tests, you will need to prepare an emulator (or a physical device). Please make sure that animations are disabled, as they are a core reason for flaky test results.
To disable them, you will need to enable the Developer Mode in your emulator by opening the `Settings` app,
tapping on  `About emulated device` and 7 times on `Build number`.

Enter the Developer options by going back to the root of the settings app, tap on `System` -> `Developer Options`, scroll down to
`Window animation scale`, `Transition animation scale` and `Animator animation scale`. Set all of these options to `Animation off`.

It's now time to run the tests. Make sure your emulator is running and enter

```
/gradlew app:connectedDebugAndroidTest
```

into your console.

# License

This code is licensed under the [3-clause BSD license](https://opensource.org/license/bsd-3-clause/)
