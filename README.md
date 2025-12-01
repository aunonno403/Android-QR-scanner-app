# QR Scanner App

A feature-rich QR code scanner app built with Kotlin for Android, featuring Firebase integration for cloud-based scan history and real-time synchronization across devices.

## 🚀 Features

✅ **Real-Time QR Scanning** - Camera-based scanning with ML Kit  
✅ **Gallery Scanning** - Scan QR codes from existing images  
✅ **Firebase Authentication** - Secure user login/signup  
✅ **Cloud Scan History** - Store scans on Firebase Realtime Database  
✅ **Flashlight Toggle** - Built-in flashlight control for low-light scanning  
✅ **URL Handling** - Open URLs in WebView without leaving the app  
✅ **Copy/Share** - Easy content sharing to other apps  
✅ **Multi-Device Sync** - Access scan history from any device  
✅ **Smart Detection** - Auto-detect URL, text, email, phone types  
✅ **Material Design UI** - Modern and intuitive interface  

## 📖 Documentation

### Quick Start
- **[QUICK_SETUP.md](QUICK_SETUP.md)** - Get up and running in 5 minutes

### Implementation Guides
- **[FIREBASE_IMPLEMENTATION_GUIDE.md](FIREBASE_IMPLEMENTATION_GUIDE.md)** - Complete Firebase setup and troubleshooting
- **[FEATURE_IMPLEMENTATION_GUIDE.md](FEATURE_IMPLEMENTATION_GUIDE.md)** - Add new features with code examples
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Technical overview and architecture

## ⚠️ Important: Firebase Database Setup Required

**Your database isn't storing data because the Firebase Realtime Database hasn't been created yet.**

### Quick Fix (2 minutes):

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: **qr-scanner-app-12868**
3. Click **"Realtime Database"** → **"Create Database"**
4. Choose **"Test mode"** for development
5. Click **"Enable"**

📖 **Detailed instructions:** See [FIREBASE_IMPLEMENTATION_GUIDE.md](FIREBASE_IMPLEMENTATION_GUIDE.md)

## 🛠️ Technologies Used

- **Kotlin** - Primary programming language
- **Firebase Auth** - User authentication
- **Firebase Realtime Database** - Cloud data storage
- **ML Kit Barcode Scanning** (v17.3.0) - QR code detection
- **CameraX** (v1.5.0) - Modern camera API
- **RecyclerView** - Efficient list display
- **Material Design 3** - Modern UI components
- **ViewBinding** - Type-safe view access

## 📂 Project Structure

```
app/
├── src/main/java/com/example/qrscannerapp/
│   ├── MainActivity.kt              # Main scanner screen (camera, flash, gallery)
│   ├── LoginActivity.kt             # User authentication
│   ├── SignupActivity.kt            # User registration
│   ├── WebViewActivity.kt           # In-app URL viewer
│   ├── ScanHistoryActivity.kt       # Cloud-synced scan history
│   ├── models/
│   │   └── ScanHistory.kt           # Data model for scans
│   ├── repository/
│   │   └── ScanHistoryRepository.kt # Firebase CRUD operations
│   └── adapter/
│       └── ScanHistoryAdapter.kt    # RecyclerView adapter
└── src/main/res/
    ├── layout/                      # XML layouts
    └── values/                      # Strings, colors, themes

Documentation/
├── QUICK_SETUP.md                   # Quick start guide
├── FIREBASE_IMPLEMENTATION_GUIDE.md # Firebase setup & troubleshooting
├── FEATURE_IMPLEMENTATION_GUIDE.md  # Add new features (QR generator, etc.)
└── IMPLEMENTATION_SUMMARY.md        # Technical architecture overview
```

## 🎯 Getting Started

### Prerequisites

- Android Studio (latest version)
- Android device or emulator (API 24+)
- Google account (for Firebase)
- Internet connection

### Installation

1. **Clone or Open Project**
   ```bash
   # Open this folder in Android Studio
   ```

2. **Create Firebase Realtime Database**
   - Follow instructions in [FIREBASE_IMPLEMENTATION_GUIDE.md](FIREBASE_IMPLEMENTATION_GUIDE.md)
   - Or see "Quick Fix" section above

3. **Update google-services.json**
   - Download updated file from Firebase Console
   - Replace `app/google-services.json`

4. **Build & Run**
   ```bash
   # Windows PowerShell
   .\gradlew assembleDebug
   ```
   Or click **Run ▶️** in Android Studio

📖 **Full setup guide:** See [QUICK_SETUP.md](QUICK_SETUP.md)

## 📱 How to Use

### 1. Sign Up / Login
- Create account with email/password
- Or sign in to existing account

### 2. Scan QR Codes
- Point camera at QR code → Auto-detects
- Click **Gallery** button to scan from images
- Click **Flash** button for low-light scanning

### 3. View Results
- **URLs:** Click to open in WebView
- **Text/Email/Phone:** Click for copy/share options

### 4. Access History
- Click **History** button to view all scans
- Synced across all your devices
- Delete individual or all scans

## 🔧 Adding New Features

Want to add more features? Check out the **[FEATURE_IMPLEMENTATION_GUIDE.md](FEATURE_IMPLEMENTATION_GUIDE.md)** for step-by-step tutorials:

### Beginner-Friendly Features
- ⭐⭐ **QR Code Generator** - Create QR codes from text/URLs
- ⭐⭐ **Export History** - Save as CSV or JSON
- ⭐⭐ **Dark Mode** - Night theme support
- ⭐⭐ **Search & Filter** - Find scans easily

### Intermediate Features
- ⭐⭐⭐ **User Profile** - Avatar upload, display name
- ⭐⭐⭐ **Batch Scanning** - Scan multiple codes at once
- ⭐⭐⭐ **Custom QR Styling** - Colorful QR codes with logos

Each feature includes:
- Complete code examples
- YouTube tutorial links
- Learning resources
- Best practices

## 🐛 Troubleshooting

### Database Not Storing Data

**Problem:** Scans aren't appearing in Firebase Console  
**Cause:** Realtime Database not created yet  
**Solution:** Follow [FIREBASE_IMPLEMENTATION_GUIDE.md](FIREBASE_IMPLEMENTATION_GUIDE.md) Step 1

### Camera Not Working

**Problem:** Black screen or no permission  
**Solution:** 
- Go to Settings → Apps → QR Scanner → Permissions → Enable Camera
- Restart the app

### Build Errors

**Problem:** Gradle sync fails  
**Solution:**
1. File → Invalidate Caches & Restart
2. Build → Clean Project
3. Build → Rebuild Project

### App Crashes

**Problem:** App crashes on launch or scan  
**Solution:**
- Check **Logcat** for error messages
- Filter by tags: `ScanHistoryRepository`, `MainActivity`
- Ensure user is logged in before scanning

📖 **Full troubleshooting guide:** See [FIREBASE_IMPLEMENTATION_GUIDE.md](FIREBASE_IMPLEMENTATION_GUIDE.md)

## 📊 Firebase Structure

### Database Schema

```json
{
  "scan_history": {
    "<user-id>": {
      "<scan-id>": {
        "id": "string",
        "userId": "string",
        "content": "string",
        "type": "URL|TEXT|EMAIL|PHONE",
        "timestamp": "long",
        "displayText": "string"
      }
    }
  }
}
```

### Security Rules

```json
{
  "rules": {
    "scan_history": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

- Each user can only access their own scans
- Requires authentication
- Data is completely isolated per user

## 🔐 Security

### Current Security Measures
- ✅ Firebase Authentication required
- ✅ User-scoped database rules
- ✅ No public data access
- ✅ Secure data transmission (HTTPS)

### Before Publishing
- [ ] Update to production Firebase rules
- [ ] Enable ProGuard/R8 obfuscation
- [ ] Add API key restrictions
- [ ] Test on multiple devices
- [ ] Add rate limiting

## 🚀 Roadmap

### Version 1.1 (Planned)
- [ ] QR Code Generator
- [ ] User Profile Management
- [ ] Export Scan History (CSV/JSON)
- [ ] Search & Filter History

### Version 1.2 (Future)
- [ ] Dark Mode
- [ ] Multiple Languages (i18n)
- [ ] Batch Scanning
- [ ] Custom QR Code Styling
- [ ] Analytics Dashboard

### Version 2.0 (Long-term)
- [ ] Offline Mode with sync
- [ ] QR Code Analytics
- [ ] Favorites/Bookmarks
- [ ] Share History with others
- [ ] Widget support

## 📚 Learning Resources

### YouTube Channels (Recommended)
- [Coding in Flow](https://www.youtube.com/c/CodinginFlow) - Kotlin & Android
- [Philipp Lackner](https://www.youtube.com/c/PhilippLackner) - Modern Android
- [Stevdza-San](https://www.youtube.com/c/StevdzaSan) - Firebase tutorials
- [Android Developers](https://www.youtube.com/c/AndroidDevelopers) - Official

### Documentation
- [Android Developer Docs](https://developer.android.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)

### Community
- [Stack Overflow - Android](https://stackoverflow.com/questions/tagged/android)
- [Reddit - r/androiddev](https://reddit.com/r/androiddev)
- [Android Developers Discord](https://discord.gg/androiddev)

## 🤝 Contributing

This is a learning project! Feel free to:
- Fork and experiment
- Add new features
- Improve documentation
- Share your modifications

## 📄 License

This project is for educational purposes. Free to modify and use for learning.

## 🆘 Support

Need help? Check these in order:

1. 📖 **Read the guides** (MD files in project root)
2. 🔍 **Check Logcat** for error details
3. 🔧 **Try Clean & Rebuild** project
4. 💬 **Search Stack Overflow** for similar issues
5. 🐛 **Check GitHub Issues** (if applicable)

## 📞 Key Contacts

- **Firebase Project:** qr-scanner-app-12868
- **Package Name:** com.example.qrscannerapp
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36

## 🎓 Skills You'll Learn

By working with this project, you'll gain experience with:

- ✅ Kotlin programming
- ✅ Android app architecture
- ✅ Firebase integration (Auth + Database)
- ✅ CameraX API
- ✅ ML Kit integration
- ✅ RecyclerView & Adapters
- ✅ Repository pattern
- ✅ Material Design
- ✅ Permissions handling
- ✅ Async operations & callbacks

## 🏆 Achievements Unlocked

- [x] Implement real-time QR scanning
- [x] Integrate Firebase Authentication
- [x] Build cloud-synced database
- [x] Add flashlight toggle
- [x] Create scan history feature
- [x] Implement WebView for URLs
- [x] Add comprehensive logging
- [ ] Create QR code generator *(Next!)*
- [ ] Build user profile system
- [ ] Add export functionality

---

**Built with ❤️ for learning Android development**

**Last Updated:** November 2025  
**Status:** Active Development  
**Version:** 1.0
├── IMPLEMENTATION_SUMMARY.md - Implementation overview
├── FIREBASE_IMPLEMENTATION_GUIDE.md - Firebase learning guide
└── FEATURE_IMPLEMENTATION_GUIDE.md - Future features guide
```

## 🔧 Setup Instructions

### Prerequisites
- Android Studio (latest version)
- Android device or emulator (API 24+)
- Firebase account

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/QRscannerapp.git
   cd QRscannerapp
   ```

2. **Open in Android Studio**
   - File → Open → Select project folder

3. **Configure Firebase**
   - Already configured! (`google-services.json` included)
   - **IMPORTANT**: Set Firebase Security Rules (see below)

4. **Sync Gradle**
   - File → Sync Project with Gradle Files

5. **Run the app**
   - Click Run ▶️ button
   - Select device/emulator

### Firebase Security Rules

**🔒 CRITICAL: Set these rules in Firebase Console!**

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select project: **qr-scanner-app-12868**
3. Navigate to: **Realtime Database** → **Rules**
4. Replace with:

```json
{
  "rules": {
    "scan_history": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

5. Click **Publish**


## 🎯 How to Use

### Scanning QR Codes
1. Launch the app and login
2. Point camera at QR code
3. Code is automatically scanned and saved to cloud
4. Tap result to open (URLs) or view details

### Using Gallery
1. Tap gallery icon at bottom
2. Select image with QR code
3. Code is extracted and saved

### Viewing History
1. Tap history icon (clock) at bottom
2. View all past scans
3. Copy, share, or delete entries

### Flashlight
1. Tap flash icon to toggle
2. Useful for scanning in dark

## 🔥 Firebase Database Structure

```
qr-scanner-app-12868/
└── scan_history/
    └── {userId}/
        └── {scanId}/
            ├── id: "unique-id"
            ├── userId: "user-uid"
            ├── content: "scanned content"
            ├── type: "URL|EMAIL|PHONE|TEXT"
            ├── timestamp: 1234567890
            └── displayText: "preview text..."
```

## 🚧 Roadmap

### Implemented ✅
- [x] QR code scanning (camera + gallery)
- [x] Firebase authentication
- [x] Cloud scan history
- [x] Flashlight toggle
- [x] Copy/share functionality

### In Progress 🚀
- [ ] User profile management
- [ ] QR code generator
- [ ] Search/filter history
- [ ] Export history to file

### Future Features 💡
- [ ] Batch scanning
- [ ] Custom QR designs
- [ ] Scan analytics
- [ ] Offline mode with sync
- [ ] Multiple language support

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

- Your Name
- GitHub: [@yourusername](https://github.com/yourusername)

## 🙏 Acknowledgments

- Firebase for backend services
- Google ML Kit for barcode scanning
- CameraX for camera functionality
- Material Design for UI components

## 📧 Support

If you have questions or need help:
- Open an issue on GitHub
- Check the documentation files
- Review YouTube tutorials mentioned in guides

## 📊 Project Stats

- **Language**: Kotlin 100%
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Architecture**: Repository Pattern
- **Database**: Firebase Realtime Database

---

**⭐ If you find this project helpful, please give it a star!**

**🚀 Happy Scanning!**


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



