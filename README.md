# Bloodwork Tracker

A comprehensive Android app for tracking blood test results with German laboratory value support.

## Features

- **Complete Blood Value Database**: Supports all German laboratory values including:
  - Complete blood count (kleines & großes Blutbild)
  - Liver values (Leberwerte): ALT, AST, Gamma-GT, AP, Bilirubin
  - Kidney values (Nierenwerte): Kreatinin, GFR, Harnstoff, Harnsäure
  - Lipid panel (Blutfette): Cholesterin, LDL, HDL, Triglyceride
  - Thyroid (Schilddrüse): TSH, T3, T4, fT3, fT4
  - Vitamins & minerals: B12, D3, Folsäure, Eisen, Ferritin
  - Hormones: Testosterone, Cortisol, Insulin
  - Inflammation markers: CRP, BSG, PCT
  - Diabetes markers: HbA1c, Glucose, Insulin
  - And many more...

- **Smart Tracking**: 
  - Add blood test results by date
  - Visual charts showing value trends over time
  - Reference range indicators (green/yellow/red)
  - Constellation analysis for value combinations

- **Modern Design**:
  - Material Design 3
  - German + English language support
  - Dark/light theme support
  - Intuitive navigation

- **Privacy First**:
  - Local storage only (SQLite database)
  - No cloud sync required
  - Export to PDF for sharing with doctors

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Database**: Room (SQLite)
- **Charts**: MPAndroidChart
- **Architecture**: MVVM with Repository pattern
- **Material Design 3**

## Building the APK

### Using Android Studio

1. Clone the repository:
   ```bash
   git clone https://github.com/aureliansevenassets/bloodwork-tracker.git
   cd bloodwork-tracker
   ```

2. Open the project in Android Studio

3. Build > Build Bundle(s) / APK(s) > Build APK(s)

4. Find the APK in `app/build/outputs/apk/debug/app-debug.apk`

### Using Command Line

1. Clone and navigate to project:
   ```bash
   git clone https://github.com/aureliansevenassets/bloodwork-tracker.git
   cd bloodwork-tracker
   ```

2. Build the APK:
   ```bash
   ./gradlew assembleDebug
   ```

3. Find the APK in `app/build/outputs/apk/debug/app-debug.apk`

## Installing on Your Phone

### Method 1: Direct Installation (Recommended)

1. Copy the APK file to your phone (via USB, cloud storage, or email)
2. Open the APK file on your phone
3. Enable "Install from unknown sources" if prompted
4. Follow the installation prompts

### Method 2: ADB Installation

1. Enable USB Debugging on your phone:
   - Settings > Developer Options > USB Debugging
   - (If Developer Options is not visible: Settings > About Phone > tap "Build number" 7 times)

2. Connect your phone to computer via USB

3. Install using ADB:
   ```bash
   adb install app-debug.apk
   ```

## Screenshots

[Screenshots will be available after UI implementation]

## License

MIT License - see LICENSE file for details

## Contributing

Pull requests welcome! Please ensure all German laboratory values are properly researched and medically accurate.