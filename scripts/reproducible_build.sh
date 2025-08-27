#!/bin/bash

set -e

# Ensure executing in ...../vpn/
cd $(dirname "$0")
cd ..

ONIONMASQ_HASH=1.0.0Beta
TRANSLATION_HASH=bf2b2bde7276b6a9d329df2e00e282f4372b8d93

UNZIP=$(whereis unzip | wc | awk '{print $2}')
if [ $UNZIP -eq 1 ]; then
  echo "Error: unzip may be required, please install"
  exit 1
fi

CURL=$(whereis curl | wc | awk '{print $2}')
if [ $CURL -eq 1 ]; then
  echo "Error: curl may be required, please install"
  exit 1
fi

echo "Confirming Environment..."

if [ ! -d "bin/openlogic-openjdk-17.0.16+8-linux-x64/bin" ]; then
  echo "  Java 17 not found, attempting to download..."
  mkdir -p bin
  cd bin
  curl https://builds.openlogic.com/downloadJDK/openlogic-openjdk/17.0.16+8/openlogic-openjdk-17.0.16+8-linux-x64.tar.gz -o openjdk-17.0.16+8-linux-x64.tar.gz
  tar -xzf openjdk-17.0.16+8-linux-x64.tar.gz
  rm openjdk-17.0.16+8-linux-x64.tar.gz
  cd ..
fi

export PATH="$PWD/bin/openlogic-openjdk-17.0.16+8-linux-x64/bin/":$PATH
export JAVA_HOME="$PWD/bin/openlogic-openjdk-17.0.16+8-linux-x64"

echo "  Java 17 OK!"


export RUSTUP_HOME=$PWD/bin/rustup
export CARGO_HOME=$PWD/bin/cargo

if [ ! -d $RUSTUP_HOME ]; then

  echo "  rustup not installed, fetching..."
  echo ""

  mkdir -p bin
  curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs -o bin/rustup-init.sh
  chmod a+x bin/rustup-init.sh
  ./bin/rustup-init.sh -y
fi

. "$CARGO_HOME/env"


RUST_VER=$(rustup default  | cut -b 1-4)
if [ "$RUST_VER" != '1.87' ] ; then
  echo "  Rust 1.87: not found. Installing..."
  echo ""
  rustup default  1.87.0
  rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
fi

echo "  Rust 1.87 OK!"



if [ ! -d bin/Android/Sdk ]; then
  echo "  Android SDK not found. Installing..."
  echo ""
  mkdir -p bin/Android/Sdk/cmdline-tools
  pushd bin/Android/Sdk/cmdline-tools
  curl https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -o commandlinetools-linux-13114758_latest.zip
  # todo: if no unzip, fails, and further runs thinks the ndk is installed
  unzip -qq commandlinetools-linux-13114758_latest.zip
  mv cmdline-tools latest
  cd latest/bin
  yes | ./sdkmanager --licenses
  ./sdkmanager --install "platforms;android-34"
  ./sdkmanager --install "build-tools;34.0.0"
  popd
fi

export ANDROID_HOME=$PWD/bin/Android/Sdk

if [ ! -d $ANDROID_HOME/ndk/25.2.9519653 ]; then
  echo "  Android NDK not found. Installing..."
  echo ""
  pushd $ANDROID_HOME/cmdline-tools/latest/bin
  ./sdkmanager --install "ndk;25.2.9519653"
  popd
fi
export ANDROID_NDK_ROOT=$ANDROID_HOME/ndk/25.2.9519653
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/25.2.9519653



echo "  Android NDK 25.2.9519653 OK!"

echo "  Installing rust tools: bindgen-cli, cargo-ndk..."
cargo install bindgen-cli@0.71.1 --locked
cargo install cargo-ndk@3.5.4 --locked

echo "Env OK for reproducible build!"
echo ""

# Get Translations dep

mkdir -p deps
cd deps

if [ ! -d "translation" ]; then
  git clone https://gitlab.torproject.org/tpo/translation.git
fi

cd translation
git checkout tor-vpn
git pull
git checkout $TRANSLATION_HASH
cd ../..

# Get and build onionmasq

cd deps
if [ ! -d "onionmasq" ]; then
  git clone https://gitlab.torproject.org/tpo/core/onionmasq.git
fi

cd onionmasq
git fetch
git checkout -f $ONIONMASQ_HASH

# Since we have overridden $CARGO_HOME and $RUSTUP_DIR we need to inject additional remaps for them
#  Find EPOCH line, add 2, append 'i' while we are at it for sed cus it's easier in the pipeline
#  Then use sed to add the remap line adding it to the existing RUSTFLAGS
LINE=$(grep -n SOURCE_DATE_EPOCH build-ndk.sh |  cut -f1 -d':' | xargs -n 1 expr 2 + | xargs printf -- '%si' | xargs echo)
sed -i -e "$LINE\\
export RUSTFLAGS=\"\$RUSTFLAGS --remap-path-prefix $CARGO_HOME=/cargo/ --remap-path-prefix $RUSTUP_HOME=/rustup/\"" build-ndk.sh

cargo clean
# https://gitlab.torproject.org/tpo/core/arti/-/issues/1993
# Arti #1993: reproducible builds are not 
# currently open but the current guidance is still downgrade to this version of openssl
cargo update openssl-src --precise 300.2.3+3.2.1
./build-ndk.sh
cd android/OnionmasqAndroid
./gradlew :clean
./gradlew :onionmasq:build
./gradlew :onionmasq:publishLibraryReleasePublicationToMavenLocal
cd ../..

cd ../../

# Build VPN

# F-Droid contributed second line disable of remote maven repos
sed -i -e "s/gitlab.torproject.org.*'/maven.google.com'/" build.gradle

python3 scripts/get-l10n.py deps/translation

# Supposed to do to update but not reproducible over time yet
# So should be done and commited pre each release
./scripts/fetch_default_bridges.sh
./gradlew :clean

# ensure we're using the local maven repository to fetch the onionmasq artifacts while compiling tor-vpn
ENFORCE_LOCAL_MAVEN=1 ./gradlew assembleRelease
ENFORCE_LOCAL_MAVEN=1 ./gradlew bundleRelease
