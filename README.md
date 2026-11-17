# Aurora Store

> **This is a fork** of [Aurora Store](https://auroraoss.com/) by AuroraOSS. It gives the phone UI a Play Store-style look, adds a Compose for TV interface for Android TV / Google TV, and includes a few bug fixes. Everything else works the same as upstream Aurora Store. It is not affiliated with AuroraOSS, so please report problems with this fork [here](https://github.com/aayy-dev/AuroraStore/issues), not to the upstream project.

Aurora Store enables you to search and download apps from the official Google Play store. You can check app descriptions, screenshots, updates, reviews, and download the APK directly from Google Play to your device. 

To use Aurora Store, log in using Google Play account, when you first open and configure Aurora Store.

Unlike a traditional app store, Aurora Store does not own, license or distribute any apps. All apps, app descriptions, screenshots and other content in Aurora Store are directly accessed, downloaded and/or displayed from Google Play. 

Aurora Store works exactly like a door or a browser, allowing you to log in to your Google Play account and find the apps from Google Play. 

*_Please note that Aurora Store does not have any approval, sponsorship or authorization from Google, Google Play, any apps downloaded through Aurora Store or any app developers; neither does Aurora Store have any affiliation, cooperation or connection with them._*


## Features

- FOSS: Has GPLv3 licence
- Beautiful design: Built upon latest Material 3 guidelines
- Account login: You can login with either personal or an anonymous account
- Device & Locale spoofing: Change your device and/or locale to access geo locked apps
- [Exodus Privacy](https://exodus-privacy.eu.org/) integration: Instantly see trackers in app
- [Plexus](https://plexus.techlore.tech/) integration: Instantly see app compatibility without Google Play Services or with microG
- Updates blacklisting: Ignore updates for specific apps
- Download manager
- Manual downloads: allows you to download older version of apps, provided
  - The APKs are available with Google
  - You know the version codes for older versions 

## Limitations

- The underlying API used is reversed engineered from the Google Play Store, changes on side may break it.
- Provides only base minimum features
  - Can not download or update paid apps.
  - Can not update apps/games with [Play Asset Delivery](https://developer.android.com/guide/playcore/asset-delivery)
- Multiple in-app features are not available if logged in as Anonymous.
  - Library
  - Purchase History
  - Editor's choice
  - Beta Programs
  - Review Add/Update
- Token dispenser server is not super reliable, downtimes are expected.  

## AI Usage

Aurora Store is developed with the help of AI tooling. Here is what that means.

**In this project**

- AI assistance is used in day to day development: writing and refactoring code,
  reviewing diffs, and drafting documentation.
- Nothing gets merged that a maintainer has not read, understood and tested.
  Generated code is a starting point, not a finished result.
- Accountability does not change. Whatever ships in Aurora Store is the
  maintainers' responsibility, no matter how it was written.

**In contributions**

- You are welcome to use AI tooling on your contributions. Just mention it in the
  merge request description.
- You are still responsible for what you send. Be ready to explain how your change
  works, and to show that you built and tested it yourself.
- Merge requests and issue reports that are plainly unreviewed model output (calls
  to APIs that do not exist, unrelated changes, made up bug reports) will be closed
  without review.

## Downloads

Get this fork from [GitHub Releases](https://github.com/aayy-dev/AuroraStore/releases). Each release has:

- a **release** APK (`com.aurora.store`), signed with this fork's own key, and
- a **debug** APK (`com.aurora.store.debug`), signed with the AOSP test key, which installs alongside any other build.

Because the release APK is signed with a different key, it can't be installed over upstream builds (official website, F-Droid, IzzyOnDroid, App Gallery), and they can't update it. Uninstall upstream Aurora Store first. Self-update is turned off in this fork for the same reason.

## Certificate Fingerprints

Release builds of this fork:

- SHA1: 43:83:8A:E7:B1:B8:D4:FD:B7:A5:D8:20:E0:48:3D:80:20:27:9B:61
- SHA256: C6:10:B7:27:08:EA:31:96:6C:63:2F:86:73:99:5B:EB:BA:1D:E0:78:BA:46:C9:9A:44:6F:69:BD:11:21:60:E0

## Support

For problems with this fork, open an issue on [GitHub](https://github.com/aayy-dev/AuroraStore/issues).
For general Aurora Store questions, visit [Aurora Wiki](https://gitlab.com/AuroraOSS/AuroraStore/-/wikis/home) for FAQs.

- [Telegram](https://t.me/AuroraSupport)
- [XDA Developers](https://forum.xda-developers.com/t/app-5-0-aurora-store-open-source-google-play-client.3739733/)

## Permissions

- `android.permission.INTERNET` to download and install/update apps from the Google Play servers
- `android.permission.ACCESS_NETWORK_STATE` to check internet availability
- `android.permission.FOREGROUND_SERVICE` to download apps without interruption
- `android.permission.FOREGROUND_SERVICE_DATA_SYNC` to download apps without interruption
- `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` to auto-update apps without interruption (optional)
- `android.permission.MANAGE_EXTERNAL_STORAGE` to access the OBB directory to download APK expansion files for games or large apps
- `android.permission.READ_EXTERNAL_STORAGE` to access the OBB directory to download APK expansion files for games or large apps
- `android.permission.WRITE_EXTERNAL_STORAGE` to access the OBB directory to download APK expansion files for games or large apps
- `android.permission.QUERY_ALL_PACKAGES` to check updates for all installed apps
- `android.permission.REQUEST_INSTALL_PACKAGES` to install and update apps
- `android.permission.REQUEST_DELETE_PACKAGES` to uninstall apps
- `android.permission.ENFORCE_UPDATE_OWNERSHIP` to silently update apps
- `android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION` to silently update apps
- `android.permission.POST_NOTIFICATIONS` to notify user about ongoing downloads, available updates, and errors (optional)
- `android.permission.USE_CREDENTIALS` to allow users to sign into their personal Google account via microG

## Screenshots

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot-01.png" height="400">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot-03.png" height="400">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot-07.png" height="400">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot-08.png" height="400">

## Translations

Don't see your preferred language? Click on the widget below to help translate Aurora Store!

<a href="https://hosted.weblate.org/engage/aurora-store/">
  <img src="https://hosted.weblate.org/widgets/aurora-store/-/287x66-grey.png" alt="Translation status" />
</a>

## Donations

You can support Aurora Store's development financially via options below. For more options, checkout the **About** page within the Aurora Store.

[![Liberapay](https://liberapay.com/assets/widgets/donate.svg)](https://liberapay.com/whyorean)
<a href="https://www.paypal.com/paypalme/AuroraDev">
  <img src="https://www.paypalobjects.com/webstatic/mktg/logo/AM_mc_vs_dc_ae.jpg" height="45" alt="PayPal">
</a>

## Project references

Aurora Store is based on these projects

- [YalpStore](https://github.com/yeriomin/YalpStore)
- [AppCrawler](https://github.com/Akdeniz/google-play-crawler)
- [Raccoon](https://github.com/onyxbits/raccoon4)
- [SAI](https://github.com/Aefyr/SAI)
