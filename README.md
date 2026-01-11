# Offline-Supported Live Location Tracking Module

A robust Android application that ensures reliable location tracking and data synchronization even when the device is offline. This module captures employee location updates and automatically syncs them to the server when network connectivity is restored.

## ✨ Features

- ✅ **Continuous Location Tracking**: Uses FusedLocationProviderClient for accurate location updates
- ✅ **Offline Support**: Stores location data locally using Room Database when offline
- ✅ **Automatic Sync**: Automatically syncs pending locations when network is restored
- ✅ **Zero Data Loss**: Ensures all location logs are delivered to the server
- ✅ **Background Execution**: Works reliably even when app is in background or closed
- ✅ **FIFO Order**: Syncs locations in First-In-First-Out order
- ✅ **Batch Processing**: Processes locations in batches for efficient sync
- ✅ **Error Handling**: Comprehensive error handling with retry mechanism
- ✅ **Battery Optimization Handling**: Requests battery optimization exemption for reliable background execution

## 🏗️ Architecture

The application follows **MVVM (Model-View-ViewModel)** architecture pattern with clean separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  (MainActivity, TrackingViewModel)                          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│              (LocationRepository)                            │
└─────────────────────────────────────────────────────────────┘
         │                           │
         ▼                           ▼
┌──────────────────┐      ┌──────────────────────┐
│   Local Storage  │      │   Remote API         │
│   (Room DB)      │      │   (Retrofit)         │
└──────────────────┘      └──────────────────────┘
         │                           │
         ▼                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Background Services & Workers                   │
│  (LocationTrackingService, LocationSyncWorker)              │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

1. **LocationTrackingService**: Foreground service that continuously collects location updates
2. **LocationSyncWorker**: WorkManager worker that syncs pending locations to server
3. **LocationRepository**: Repository pattern for data access abstraction
4. **NetworkMonitor**: Monitors network connectivity and triggers sync
5. **AppDatabase**: Room database for offline storage

## 📱 Requirements

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 36)
- **Kotlin**: 2.0.21
- **Gradle**: 8.13.2
- **JDK**: 11

### Permissions Required

- `ACCESS_FINE_LOCATION`: For precise location tracking
- `ACCESS_COARSE_LOCATION`: For approximate location (fallback)
- `ACCESS_BACKGROUND_LOCATION`: For location updates when app is in background (Android 10+)
- `FOREGROUND_SERVICE`: For running foreground service
- `FOREGROUND_SERVICE_LOCATION`: For location foreground service (Android 14+)
- `POST_NOTIFICATIONS`: For foreground service notification (Android 13+)
- `INTERNET`: For syncing data to server

## 🚀 Setup & Installation

### Step 1: Clone the Repository

```bash
git clone https://github.com/ritikraushan80/Location-Tracking.git
cd Location Tracking
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the Location Tracking folder
4. Click "OK"

### Step 3: Sync Gradle

Android Studio will automatically sync Gradle dependencies. If not:
- Click "Sync Project with Gradle Files" icon in the toolbar
- Or go to: `File > Sync Project with Gradle Files`

### Step 4: Configure API Endpoint (Optional)

Edit `app/src/main/java/com/kredily/tracking/util/Config.kt`:

```kotlin
const val API_BASE_URL = "https://your-api-endpoint.com/"
```

### Step 5: Build the Project

```bash
./gradlew build
```

### Step 6: Install on Device

```bash
./gradlew installDebug
```

Or build and install from Android Studio:
- Click "Run" button (green play icon)
- Select a device/emulator
- Wait for installation to complete

## ⚙️ Configuration

All configuration constants are located in `app/src/main/java/com/kredily/tracking/util/Config.kt`:

```kotlin
object Config {
    // API Configuration
    const val API_BASE_URL = "https://6960c80ce7aa517cb7971587.mockapi.io/"
    
    // Sync Configuration
    const val SYNC_BATCH_SIZE = 100                    // Locations per batch
    const val MAX_RETRY_ATTEMPTS = 3                   // Max retry attempts
    const val PERIODIC_SYNC_INTERVAL_MINUTES = 15L     // WorkManager periodic sync
    const val IMMEDIATE_SYNC_DELAY_SECONDS = 5L        // Delay before immediate sync
    const val SERVICE_SYNC_CHECK_INTERVAL_MINUTES = 2L // Service sync check interval
    
    // Location Configuration
    const val LOCATION_UPDATE_INTERVAL_MS = 10_000L    // 10 seconds
    
    // Employee ID (configure in production)
    const val DEFAULT_EMPLOYEE_ID = "EMP001"
}
```

### Customizing Configuration

- **API_BASE_URL**: Change to your server endpoint
- **LOCATION_UPDATE_INTERVAL_MS**: Adjust location update frequency (milliseconds)
- **SYNC_BATCH_SIZE**: Number of locations to sync per batch 

## 📖 Usage

### Starting Location Tracking

1. **Launch the app**
2. **Grant permissions** when prompted:
   - Location permission (required)
   - Background location permission (Android 10+)
   - Notification permission (Android 13+)
3. **Grant battery optimization exemption** (recommended for reliable tracking)
4. **Tap "Start Tracking"** button
5. Location tracking will start immediately

### Stopping Location Tracking

1. **Tap "Stop Tracking"** button
2. The service will stop and no new locations will be captured

### Manual Sync

1. **Tap "Sync Now"** button
2. Sync will start if device is online
3. Pending locations will be uploaded to server

### Monitoring Status

The UI displays:
- **Network Status**: "Now Online" (green) or "You are offline" (red)
- **Pending Logs**: Count of locations waiting to be synced

## 📡 API Contract

### Endpoint

```
POST /location/location_tracking
```

### Request Format

The app sends an array of location objects:

```json
[
  {
    "employeeId": "EMP001",
    "latitude": 28.6139,
    "longitude": 77.2090,
    "accuracy": 10.5,
    "timestamp": 1712230429000,
    "speed": 1.8
  },
  {
    "employeeId": "EMP001",
    "latitude": 28.6140,
    "longitude": 77.2091,
    "accuracy": 12.0,
    "timestamp": 1712230439000,
    "speed": 2.1
  }
]
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `employeeId` | String | Employee identifier |
| `latitude` | Double | Latitude coordinate |
| `longitude` | Double | Longitude coordinate |
| `accuracy` | Float | Location accuracy in meters |
| `timestamp` | Long | Unix timestamp in milliseconds |
| `speed` | Float? | Speed in m/s (nullable) |


### Mock API Setup

For testing, you can use:
- **MockAPI.io**: https://mockapi.io/

## 🔄 How It Works

### Location Collection Flow

1. User starts tracking
2. `LocationTrackingService` (foreground service) starts
3. `FusedLocationProviderClient` requests location updates every 10 seconds
4. Each location is saved to Room database with `synced = false`
5. Service continues even when app is in background

### Sync Flow

#### Service-Based Sync (Primary)
1. Service checks for pending locations every 2 minutes
2. If pending locations found, triggers WorkManager sync
3. Throttled to maximum 1 sync per minute

#### WorkManager Periodic Sync (Backup)
1. WorkManager runs periodic sync every 15 minutes (Android minimum)
2. Works even when app is completely closed
3. Processes locations in batches of 100

#### Network-Based Sync
1. `NetworkMonitor` detects when network becomes available
2. Automatically triggers immediate sync
3. Ensures data is synced as soon as connection is restored

### Offline Storage

- All locations are stored in Room database (`tracking_db`)
- Each location has a `synced` flag (boolean)
- Unsynced locations are queried with `ORDER BY id ASC` (FIFO)
- After successful sync, locations are marked as `synced = true`

### Sync Process

1. **Fetch Pending Locations**: Query database for unsynced locations (FIFO)
2. **Batch Processing**: Process in batches of 100 locations
3. **Network Check**: Verify network connectivity
4. **API Call**: Send batch to server via Retrofit
5. **Mark as Synced**: Update database after successful upload
6. **Retry on Failure**: Retry with exponential backoff (2s, 4s, 8s)
7. **Continue**: Process next batch until all synced

## 🧪 Testing

### Testing Location Tracking

1. **Start Tracking**
   - Tap "Start Tracking"
   - Verify notification appears
   - Check logs: `adb logcat | grep LOCATION_TRACK`

2. **Verify Location Capture**
   - Move around with device
   - Check logs for location updates
   - Verify pending count increases in UI

3. **Test Offline Mode**
   - Turn off WiFi/Mobile data
   - Continue moving
   - Verify locations are still captured (pending count increases)
   - Locations should be stored in database

### Testing Sync

1. **Test Online Sync**
   - Have some pending locations
   - Ensure device is online
   - Tap "Sync Now" or wait for automatic sync
   - Verify pending count decreases
   - Check server receives data

2. **Test Offline-to-Online Sync**
   - Go offline
   - Collect some locations
   - Go online
   - Verify automatic sync triggers
   - Check logs: `adb logcat | grep LOCATION_SYNC`

3. **Test Background Sync**
   - Start tracking
   - Put app in background (press home)
   - Wait 2-3 minutes
   - Check logs for sync attempts
   - Verify locations are synced

### Testing Battery Optimization

1. **Grant Battery Optimization Exemption**
   - Launch app
   - System dialog should appear
   - Grant permission
   - Verify tracking works reliably

2. **Test Without Exemption**
   - Revoke battery optimization exemption in settings
   - Start tracking
   - Put device to sleep
   - Verify if tracking continues (may be limited)

### Using ADB for Testing

```bash
# View location logs
adb logcat | grep LOCATION_TRACK

# View sync logs
adb logcat | grep LOCATION_SYNC

# View database logs
adb logcat | grep LOCATION_DB

# Check WorkManager jobs
adb shell dumpsys jobscheduler | grep location_sync

# Check running services
adb shell dumpsys activity services | grep LocationTrackingService
```

## 🔧 Troubleshooting

### Location Not Being Captured

**Possible Causes:**
1. Permissions not granted
2. GPS/location services disabled
3. Battery optimization killing service

**Solutions:**
- Check permissions in Settings > Apps > Tracking > Permissions
- Enable location services on device
- Grant battery optimization exemption
- Check logs: `adb logcat | grep LOCATION_TRACK`

### Locations Not Syncing

**Possible Causes:**
1. No network connection
2. API endpoint unreachable
3. Server returning errors
4. Battery optimization preventing sync

**Solutions:**
- Verify internet connection
- Check API endpoint URL in `Config.kt`
- Verify server is responding
- Check logs: `adb logcat | grep LOCATION_SYNC`
- Grant battery optimization exemption
- Check WorkManager status: `adb shell dumpsys jobscheduler`

### Service Stops in Background

**Possible Causes:**
1. Battery optimization enabled
2. Device in Doze mode
3. System killing service due to memory pressure

**Solutions:**
- Grant battery optimization exemption (critical)
- Ensure service is foreground service (should show notification)
- Check device battery settings
- On some OEM devices, may need additional settings

### High Battery Usage

**Possible Causes:**
1. Location update interval too frequent
2. Too many sync attempts

**Solutions:**
- Increase `LOCATION_UPDATE_INTERVAL_MS` in Config.kt
- Increase `SERVICE_SYNC_CHECK_INTERVAL_MINUTES`
- Ensure battery optimization exemption is granted (paradoxically helps)

### Database Errors

**Possible Causes:**
1. Database corruption
2. Storage full
3. Permission issues

**Solutions:**
- Clear app data: Settings > Apps > Tracking > Storage > Clear Data
- Reinstall app
- Check device storage space
- Check logs: `adb logcat | grep LOCATION_DB`

## 📁 Project Structure

```
Tracking/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/kredily/tracking/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt          # Room database
│   │   │   │   │   │   ├── LocationDao.kt          # Database access
│   │   │   │   │   │   └── LocationEntity.kt       # Database entity
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── LocationApi.kt          # Retrofit API interface
│   │   │   │   │   │   └── LocationRequest.kt      # Request model
│   │   │   │   │   └── repository/
│   │   │   │   │       └── LocationRepository.kt   # Repository pattern
│   │   │   │   ├── receiver/
│   │   │   │   │   └── BootCompletedReceiver.kt    # Boot receiver
│   │   │   │   ├── service/
│   │   │   │   │   └── LocationTrackingService.kt  # Foreground service
│   │   │   │   ├── ui/
│   │   │   │   │   ├── MainActivity.kt             # Main UI
│   │   │   │   │   └── TrackingViewModel.kt        # ViewModel
│   │   │   │   ├── util/
│   │   │   │   │   ├── BatteryOptimizationHelper.kt
│   │   │   │   │   ├── Config.kt                   # Configuration
│   │   │   │   │   ├── LogTags.kt                  # Log tags
│   │   │   │   │   ├── NetworkMonitor.kt           # Network monitoring
│   │   │   │   │   ├── NotificationHelper.kt       # Notification setup
│   │   │   │   │   └── SyncScheduler.kt            # WorkManager scheduling
│   │   │   │   ├── worker/
│   │   │   │   │   └── LocationSyncWorker.kt       # Sync worker
│   │   │   │   └── TrackingApp.kt                  # Application class
│   │   │   ├── res/                                # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                                   # Unit tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── README.md
```

## ⚠️ Limitations & Assumptions

### Limitations

1. **Employee ID**: Currently hardcoded as "EMP001" (should be configurable in production)
2. **No Authentication**: API calls don't include authentication tokens
3. **No Data Encryption**: Database is not encrypted (should be added for production)
4. **No Location Filtering**: All locations are stored regardless of accuracy
5. **No Duplicate Detection**: Same location can be stored multiple times
6. **OEM Device Limitations**: Some OEM devices may have additional restrictions

### Assumptions

1. **API Endpoint**: Server accepts POST requests with array of locations
2. **Network Reliability**: Assumes network will eventually be available
3. **Battery Optimization**: User will grant battery optimization exemption
4. **Permissions**: User will grant all required permissions
