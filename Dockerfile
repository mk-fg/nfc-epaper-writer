## Dockerfile to build apk, fetching all decessary dependencies for that along the way
## Build/copy app-debug.apk to current dir with: docker build --output type=local,dest=. .

FROM debian:stable-20240722-slim AS build

RUN mkdir /build
WORKDIR /build

RUN echo >apt '#!/bin/sh' && chmod +x apt && echo >>apt \
	'export DEBIAN_FRONTEND=noninteractive; exec apt-get </dev/null' \
	'-o=Dpkg::Options::=--force-confold -o=Dpkg::Options::=--force-confdef' \
	'--assume-yes --quiet --no-install-recommends "$@"'
RUN ./apt update
RUN ./apt install openjdk-17-jre-headless
RUN ./apt install curl unzip git

RUN file=commandlinetools-linux-11076708_latest.zip \
	&& curl -fsLO https://dl.google.com/android/repository/"$file" && unzip "$file" && rm -f "$file"
RUN cd cmdline-tools && yes | ./bin/sdkmanager --licenses --sdk_root=.

RUN curl -fsL https://github.com/mk-fg/nfc-epaper-writer/archive/refs/heads/main.tar.gz \
	| tar -xzf- && ln -s nfc-epaper-writer-main nfc-epaper-writer

# First time build fails with following error:
#   Could not determine the dependencies of task ':app:compileDebugJavaWithJavac'.
#   > Failed to find target with hash string 'android-30' in: /build/cmdline-tools
# Probably some path issue, but just re-running it works - that's the workaround used here
RUN cd nfc-epaper-writer && ANDROID_HOME=/build/cmdline-tools bash gradlew build ||:
RUN cd nfc-epaper-writer && ANDROID_HOME=/build/cmdline-tools bash gradlew build

FROM scratch AS artifact
COPY --from=build /build/nfc-epaper-writer/app/build/outputs/apk/debug/app-debug.apk /
