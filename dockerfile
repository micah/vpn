FROM registry.0xacab.org/leap/docker/debian:bookworm_amd64

ENV ANDROID_HOME="${PWD}/android-home" \
    ANDROID_COMPILE_SDK="34" \
    ANDROID_BUILD_TOOLS="34.0.0" \
    ANDROID_SDK_TOOLS="10406996" \
    ANDROID_NDK_VERSION="25.2.9519653" \
    ANDROID_NDK_SHA256="769ee342ea75f80619d985c2da990c48b3d8eaf45f48783a2d48870d04b46108" \
    ANDROID_CLT_SHA256="8919e8752979db73d8321e9babe2caedcc393750817c1a5f56c128ec442fb540" \
    ANDROID_NDK_RELNAME="r25c"

RUN install -d $ANDROID_HOME \
    apt-get clean && \
    apt-get --quiet update --yes && \
    DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata && \
    apt-get --quiet install --yes apt-utils wget tar unzip lib32stdc++6 lib32z1 build-essential curl git pkg-config libssl-dev openjdk-17-jdk

#Java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    PATH="${JAVA_HOME}/bin:${PATH}"

# Update new packages
RUN apt-get update

# Get Rust
RUN curl https://sh.rustup.rs -sSf | bash -s -- -y

ENV PATH="/root/.cargo/bin:${PATH}"

RUN rustup install "stable" \
    && rustup default stable \
    && rustup target add armv7-linux-androideabi aarch64-linux-android i686-linux-android x86_64-linux-android \
    && rustup show

# Get Android ndk
RUN cd $ANDROID_HOME \
    && wget -nv --output-document=ndk.zip https://dl.google.com/android/repository/android-ndk-${ANDROID_NDK_RELNAME}-linux.zip \
    && echo "${ANDROID_NDK_SHA256} ndk.zip" | sha256sum --strict -c - \
    && unzip -qq -d ndk ndk.zip \
    && rm -f ndk.zip \
    && mv ndk/android-ndk-${ANDROID_NDK_RELNAME}/ ndk/${ANDROID_NDK_VERSION}/ \
    && cd ..

# Exporting required ndk paths
ENV ANDROID_NDK_HOME=$ANDROID_HOME/ndk/${ANDROID_NDK_VERSION} \
    ANDROID_NDK=$ANDROID_HOME/ndk/${ANDROID_NDK_VERSION} \
    LLVM_PREBUILD=$ANDROID_NDK_VERSION/toolchains/llvm/prebuilt/linux-x86_64/bin \
    PATH=$ANDROID_NDK_HOME:$PATH \
    PATH=$ANDROID_NDK_VERSION:$PATH \
    PATH=$LLVM_PREBUILD:$PATH

# Get Android sdk
RUN wget -nv --output-document=$ANDROID_HOME/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip \
    && cd $ANDROID_HOME \
    && echo "${ANDROID_CLT_SHA256} cmdline-tools.zip" | sha256sum --strict -c - \
    && unzip -qq -d cmdline-tools cmdline-tools.zip \
    && rm -f cmdline-tools.zip \
    && cd ..

# Exporting required sdk paths
ENV PATH=$PATH:${ANDROID_HOME}/cmdline-tools/cmdline-tools/bin/
    # Accepting licences before using sdkmanager
RUN yes | sdkmanager --licenses \
    && sdkmanager --sdk_root=${ANDROID_HOME} "platforms;android-${ANDROID_COMPILE_SDK}" \
    && sdkmanager --sdk_root=${ANDROID_HOME} "platform-tools" \
    && sdkmanager --sdk_root=${ANDROID_HOME} "build-tools;${ANDROID_BUILD_TOOLS}"
    


