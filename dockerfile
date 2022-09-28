FROM ubuntu:20.04

ENV ANDROID_HOME="${PWD}/android-home" \
    ANDROID_COMPILE_SDK="32" \
    ANDROID_BUILD_TOOLS="32.0.0" \
    ANDROID_SDK_TOOLS="8092744" \
    ANDROID_NDK="20.1.5948944" \
    ANDROID_NDK_SHA1="d903fdf077039ad9331fb6c3bee78aa46d45527b" \
    ANDROID_CLT_SH256="d71f75333d79c9c6ef5c39d3456c6c58c613de30e6a751ea0dbd433e8f8b9cbf" \
    ANDROID_NDK_VERSION="r20b" \
    JAVA_VERSION="jdk-11.0.11+9"

RUN install -d $ANDROID_HOME \
    apt-get clean && \
    apt-get --quiet update --yes && \
    DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata && \
    apt-get --quiet install --yes apt-utils wget tar unzip lib32stdc++6 lib32z1 build-essential curl git pkg-config libssl-dev

#Java
RUN set -eux; \
    ARCH="$(dpkg --print-architecture)"; \
    case "${ARCH}" in \
       aarch64|arm64) \
         ESUM='4966b0df9406b7041e14316e04c9579806832fafa02c5d3bd1842163b7f2353a'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_aarch64_linux_hotspot_11.0.11_9.tar.gz'; \
         ;; \
       armhf|armv7l) \
         ESUM='2d7aba0b9ea287145ad437d4b3035fc84f7508e78c6fec99be4ff59fe1b6fc0d'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_arm_linux_hotspot_11.0.11_9.tar.gz'; \
         ;; \
       ppc64el|ppc64le) \
         ESUM='945b114bd0a617d742653ac1ae89d35384bf89389046a44681109cf8e4f4af91'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_ppc64le_linux_hotspot_11.0.11_9.tar.gz'; \
         ;; \
       s390x) \
         ESUM='5d81979d27d9d8b3ed5bca1a91fc899cbbfb3d907f445ee7329628105e92f52c'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_s390x_linux_hotspot_11.0.11_9.tar.gz'; \
         ;; \
       amd64|x86_64) \
         ESUM='e99b98f851541202ab64401594901e583b764e368814320eba442095251e78cb'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.11_9.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
    curl -LfsSo /tmp/openjdk.tar.gz ${BINARY_URL}; \
    echo "${ESUM} */tmp/openjdk.tar.gz" | sha256sum -c -; \
    mkdir -p /opt/java/openjdk; \
    cd /opt/java/openjdk; \
    tar -xf /tmp/openjdk.tar.gz --strip-components=1; \
    rm -rf /tmp/openjdk.tar.gz;

ENV JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:$PATH"

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
    && wget -nv --output-document=ndk.zip https://dl.google.com/android/repository/android-ndk-${ANDROID_NDK_VERSION}-linux-x86_64.zip \
    && unzip -qq -d ndk ndk.zip \
    && rm -f ndk.zip \
    && mv ndk/android-ndk-r20b/ ndk/${ANDROID_NDK}/ \
    && cd ..
  
# Exporting required ndk paths
ENV ANDROID_NDK_HOME=$ANDROID_HOME/ndk/${ANDROID_NDK} \
    ANDROID_NDK=$ANDROID_HOME/ndk/${ANDROID_NDK} \
    LLVM_PREBUILD=$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64/bin \
    PATH=$ANDROID_NDK_HOME:$PATH \
    PATH=$ANDROID_NDK:$PATH \
    PATH=$LLVM_PREBUILD:$PATH

# Get Android sdk
RUN wget -nv --output-document=$ANDROID_HOME/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip \
    && cd $ANDROID_HOME \
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
    


