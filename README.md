# Tasuku (タスク)

[![Release](https://img.shields.io/github/v/release/rimehrab/Tasuku?display_name=tag&style=for-the-badge)](https://github.com/rimehrab/Tasuku/releases/latest)
[![Build Debug](https://img.shields.io/github/actions/workflow/status/rimehrab/Tasuku/build.yml?branch=main&style=for-the-badge)](https://github.com/rimehrab/Tasuku/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/rimehrab/Tasuku?style=for-the-badge)](/LICENSE)

Tasuku is a minimalist, Android to-do application built with Kotlin and Jetpack Compose. It follows the "Material Expressive" design, prioritizing a clean, modern interface and a seamless user experience.

## ✨ Key features
* **Material 3 Expressive design:** Sleek and modern UI with fluid motion, custom animations, and card-based layouts.
* **Task management & details:** Organize tasks with descriptions, due dates, due times, and customizable tags.
* **Tabs & trash recovery:** Separate views for pending and completed tasks, plus a trash bin to restore deleted tasks.
* **Dynamic color & theming:** Full support for Light, Dark, and System themes with dynamic Material You wallpaper palettes on Android 12+.
* **Offline-first:** All tasks are stored locally via Room Database — no accounts, tracking, or network required.

## ⬇️ Downloads
*The app is currently in early development. You can download the latest debug APK from the GitHub Actions artifacts.*

## 🎨 Screenshots
| ![Screenshot 1](fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) | ![Screenshot 2](fastlane/metadata/android/en-US/images/phoneScreenshots/2.png) | ![Screenshot 3](fastlane/metadata/android/en-US/images/phoneScreenshots/3.png) |
|--------------------------------------------------------------------------------|--------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| ![Screenshot 4](fastlane/metadata/android/en-US/images/phoneScreenshots/4.png) | ![Screenshot 5](fastlane/metadata/android/en-US/images/phoneScreenshots/5.png) | ![Screenshot 6](fastlane/metadata/android/en-US/images/phoneScreenshots/6.png) |
| ![Screenshot 7](fastlane/metadata/android/en-US/images/phoneScreenshots/7.png) | ![Screenshot 8](fastlane/metadata/android/en-US/images/phoneScreenshots/8.png) | ![Screenshot 9](fastlane/metadata/android/en-US/images/phoneScreenshots/9.png) |

## 🧰 Build instructions
1. Clone this repository:
```bash
git clone https://github.com/rimehrab/tasuku.git
```
2. Open the project in Android Studio Latest Beta.
3. Sync Gradle files and resolve dependencies.
4. Run the project on an emulator or a physical device.

## 🛠️ Tech Stack
* **Programming language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Design framework:** Material 3 Expressive
* **Navigation:** Jetpack Navigation 3
* **Architecture:** MVVM (ViewModel, Coroutines, StateFlow)
* **Database:** Room (SQLite)
* **CI/CD:** GitHub Actions

## 📧 Contact
Feel free to reach out with questions or suggestions:
* Email: contact@rimehrab.is-a.dev

## 🙌 Special thanks to:  
* [hotarunichijou](https://github.com/hotarunichijou) for the idea of it.
* [syntaxspin](https://github.com/syntaxspin) for the amazing icon.
