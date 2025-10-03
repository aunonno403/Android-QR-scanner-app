# QR Scanner App

An Android application that scans QR codes and barcodes using the camera or from gallery images, with Firebase Authentication for user management.

## Features

- Real-time QR code/barcode scanning using camera
- Scan QR codes from gallery images
- User authentication (Login/Signup) with Firebase
- Display scan results
- Modern Material Design UI

## Setup Instructions

### Prerequisites

- Android Studio (Latest version recommended)
- Android SDK
- Firebase account

### Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app to your Firebase project
   - Package name: `com.example.qrscannerapp`
   - Download the `google-services.json` file
4. Place the downloaded `google-services.json` file in the `app/` directory
   - You can use `app/google-services.json.template` as a reference
   - Replace all placeholder values with your actual Firebase configuration

### Installation

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   ```

2. Open the project in Android Studio

3. Add your `google-services.json` file to the `app/` directory (see Firebase Setup above)

4. Sync the project with Gradle files

5. Run the app on an emulator or physical device

## Required Permissions

- Camera access (for scanning QR codes)
- Storage access (for reading images from gallery)

## Technologies Used

- Kotlin
- CameraX
- ML Kit Barcode Scanning
- Firebase Authentication
- Material Design Components
- ViewBinding

## Security Note

The `google-services.json` file contains sensitive API keys and is not included in the repository. You must obtain your own from Firebase Console.



