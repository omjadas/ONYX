# University of Melbourne COMP30022 IT Project
Android application designed to assist elderly and disabled people with navigation. Users authenticated via Google authentication. While signed in users may access the given functions:
1. Maps
   1. Get user's current location
   2. Get locations from text search
   3. Route from current location to a search location
2. Interact with other users
   1. Add users to a list of contacts
   2.Send messages to contacts
   3. Begin video/voice call with a contact
   4. Request assistance from a nearby carer

The application is designed for Android API 28
Minimum Requirement: Android API 23

## Getting Started
### Prerequisites
* Latest version of Android Studio is installed on device

### Installing
1. Clone repository
```
git clone https://github.com/COMP30022-18/ONYX
```
2. Checkout branch 'master'
```
git checkout origin master
```

## Deployment
1. Build project
2. Run project
   1. For Emulator
      1. Install Android emulator using **tools>SDK Manager**
      2. Choose a virtual device using **tools>AVD Manager**
      3. For running the video chat portion of the app:
         a. Create a new virtual device or edit an existing one
         b. Click on "Show Advanced Settings"
         c. Set the front and back cameras (recommended front=webcam, back=emulated)
      4. On clicking run, choose the virtual device
   2. For Android device
      1. Enable app debugging
      2. Connect device via usb cable
      3. On clicking run, choose the device

## Members:
* Omja Das
* Xinwei Ding
* Brody Taylor
* Ashley Duffy
* Sameer Asim

## Notes
* Currently works better with devices rather than emulators
