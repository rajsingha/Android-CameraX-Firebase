# Android-CameraX-Firebase

## Architecture Used => MVVM
## Features
1. Low Latency (Camera) Hardware access.
2. Direclty upload the data to the FirebaseCloudStorage.
3. Easily accessible data through Firestore Database.

### Apk link @ https://github.com/rajsingha/Android-CameraX-Firebase/releases/download/v1.0/cameraX-android.apk
### The file uploading and storing task in running on main ui thread, so it will take some time.On the other hand we can use background thread it will not interrupt the user experience, but it can lead to the memory leakage.
