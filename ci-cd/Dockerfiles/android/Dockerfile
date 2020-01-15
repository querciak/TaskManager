FROM alpine:3.10

WORKDIR /application

ENV ANDROID_HOME /usr/local/android
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV SDK_TOOLS_VERSION sdk-tools-linux-4333796
ENV ANDROID_SDK_VERSION 26
ENV ANDROID_BUILD_TOOLS_VERSION 26.0.3
ENV PATH $PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/build-tools/$ANDROID_BUILD_TOOLS_VERSION

# Install glibc
RUN \
  wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
  wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-2.30-r0.apk -O /tmp/glibc.apk && \
  wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-bin-2.30-r0.apk -O /tmp/glibc-bin.apk && \
  apk add --no-cache /tmp/glibc.apk /tmp/glibc-bin.apk && \
  rm -rf /tmp/* && \
  rm -rf /var/cache/apk/*

# Install openjdk
RUN \
  apk update && \
  apk upgrade && \
  apk add --no-cache openjdk8 && \
  rm -rf /var/cache/apk/*

# Install android
RUN apk update && apk add --no-cache bash unzip libstdc++

RUN \
  wget -q http://dl.google.com/android/repository/${SDK_TOOLS_VERSION}.zip -O android-sdk-tools.zip && \
  unzip -q android-sdk-tools.zip -d ${ANDROID_HOME} && \
  rm -f android-sdk-tools.zip && \
  mkdir -p /root/.android/ && touch /root/.android/repositories.cfg && \
  yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses
RUN \
  sdkmanager --verbose "platform-tools" "platforms;android-${ANDROID_SDK_VERSION}" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"
RUN \
  sdkmanager --verbose "extras;android;m2repository" "extras;google;google_play_services" "extras;google;m2repository"
