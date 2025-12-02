# QR Scanner App 📱

A feature-rich Android QR code scanner application built with Kotlin, featuring real-time scanning, Firebase cloud storage, offline mode support, and a beautiful Material Design interface.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

## ✨ Features

### Core Functionality
- 📸 **Real-Time QR Scanning** - Camera-based scanning with Google ML Kit
- 🖼️ **Gallery Scanning** - Scan QR codes from existing images
- ⚡ **QR Code Generator** - Create custom QR codes from text/URLs
- 💡 **Flashlight Toggle** - Built-in flashlight for low-light scanning
- 🔄 **Duplicate Prevention** - Smart debounce system prevents duplicate scans

### Cloud Features (Online Mode)
- 🔐 **Firebase Authentication** - Secure user login and signup
- ☁️ **Cloud Sync** - Store scan history in Firebase Realtime Database
- 📊 **Scan History** - View and manage all your scans with filtering
- 👤 **User Profiles** - Profile management with photo upload (Base64)
- 🔄 **Multi-Device Sync** - Access your data from any device

### Offline Mode
- ✈️ **Offline Scanning** - Scan and generate QR codes without internet
- 🔒 **Privacy First** - No data collection in offline mode
- 🎯 **Core Features Only** - Focus on scanning and generation

### User Experience
- 🎨 **Material Design 3** - Modern and intuitive interface
- 🌟 **Splash Screen** - Professional app launch experience
- ⚙️ **Settings Menu** - Easy mode switching and app information
- 📤 **Share & Copy** - Export scan results to other apps
- 🚪 **Logout** - Secure session management


## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Hedgehog | 2023.1.1 or later)
- **Android SDK** (API 24+)
- **Firebase Account** (for online features)
- **JDK 11** or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/aunonno403/Android-QR-scanner-app.git
   cd QRscannerapp
   ```

2. **Open in Android Studio**
   - File → Open → Select project folder
   - Wait for Gradle sync to complete

3. **Configure Firebase (Optional - for online features)**
   
   a. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   
   b. Add an Android app with package name: `com.example.qrscannerapp`
   
   c. Download `google-services.json` and place in `app/` directory
   
   d. Enable **Firebase Authentication** (Email/Password)
   
   e. Create **Realtime Database** with these security rules:
   ```json
   {
     "rules": {
       "users": {
         "$userId": {
           ".read": "auth != null",
           ".write": "auth != null && auth.uid == $userId"
         }
       },
       "scan_history": {
         "$userId": {
           ".read": "auth != null && auth.uid == $userId",
           ".write": "auth != null && auth.uid == $userId"
         }
       }
     }
   }
   ```

4. **Build and Run**
   ```bash
   # On Windows
   .\gradlew assembleDebug
   
   # On Mac/Linux
   ./gradlew assembleDebug
   ```
   Or click **Run ▶️** in Android Studio

## 📖 How to Use

### First Launch
1. App opens with **splash screen** (2 seconds)
2. Choose mode:
   - **Online Mode** - Login required, full features with cloud sync
   - **Offline Mode** - No login, scanning and generation only

### Scanning QR Codes
1. Point camera at QR code
2. Auto-detects and displays result
3. **URLs** - Tap to open in WebView
4. **Text/Email/Phone** - Tap for copy/share options
5. Scans saved automatically (online mode only)

### Using Gallery
1. Tap **Gallery** icon (🖼️)
2. Select image with QR code
3. Code extracted and processed

### Generating QR Codes
1. Tap **QR Generator** icon (✏️)
2. Enter text or URL
3. Tap "Generate QR"
4. Save to gallery or share

### Managing History (Online Mode)
1. Tap **History** icon (📜)
2. View all scans with filters:
   - **All** - Show everything
   - **Scanned** - Camera/gallery scans only
   - **Generated** - Created QR codes only
3. Tap entry to view details
4. Long press to delete individual items
5. Use "Delete All" for bulk removal

### Profile Management (Online Mode)
1. Tap **Profile** icon (👤) in toolbar
2. Upload profile picture (auto-compressed to ~200KB)
3. Edit display name
4. View email
5. Tap "Logout" to sign out

### Switching Modes
1. Tap menu (⋮) in toolbar
2. Select "Change Mode"
3. Choose Online or Offline
4. Confirm (login required for online)

## 🛠️ Technology Stack

### Core Technologies
- **Kotlin** - Modern Android development language
- **Android SDK** - Target SDK 36 (Android 15)
- **Material Design 3** - Latest design guidelines

### Firebase Services
- **Firebase Authentication** - User management
- **Firebase Realtime Database** - Cloud data storage
- **Security Rules** - User-scoped data protection

### Libraries & APIs
| Library | Version | Purpose |
|---------|---------|---------|
| CameraX | 1.5.0 | Modern camera implementation |
| ML Kit Barcode Scanning | 17.3.0 | QR code detection |
| ZXing Core | 3.5.0 | QR code generation |
| RecyclerView | 1.3.2 | Efficient list display |
| ViewBinding | 8.7.3 | Type-safe view access |
| Firebase BOM | Latest | Unified dependency management |

## 📂 Project Structure

```
QRscannerapp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/qrscannerapp/
│   │   │   ├── MainActivity.kt              # Main scanner screen
│   │   │   ├── SplashActivity.kt            # Splash screen
│   │   │   ├── LoginActivity.kt             # Authentication
│   │   │   ├── SignupActivity.kt            # Registration
│   │   │   ├── ScanHistoryActivity.kt       # History viewer
│   │   │   ├── QrGeneratorActivity.kt       # QR generator
│   │   │   ├── ProfileActivity.kt           # User profile
│   │   │   ├── WebViewActivity.kt           # In-app browser
│   │   │   ├── AppMode.kt                   # Mode management
│   │   │   ├── models/
│   │   │   │   ├── ScanHistory.kt           # Scan data model
│   │   │   │   └── UserProfile.kt           # Profile data model
│   │   │   ├── repository/
│   │   │   │   ├── ScanHistoryRepository.kt # Firebase operations
│   │   │   │   └── UserProfileRepository.kt # Profile operations
│   │   │   ├── adapter/
│   │   │   │   └── ScanHistoryAdapter.kt    # RecyclerView adapter
│   │   │   └── qrgenerator/
│   │   │       └── QrGenerator.kt           # QR generation logic
│   │   └── res/
│   │       ├── layout/                      # XML layouts
│   │       ├── drawable/                    # Icons and drawables
│   │       ├── menu/                        # Menu definitions
│   │       └── values/                      # Colors, strings, themes
│   ├── google-services.json                 # Firebase config (not in git)
│   └── build.gradle.kts                     # App dependencies
├── gradle/                                  # Gradle wrapper
├── build.gradle.kts                         # Project config
└── README.md                                # This file
```

## 🔒 Security & Privacy

### Data Protection
- ✅ **User-scoped database rules** - Users can only access their own data
- ✅ **Firebase Authentication** - Secure login with email/password
- ✅ **Base64 image storage** - Profile pictures stored securely
- ✅ **No public data access** - All data is private by default
- ✅ **HTTPS encryption** - All Firebase communication is encrypted

### Offline Mode Privacy
- ✅ **No data collection** - Nothing stored or transmitted
- ✅ **No tracking** - Complete privacy for offline users
- ✅ **No authentication** - Use app without creating an account

### Permissions Required
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.INTERNET" />
```

## 🎯 Key Features Explained

### Smart Duplicate Prevention
Prevents multiple saves when camera continuously detects the same QR code:
- **5-second debounce** - Same code won't save twice within 5 seconds
- **10-second confirmation** - After 10 seconds, ask if user wants to rescan
- **Auto-reset** - Scanning different code resets the timer

### Offline Mode
Complete QR functionality without internet:
- Scan QR codes with camera or gallery
- Generate QR codes from text
- No login required
- Perfect for privacy-conscious users

### Profile Pictures (Base64 Storage)
Efficient image storage on free Firebase plan:
- **Auto-compression** - Images compressed to ~200KB
- **JPEG format** - Good quality-to-size ratio
- **No Firebase Storage needed** - Works on free tier
- **Capacity** - Support ~5,000 users with pictures on 1GB limit

### History Filtering
Organize scans efficiently:
- **All** - View complete history
- **Scanned** - Only camera/gallery scans
- **Generated** - Only created QR codes
- Type indicators (URL, TEXT, EMAIL, PHONE, GENERATED)

## 🐛 Troubleshooting

### Firebase Issues

**Problem: Database not saving data**
- Ensure Realtime Database is created in Firebase Console
- Check security rules are published
- Verify `google-services.json` is in `app/` directory
- Check internet connection

**Problem: Authentication fails**
- Enable Email/Password in Firebase Console → Authentication → Sign-in method
- Check for typos in email/password
- Verify Firebase project is active

### App Issues

**Problem: Camera not working**
- Grant camera permission: Settings → Apps → QR Scanner → Permissions
- Restart app after granting permission
- Check device has working camera

**Problem: Gallery scanning fails**
- Grant storage permission
- Ensure image contains visible QR code
- Try with different image

**Problem: Build errors**
```bash
# Clean and rebuild (Mac/Linux)
./gradlew clean
./gradlew build

# Clean and rebuild (Windows)
.\gradlew clean
.\gradlew build

# Or in Android Studio:
# Build → Clean Project
# Build → Rebuild Project
```

### Common Fixes

```
# In Android Studio:

# Invalidate caches
File → Invalidate Caches & Restart

# Sync Gradle
File → Sync Project with Gradle Files

# Check Logcat for errors
View → Tool Windows → Logcat
Filter by: MainActivity, ScanHistoryRepository, ProfileActivity
```

## 📊 Firebase Database Structure

```json
{
  "users": {
    "userId123": {
      "uid": "userId123",
      "displayName": "John Doe",
      "email": "john@example.com",
      "photoBase64": "/9j/4AAQSkZJRg...",
      "createdAt": 1733184000000
    }
  },
  "scan_history": {
    "userId123": {
      "scanId1": {
        "id": "scanId1",
        "userId": "userId123",
        "content": "https://example.com",
        "type": "URL",
        "timestamp": 1733184000000,
        "displayText": "example.com"
      },
      "scanId2": {
        "id": "scanId2",
        "userId": "userId123",
        "content": "Hello World",
        "type": "GENERATED",
        "timestamp": 1733185000000,
        "displayText": "Hello World"
      }
    }
  }
}
```

## 🚀 Roadmap

### ✅ Completed Features
- [x] Real-time QR code scanning
- [x] Gallery image scanning
- [x] Firebase authentication
- [x] Cloud scan history with sync
- [x] Flashlight toggle
- [x] QR code generator
- [x] User profile management
- [x] Offline mode support
- [x] Scan history filtering
- [x] Duplicate scan prevention
- [x] Profile picture upload
- [x] Mode switching
- [x] Logout functionality
- [x] Splash screen

### 🔄 Potential Improvements
- [ ] Dark mode / Theme customization
- [ ] Export history (CSV/JSON)
- [ ] Scan statistics & analytics
- [ ] Batch QR code scanning
- [ ] Custom QR code styling (colors, logos)
- [ ] Multiple language support (i18n)
- [ ] QR code favorites/bookmarks
- [ ] Share history with other users
- [ ] Widget support
- [ ] Backup & restore
- [ ] Advanced search in history
- [ ] QR code templates

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### Contribution Guidelines
- Follow Kotlin coding conventions
- Add comments for complex logic
- Update README if adding new features
- Test thoroughly before submitting
- Keep commits atomic and well-described

## 📝 License

This project is licensed under the MIT License - see below for details:

```
MIT License

Copyright (c) 2025 QR Scanner App

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 📚 Learning Resources

### Recommended YouTube Channels
- [Coding in Flow](https://www.youtube.com/c/CodinginFlow) - Kotlin & Android tutorials
- [Philipp Lackner](https://www.youtube.com/c/PhilippLackner) - Modern Android development
- [Stevdza-San](https://www.youtube.com/c/StevdzaSan) - Firebase integration guides
- [Android Developers](https://www.youtube.com/c/AndroidDevelopers) - Official tutorials

### Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase Documentation](https://firebase.google.com/docs)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning/android)
- [CameraX Documentation](https://developer.android.com/training/camerax)

### Communities
- [Stack Overflow - Android](https://stackoverflow.com/questions/tagged/android)
- [Reddit - r/androiddev](https://reddit.com/r/androiddev)
- [Android Developers Discord](https://discord.gg/androiddev)

## 🎓 Skills Demonstrated

Working with this project demonstrates proficiency in:
- **Kotlin** - Modern Android development
- **Firebase** - Authentication & Realtime Database
- **CameraX** - Advanced camera operations
- **ML Kit** - Machine learning integration
- **Material Design** - UI/UX best practices
- **Repository Pattern** - Clean architecture
- **MVVM Concepts** - Separation of concerns
- **Async Operations** - Callbacks and threading
- **Permissions Handling** - Runtime permissions
- **Data Persistence** - Cloud and local storage
- **Image Processing** - Compression and Base64 encoding

## 📞 Project Information

- **Package Name:** `com.example.qrscannerapp`
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 36 (Android 15)
- **Language:** Kotlin 100%
- **Architecture:** Repository Pattern
- **Database:** Firebase Realtime Database
- **Version:** 1.0.0

## 🆘 Support

Need help?

1. **Check existing documentation** - Most issues are covered
2. **Review Logcat** - Look for error messages and stack traces
3. **Search Stack Overflow** - Many common issues already solved
4. **Open GitHub Issue** - For bugs or feature requests

## 💡 Tips & Best Practices

### For Developers
- Always check Firebase Console for data verification
- Use Logcat with filters (MainActivity, ScanHistoryRepository) for debugging
- Test offline mode separately from online mode
- Clear app data when testing authentication flows
- Monitor Firebase usage to stay within free tier limits

### For Users
- Grant all required permissions for full functionality
- Use offline mode for maximum privacy
- Regularly review and clean scan history
- Enable flashlight in dark environments
- Update profile for personalized experience

## 🏆 Acknowledgments

### Built With
- **Firebase** - Backend as a Service
- **Google ML Kit** - QR code detection
- **ZXing** - QR code generation
- **CameraX** - Camera functionality
- **Material Design** - UI components

### Inspired By
- Modern Android development practices
- Clean architecture principles
- User privacy considerations
- Material Design guidelines

## 👨‍💻 Author

- **GitHub**: [@aunonno403](https://github.com/aunonno403)
- **Project Link**: [Android QR Scanner App](https://github.com/aunonno403/Android-QR-scanner-app)

---

## 🌟 Star This Project

If you find this project helpful, please consider giving it a star ⭐

**Happy Scanning! 📱✨**

---

*Built with ❤️ using Kotlin and Firebase*

*Last Updated: December 2025*

