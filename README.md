# TorVPN

## Build and run a debug version
To build the app the Android SDK is required and the command line tool ADB is recommended.

For now the build and installation steps are as easy as:
1. Get the repository:
```
    git clone https://gitlab.torproject.org/tpo/applications/vpn.git`
    cd vpn
```
2. Build the app: `./gradlew assembleDebug`
3. Install it on your phone: `adb -d install -t app/build/outputs/apk/debug/app-debug.apk`

Have fun!