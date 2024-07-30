# NFC E-Ink Writer

Android application to generate/process images for and upload to
WaveShare Passive NFC-Powered (aka *parasitic*) E-Paper / E-Ink displays.

Contents:

- [Features](#hdr-features)
- [How to build and install this to an Android device]
- [Demos](#hdr-demos)
- [Known Issues](#hdr-known_issues)
- [Backlog](#hdr-backlog)
- [Technical Details from the original project]

It's only intended to interact with WaveShare displays through [their Android SDK],
and unlikely to work with similar displays from any other vendor, unless they are
intentionally designed to be compatible.\
WaveShare documentation explicitly says "This product is not compatible with
Samsung smartphones", but not sure why specifically - could be that it's just
their original app that doesn't work on these.\
This application requires Android 11+ OS (API level 30 or higher).

Passive NFC E-Paper devices supported by the app are typically intended for
commercial usage like shelf labels and product tags, other informational notes,
or smaller color ones also seem to be for decorative uses (like a chain/keyfob toy).\
Some of the compatible epaper device links: [1.54"], [2.13"], [2.7"], [2.9"], [4.2"], [7.5"], etc.

[How to build and install this to an Android device]:
  #hdr-how_to_build_and_install_this_to_an_andr.1gO6
[Technical Details from the original project]:
  #hdr-technical_details_from_the_original_project

![WaveShare 1.54" Passive NFC-Powered EInk Display, showing an image of car from Initial D anime](https://github.com/DevPika/nfc-epaper-writer-update/assets/19701790/4fab5732-7b0c-4a90-b7ff-1b47d66bbc96)

![WaveShare 2.9" Passive NFC-Powered EInk Display, showing the text "Hello World" with a waving hand emoji at the end](https://user-images.githubusercontent.com/17817563/118736344-32156480-b7f7-11eb-9a03-7d5b7c878c30.jpg)

This particular version of the app is a code fork of the original version,
written by Joshua Tzucker (in [joshuatz/nfc-epaper-writer] repository),
with fixes/features merged from other forks, aimed to fix some build and
UI issues that popped-up when I tried to use it, since first-party WaveShare
application straight-up doesn't work on my device.

Alternative URLs for this repository:

- <https://github.com/mk-fg/nfc-epaper-writer>
- <https://codeberg.org/mk-fg/nfc-epaper-writer>
- <https://fraggod.net/code/git/nfc-epaper-writer>

[their Android SDK]: https://www.waveshare.com/wiki/Android_SDK_for_NFC-Powered_e-Paper
[1.54"]: https://www.waveshare.com/1.54inch-nfc-powered-e-paper-bw.htm
[2.13"]: https://www.waveshare.com/2.13inch-nfc-powered-e-paper.htm
[2.7"]: https://www.waveshare.com/2.7inch-nfc-powered-e-paper-module.htm
[2.9"]: https://www.waveshare.com/2.9inch-nfc-powered-e-paper.htm
[4.2"]: https://www.waveshare.com/4.2inch-nfc-powered-e-paper.htm
[7.5"]: https://www.waveshare.com/7.5inch-nfc-powered-e-paper.htm
[joshuatz/nfc-epaper-writer]: https://github.com/joshuatz/nfc-epaper-writer


<a name=hdr-features></a>
## Features

Main application screen should contain following elements, top-to-bottom:

- Screen size selection (in top toolbar, remembered) -
  determines image size and crop/scaling.

- Re-upload last-used image box (with preview) - tap to immediately go do that.

- Select exact image to upload - for pre-made images
  of exactly right size that don't need any processing.

- Select/take and crop/scale/dither image - for using camera
  or image files of any size from any source.

  Cropping dialog will allow to pick part of the image with right aspect ratio for
  selected screen, which will then be scaled to its size, and converted to 3-color
  (BWR) image using color dithering (added in [DevPika/nfc-epaper-writer-update] fork).

- Create image from any composed text. Supports emojis.

- Draw image on the phone screen, using [JSPaint] as WYSIWYG editor.

App should work with all screen sizes supported by [WaveShare SDK].

> Not sure how well last two sources work, main use-case for mk-fg/nfc-epaper-writer
> fork is exact image upload - there are plenty of good tools to make/edit images.
>
> Floyd-Steinberg black-white-red (BWR) color dithering (merged from DevPika's fork)
> used for processing non-exact images might work suboptimally with 1-bit black-and-white
> displays - also didn't test it much here.

[DevPika/nfc-epaper-writer-update]:
  https://github.com/DevPika/nfc-epaper-writer-update/
[JSPaint]: https://jspaint.app/
[WaveShare SDK]: https://www.waveshare.com/wiki/Android_SDK_for_NFC-Powered_e-Paper


<a name=hdr-how_to_build_and_install_this_to_an_andr.1gO6></a>
## How to build and install this to an Android device

Steps below require any system with modern docker (aka [Docker Engine]) installed
(via e.g. `apt install docker` on ubuntu/debian linux), any terminal console app
available to also run its command-line tools from, ~5 GiB of peak memory and
about 3 GiB of disk space temporarily.

-   Copy or download [Dockerfile] from this repository into any directory.

    Doesn't really matter which dir, it'll only be used to copy build result
    (apk file) into by "docker build" command below.

    For example, to fetch it using curl from the command line:

    ```
    curl -OL https://raw.githubusercontent.com/mk-fg/nfc-epaper-writer/main/Dockerfile
    ```

-   Build and copy Android application package (APK) to the current directory:

    ```
    docker build --output type=local,dest=. .
    ```

    This will use Dockerfile as a recipe for `app-debug.apk`
    and will put it next to that Dockerfile afterwards.

    An older docker setup might give "unknown --output option" error,
    in which case [docker-buildx plugin] might be required, and command will be
    `docker buildx build --output type=local,dest=. .` instead of the one above.

-   Copy generated `app-debug.apk` file to an Android device, and open it there
    (e.g. find and tap on it in Files app).

-   Follow Android OS instruction popups from there on how to enable necessary
    settings to be able to install this tool from a sideloaded APK file.

    This type of app installation from an APK file is called "sideloading", and
    steps required to allow it change between Android versions, but should be
    easy to lookup for specific one on the internet, if Android's built-in prompts
    don't make it clear enough.

-   Installed tool should show up among the usual "application drawer" icons
    with "NFC E-Ink Writer" name and a generic "android robot head" icon.

Another, more developer-focused option, is to build APK using [Android Studio IDE],
which might be more or less complicated, depending on one's experience working
with such tools/ecosystem (vs command-line steps above) - open the project there,
and pick APK option from the Build menu.

[Dockerfile]: Dockerfile
[Android Studio IDE]: https://developer.android.com/studio
[Docker Engine]: https://docs.docker.com/engine/install/
[docker-buildx plugin]: https://www.baeldung.com/ops/docker-buildx


<a name=hdr-demos></a>
## Demos

These showcase application version from the original repository and displays
packaged separately from the control board.

- Flashing a local image file:

    ![Animated GIF showing this application letting a user select a local image file from their gallery, cropping it, and then flashing it to the EInk NFC display](https://user-images.githubusercontent.com/17817563/118732297-e2329f80-b7ee-11eb-9f5c-16b2872d6bf6.gif)

- Flashing Text:

    ![Animated GIF showing the user being able to input custom text, having the text captured as an image, and then flashing the resulting image to the EInk NFC Display](https://user-images.githubusercontent.com/17817563/118735056-7eab7080-b7f4-11eb-9d11-60d2aa58efe4.gif)

- Creating and flashing with a WYSIWYG editor:

    ![Animated GIF showing user creating a custom image via WYSIWYG paint editor, having the image captured to bitmap, and then flashing the generated bitmap to the EInk NFC Display](https://user-images.githubusercontent.com/17817563/118734322-ff696d00-b7f2-11eb-947d-dc844c259518.gif)


<a name=hdr-known_issues></a>
## Known Issues

NFC can be a little finnicky, and especially with these EInk displays.
Depending on the power and capabilities of your phone, it may take time
perfecting the exact distance and position to hold your phone in proximity
to the EInk display in order to get successful flashes.

On certain Android phones, you might also see a high rate of your NFC radio /
chipset randomly *"dying"*.
This happens at a lower level of system APIs, so it is really hard for my
application to detect or attempt to recover from.

> When detected by the lower-level APIs, Android will throw this as a
> `android.os.DeadObjectException`, with the entry:
> `NFC service dead - attempting to recover`.
> You can see the [internal recovery efforts here].

Additionally, sometimes you might see corrupted writes, where something goes
wrong during the transceiving process and the display ends up with random noise:

![Animated GIF showing a failed flash, with random noise appearing over the previously flashed image](https://user-images.githubusercontent.com/17817563/118723223-fde37900-b7e1-11eb-8b0c-c12ba4387d27.gif)

[internal recovery efforts here]:
  https://github.com/aosp-mirror/platform_frameworks_base/blob/9635abafa0053c65e04b93da16c72da8af371454/core/java/android/nfc/NfcAdapter.java#L831-L865


<a name=hdr-backlog></a>
## Backlog

- Better recovery methods for NFC adapter dying.

- When caching generated image, prefix or suffix with resolution,
  and then only allow cached image for re-upload if saved resolution matches.

- App Icon.

- Publish APK and/or provide better build options.


<a name=hdr-technical_details_from_the_original_project></a>
## Technical Details from the original project

(from the original [joshuatz/nfc-epaper-writer] repository)

Building this project was my first time touching Kotlin, Java, or Android APIs,
of which this project uses all three. I opted to go this route (native Android dev)
instead of React Native or Flutter, because I knew I was going to need access
to a lot of lower level APIs, and saw it as an opportunity to learn some new skills.

This project uses a bunch of different technologies, and takes some interesting "shortcuts":

- For the custom image generation options - both the text editor or WYSIWYG editor -
    I used WebView so that I could use HTML + JS + Web Canvas, and pass back the bitmap data to Android.

    - The WYSIWYG editor is actually just [JSPaint], but with injected
      JavaScript for capturing the bitmap data from the app's canvas.

    - The text editor is a custom tiny webpage I put together that renders
      the text to a Canvas element, and then captures the raw bitmap data.

- Local image option with processing uses [CanHub/Android-Image-Cropper] for cropping and resizing.

- By using scoped storage and the right APIs, no special permissions
  (other than NFC and Internet) are required from the User.

- For actually sending the bitmap data over the NFC protocol,
  this uses the [WaveShare Android SDK], and the JAR file that they provided.

- Kotlin coroutines are used throughout, as there are a lot of operations that are
  blocking in nature - main transceive operation is basically one long blocking sequence.

[CanHub/Android-Image-Cropper]: https://github.com/CanHub/Android-Image-Cropper
[WaveShare Android SDK]: https://www.waveshare.com/wiki/Android_SDK_for_NFC-Powered_e-Paper
