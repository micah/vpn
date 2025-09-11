#!/bin/bash

set -e

# Ensure executing in ...../vpn/
cd $(dirname "$0")
cd ..

echo "Fetching Environment..."

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