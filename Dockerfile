## Dockerfile to build apk, fetching all necessary dependencies for that along the way
## Build/copy app-debug.apk to current dir with: docker build --output type=local,dest=. .
## For older docker where build != buildx, install/run: docker buildx build --output type=local,dest=. .

FROM debian:stable-20240722-slim AS build

RUN echo >/apt '#!/bin/sh' && chmod +x /apt && echo >>/apt \
	'export DEBIAN_FRONTEND=noninteractive; exec apt-get </dev/null' \
	'-o=Dpkg::Options::=--force-confold -o=Dpkg::Options::=--force-confdef' \
	'--assume-yes --quiet --no-install-recommends "$@"'
RUN /apt update
RUN /apt install openjdk-17-jre-headless
RUN /apt install curl unzip git

# Nested homedir to be able to nuke it later
ENV HOME=/build/home UID=57839
RUN useradd -u $UID -d $HOME build \
	&& install -o build -g build -m700 -d ${HOME%/*} $HOME
WORKDIR $HOME
USER build

# See https://stackoverflow.com/a/61176718 for reason behind renames at the end
RUN file=commandlinetools-linux-11076708_latest.zip \
	&& curl --progress-bar -fLO https://dl.google.com/android/repository/$file \
	&& unzip $file && rm -f $file \
	&& mkdir android && mv cmdline-tools android/tools
RUN cd android && yes | ./tools/bin/sdkmanager --licenses --sdk_root=.

# gradle build will download a lot, which won't be reusable for other commits anyway
# So don't bother caching any of this in the build here
RUN commit=349582e832aa0cd5d386796d8119670baa21605f \
	&& curl --progress-bar -fL https://github.com/mk-fg/nfc-epaper-writer/archive/$commit.tar.gz | tar -xzf- \
	&& cd nfc-epaper-writer-$commit \
	&& ANDROID_HOME=$HOME/android bash gradlew build \
	&& cp app/build/outputs/apk/debug/app-debug.apk /build \
	&& rm -rf $HOME

FROM scratch AS artifact
COPY --from=build /build/app-debug.apk /
