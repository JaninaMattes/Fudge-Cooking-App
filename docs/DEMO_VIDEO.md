# Demo video of the application

This document answers one question: **which free tool records a scripted walk
through the Fudge application, without a telephone?**

The answer is yes, such tools exist. You do not have to write your own tool.

---

## Short answer

| Need | Tool | Command |
|---|---|---|
| Video with the name of each step, for other people to watch | **Maestro** | `make demo-video` |
| Video with no extra tool at all | **adb screenrecord** + Espresso | `MODE=adb make demo-video` |
| Video in the pipeline, with no local machine | **GitHub Actions** | the job `ui-test` in `.github/workflows/frontend.yml` |

All three work on an **emulator**. No telephone is necessary.

---

## The two parts of the problem

A demo video needs two things, and most tools give one of them only:

1. **A script of the interactions.** Something must tap the buttons. This is
   the part that Cypress does for a web page.
2. **A recording of the screen.** Something must write the picture to a file.

The table below says which part each tool gives.

---

## The tools of the market

### 1. Maestro — the nearest tool to Cypress

[Maestro](https://docs.maestro.dev/) writes the flow in YAML, in the same way
that Cypress writes a test in JavaScript. One command does both parts of the
problem:

```bash
maestro record --local tools/demo/flows/demo.yaml
```

The command runs the flow and writes an **MP4** file that shows the screen of
the application **and the name of each step**. The videos go to
`~/.maestro/tests` on macOS and on Linux, and to `%userprofile%\.maestro\tests`
on Windows. The option `--local` renders the video on your machine; without
it, Maestro sends the picture to the servers of mobile.dev.

A flow of this repository looks like this:

```yaml
appId: com.mobilesystems.feedme
---
- launchApp
- tapOn:
    id: "com.mobilesystems.feedme:id/email"
- inputText: "demo@fudge.app"
- tapOn: "Login"
```

**Good:** one tool for both parts; the YAML is short and easy to read; it
waits for the user interface without a `sleep`; it works with an emulator and
with a telephone; `maestro studio` writes the YAML while you use the
application.
**Not so good:** the step `record` is the only part that is not fully local in
the old mode. Use `--local`. Check the licence of Maestro before you use it in
a product, because the tool is free to use but the company also sells a cloud
service.

The flows of this repository are in [`tools/demo/flows/`](../tools/demo/flows).

### 2. adb screenrecord — already on your machine

The Android SDK holds a recorder. It needs no extra program:

```bash
adb shell screenrecord --time-limit 180 /sdcard/demo.mp4
adb pull /sdcard/demo.mp4
```

**Good:** no extra tool; it works on each emulator and on each telephone.
**Not so good:** three minutes for each file; it records no sound; it does
nothing by itself, so you need a second tool for the interactions.

This repository uses it together with the Espresso test
[`DemoWalkthroughTest`](../frontend/android-feedme-app/app/src/androidTest/java/com/mobilesystems/feedme/DemoWalkthroughTest.kt),
which does the steps of the demo. Run both with:

```bash
MODE=adb make demo-video
```

### 3. The recorder of the emulator

The Android emulator has its own recorder, in the menu of the window or
through `adb emu screenrecord start`. It writes an MP4 file or a **GIF**
file, which is useful for a README.

**Good:** it makes a GIF directly; no limit of three minutes.
**Not so good:** it works on the emulator only.

### 4. scrcpy

[scrcpy](https://github.com/Genymobile/scrcpy) (Apache 2.0, from Genymobile)
shows the screen of a device on your computer and records it:

```bash
scrcpy --record demo.mp4 --no-playback
```

**Good:** free software; high quality; you can also control the device with
the mouse while it records.
**Not so good:** it does not write a script of the interactions. It is the
best choice when a person does the steps by hand.

### 5. Espresso and UI Automator

[Espresso](https://developer.android.com/training/testing/espresso) is the
test framework of Google for the user interface. It belongs to the Android
SDK, so the project already has it.

**Good:** it runs in the pipeline; it reads the view identifiers of the
application, so a test is exact and stable.
**Not so good:** the test is Kotlin code and not a short YAML file; it records
no video, so it needs `screenrecord`.

### 6. Appium

[Appium](https://appium.io/) drives Android and iOS with the WebDriver
protocol. It is the largest tool of this list and many services support it.

**Good:** one script for Android and for iOS; a large ecosystem.
**Not so good:** the setup is long (a server, a driver, a client library) and
too large for one demo video.

### 7. Fastlane screengrab

[screengrab](https://docs.fastlane.tools/actions/screengrab/) makes
**screenshots** in each language of the application, not a video. It is the
right tool for the pictures of an app store, and the wrong tool for a video.

### 8. GitHub Actions with an emulator

The action
[`reactivecircus/android-emulator-runner`](https://github.com/ReactiveCircus/android-emulator-runner)
starts an emulator inside the pipeline. The job `ui-test` of
[`.github/workflows/frontend.yml`](../.github/workflows/frontend.yml) uses it,
records the screen and keeps the video as an artifact of the run. Each build
of the main branch therefore gets a new demo video, and nobody has to make one
by hand.

---

## Comparison

| Tool | Writes the script | Records the video | Licence | Works on an emulator | Effort |
|---|---|---|---|---|---|
| **Maestro** | yes (YAML) | yes (MP4, with the steps) | free to use | yes | low |
| **adb screenrecord** | no | yes (MP4, 3 min) | Apache 2.0 | yes | none |
| **Emulator recorder** | no | yes (MP4 and GIF) | Apache 2.0 | emulator only | none |
| **scrcpy** | no | yes (MP4) | Apache 2.0 | yes | low |
| **Espresso** | yes (Kotlin) | no | Apache 2.0 | yes | medium |
| **Appium** | yes (many languages) | with a plugin | Apache 2.0 | yes | high |
| **Fastlane screengrab** | yes | pictures only | MIT | yes | medium |

---

## The recommendation for this project

**Do not write your own tool.** The market gives everything that this project
needs:

1. **Maestro** for the demo video that goes into the README and into a
   portfolio. The flows are already in `tools/demo/flows/`.
2. **adb screenrecord with Espresso** as the second way. It needs no program
   that is not already in the Android SDK, so the pipeline always works.
3. **ffmpeg** to make a small GIF out of the MP4 file for the README. The
   script `record-demo.sh` does this step when ffmpeg is on the machine.

A tool of your own would have to solve the same problems again: the start of
the emulator, the wait for the user interface, the search for a view, the
format of the video. Maestro and the Android SDK solve them well.

---

## How to make the video

```bash
# 1. Start an emulator (one time)
emulator -list-avds
emulator -avd Pixel_5_API_30 &
adb wait-for-device

# 2. Start the backend, so that the screens hold data
make db-up
make backend-run      # in a second terminal

# 3. Record
make demo-video                       # with Maestro, when it is installed
MODE=adb make demo-video              # with the Android tools only
```

The script writes:

- `build/demo/fudge-demo.mp4` — the video
- `build/demo/fudge-demo.gif` — a small GIF for the README (needs ffmpeg)
- `build/demo/*.png` — the screenshots of the Maestro flows

To install Maestro:

```bash
curl -Ls https://get.maestro.mobile.dev | bash
```

---

## Sources

- [Maestro: recording your flow](https://docs.maestro.dev/cli/recording-your-flow)
- [Maestro: run your first test with the CLI](https://docs.maestro.dev/getting-started/maestro-studio-cli)
- [scrcpy on GitHub (Apache 2.0)](https://github.com/Genymobile/scrcpy)
- [Getting started with Maestro (Bitrise)](https://www.bitrise.io/blog/post/getting-started-with-maestro-the-new-mobile-ui-testing-framework-from-mobile-dev)
- [Maestro mobile testing: declarative YAML flows](https://helpmetest.com/blog/maestro-mobile-testing-flows/)
