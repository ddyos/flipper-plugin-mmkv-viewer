# Flipper MMKV 调试插件

[![](https://jitpack.io/v/ddyos/flipper-plugin-mmkv-viewer.svg)](https://jitpack.io/#ddyos/flipper-plugin-mmkv-viewer)
[![npm version](https://img.shields.io/npm/v/flipper-plugin-mmkv-viewer.svg)](https://www.npmjs.com/package/flipper-plugin-mmkv-viewer)

*[English](README.md) | [简体中文](README.zh-cn.md)*

## 简介

一个基于 [Flipper](https://fbflipper.com) 的调试插件，可以用于浏览原生应用中的 [MMKV](https://github.com/Tencent/MMKV) 文件。

插件可以查看和修改MMKV文件中的数据。

![](https://user-images.githubusercontent.com/8358125/73119200-4ba6f300-3f99-11ea-8391-a22cc826b5ca.png)

当前仅支持Android。

注意: 

由于MMKV for Android没有实现OnSharedPreferenceChangeListener接口, 所以需要通过“Refresh”按钮去刷新数据。

## 安装

### Flipper Desktop

1. 安装 [Flipper Desktop](https://fbflipper.com)
2. 通过 Flipper Desktop 左下角的 **Manage Plugins** 按钮 或者  **View**  菜单项， 进入插件管理页面
3. 选择 **Install Plugins** 选项卡，并搜索 `mmkv-viewer` 关键词
4. 通过 **Install** 按钮安装插件

![](https://user-images.githubusercontent.com/8358125/73119201-52356a80-3f99-11ea-95b3-1a0013acbce8.png)

### Android

1. 添加 [Flipper Android SDK](https://github.com/facebook/flipper) 到app模块的 `build.gradle` :
```gradle
dependencies {
    // please use Latest Version
    debugImplementation 'com.facebook.flipper:flipper:0.30.1'
    debugImplementation 'com.facebook.soloader:soloader:0.8.0'
    releaseImplementation 'com.facebook.flipper:flipper-noop:0.30.1'
}
```
2. 添加 JitPack源 到根目录的 build.gradle 中:
```gradle
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
}
```
3. 添加插件依赖到 app 的 build.gradle 中:
```gradle
dependencies {
    // please use Latest Version
    debugImplementation 'com.github.ddyos:flipper-plugin-mmkv-viewer:1.0.0'
}
```
4. 初始化插件:
```Kotlin
val client = AndroidFlipperClient.getInstance(this)
client.addPlugin(MMKVFlipperPlugin("other_mmkv"))
client.start()
```

## Android 示例

参见[`sample`](/android/sample) 文件夹中的项目。

## License

基于 MIT License, 参看 [LICENSE](/LICENSE) 文件。
