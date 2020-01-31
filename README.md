# Flipper MMKV Viewer Plugin

[![](https://jitpack.io/v/ddyos/flipper-plugin-mmkv-viewer.svg)](https://jitpack.io/#ddyos/flipper-plugin-mmkv-viewer)
[![npm version](https://img.shields.io/npm/v/flipper-plugin-mmkv-viewer.svg)](https://www.npmjs.com/package/flipper-plugin-mmkv-viewer)

*[English](README.md) | [简体中文](README.zh-cn.md)*

## Introduction

A plugin for the debug tool [Flipper](https://fbflipper.com) that inspect the [MMKV](https://github.com/Tencent/MMKV) file of your native app.

It can view or edit the key-value inside the MMKV file.

![](https://user-images.githubusercontent.com/8358125/73119200-4ba6f300-3f99-11ea-8391-a22cc826b5ca.png)

Currently only Android is supported.

Note: 

MMKV did not implement OnSharedPreferenceChangeListener on Android, so you should update the data manually by press "Refresh" Button.

## Setup

### Flipper Desktop

1. Installed [Flipper Desktop](https://fbflipper.com)
2. Go to **Manage Plugins** by pressing the button in the lower left corner of the Flipper app, or in the **View** menu
3. Select **Install Plugins** and search for `mmkv-viewer`
4. Press the **Install** button

![](https://user-images.githubusercontent.com/8358125/73119201-52356a80-3f99-11ea-95b3-1a0013acbce8.png)

### Android

1. Add [Flipper Android SDK](https://github.com/facebook/flipper) to `build.gradle` on your app module:
```gradle
dependencies {
    // please use Latest Version
    debugImplementation 'com.facebook.flipper:flipper:0.30.1'
    debugImplementation 'com.facebook.soloader:soloader:0.8.0'
    releaseImplementation 'com.facebook.flipper:flipper-noop:0.30.1'
}
```
2. Add JitPack in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
}
```
3. Add this plugin library as a dependency in your app's build.gradle file:
```gradle
dependencies {
    // please use Latest Version
    debugImplementation 'com.github.ddyos:flipper-plugin-mmkv-viewer:1.0.0'
}
```
4. Init the plugin:
```Kotlin
val client = AndroidFlipperClient.getInstance(this)
client.addPlugin(MMKVFlipperPlugin("other_mmkv"))
client.start()
```

## Android Demo

See the projects in the [`sample`](/android/sample) folder.

## License

MIT License, as found in the [LICENSE](/LICENSE) file.
