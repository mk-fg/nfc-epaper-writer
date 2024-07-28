## Dockerfile to build apk, fetching all decessary dependencies for that along the way
## Build/copy app-debug.apk to current dir with: docker build --output type=local,dest=. .
## For older docker where build != buildx, run: docker buildx build --output type=local,dest=. .

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

# See https://stackoverflow.com/a/61176718 for reason behind renames at the end
RUN file=commandlinetools-linux-11076708_latest.zip \
	&& curl -fsLO https://dl.google.com/android/repository/"$file" \
	&& unzip "$file" && rm -f "$file" \
	&& mkdir android && mv cmdline-tools android/tools
RUN cd android && yes | ./tools/bin/sdkmanager --licenses --sdk_root=.

RUN commit=f75f046f9c158201bb07f02d0d9efd1f7375b20b \
	&& curl -fsL https://github.com/mk-fg/nfc-epaper-writer/archive/"$commit".tar.gz \
		| tar -xzf- && ln -s nfc-epaper-writer-"$commit" nfc-epaper-writer

RUN cd nfc-epaper-writer && ANDROID_HOME=/build/android bash gradlew build

FROM scratch AS artifact
COPY --from=build /build/nfc-epaper-writer/app/build/outputs/apk/debug/app-debug.apk /
