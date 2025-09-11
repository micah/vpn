#!/bin/bash

set -e

# Ensure executing in ...../vpn/
cd $(dirname "$0")
cd ..

ONIONMASQ_HASH=$(cat scripts/onionmasq.hash)
TRANSLATION_HASH=$(cat scripts/translations.hash)

IS_CONTAINER=0
if [ $(pwd) == "/build/vpn" ]; then
  IS_CONTAINER=1
fi

if [ $IS_CONTAINER -eq 0 ]; then
  # leading . to absorb env vars defined inside like CARGO_HOME
  . ./scripts/reproducible_fetch_deps.sh
fi

# Get Translations dep

mkdir -p deps
cd deps

if [ ! -d "translation" ]; then
  git clone https://gitlab.torproject.org/tpo/translation.git --branch tor-vpn  --single-branch
fi

cd translation
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

if [ $IS_CONTAINER -eq 1 ]; then
  # mirroring container state so replacement lines work
  export CARGO_HOME=/root/.cargo

sed -i -e "$LINE\\
export RUSTFLAGS=\"\$RUSTFLAGS --remap-path-prefix $CARGO_HOME=/cargo/  \"" build-ndk.sh

else

sed -i -e "$LINE\\
export RUSTFLAGS=\"\$RUSTFLAGS --remap-path-prefix $CARGO_HOME=/cargo/ --remap-path-prefix $RUSTUP_HOME=/rustup/\"" build-ndk.sh

fi

cargo clean
# https://gitlab.torproject.org/tpo/core/arti/-/issues/1993
# Arti #1993: reproducible builds are not
# currently open but the current guidance is still downgrade to this version of openssl
cargo update openssl-src --precise 300.2.3+3.2.1
./build-ndk.sh
cd android/OnionmasqAndroid
./gradlew clean
./gradlew :onionmasq:build
./gradlew :onionmasq:publishLibraryReleasePublicationToMavenLocal
cd ../..

cd ../../

# Build VPN

# temporarily "disable" local.properties so java sdk and api keys are not applied to the build
if [ -f local.properties ]; then
  mv local.properties local.properties.reproducible.pause
fi

# F-Droid contributed second line disable of remote maven repos
sed -i -e "s/gitlab.torproject.org.*'/maven.google.com'/" build.gradle

python3 scripts/get-l10n.py deps/translation

# Supposed to do to update but not reproducible over time yet
# So should be done and commited pre each release
./scripts/fetch_default_bridges.sh
./gradlew clean

# ensure we're using the local maven repository to fetch the onionmasq artifacts while compiling tor-vpn
ENFORCE_LOCAL_MAVEN=1 ./gradlew assembleRelease
ENFORCE_LOCAL_MAVEN=1 ./gradlew bundleRelease

# restore local.properties if moved
if [ -f local.properties.reproducible.pause ]; then
  mv local.properties.reproducible.pause local.properties
fi
# restore build.gradle after sed disabled remote repos
git checkout build.gradle

cp app/build/outputs/apk/release/app-release-unsigned.apk build
cp app/build/outputs/bundle/release/app-release.aab build