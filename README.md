# University of Melbourne COMP30022 IT Project
Android application designed to assist elderly and disabled people with navigation. Users authenticated via Google authentication. While signed in users may access the given functions:
1. Maps
   1. Get user's current location
   2. Get locations from text search
   3. Route from current location to a search location
2. Interact with other users
   1. Add users to a list of contacts
   2. Send messages to contacts
   3. Begin video/voice call with a contact
   4. Request assistance from a nearby carer (if you are not a carer yourself)

The application is designed for Android API 28
Minimum Requirement: Android API 26

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
         1. Create a new virtual device or edit an existing one
         2. Click on "Show Advanced Settings"
         3. Set the front and back cameras (recommended front=webcam, back=emulated)
      4. On clicking run, choose the virtual device
   2. For Android device
      1. Enable app debugging
      2. Connect device via usb cable
      3. On clicking run, choose the device

## Application Instructions
* Sign in/Sign up
  * Upon opening the application you will be required to provide a Goolgle account for authentication
  * After pressing the Google button, please choose the account you would like to use
  * If the account you wish to use is not present, click the add account button and fill in your details after being redirected
  * If this is your first time signing in, you will be given an account
  * Account details come from your Google account so the only information you are required to enter is whether or not you are a carer
* Chat (Feature 1)
  * To use chat functionality you must first have a contact, see *Contacts*
  * Initiating chat with a contact opens an interface of chat history with the contact
  * If necessary, scroll to see previous messages
  * By pressing the text box, you open the texting interface
  * Press send to send a completed message
  * The message will be added to the list of messages between you and the contact
* Voice/Video call (Feature 2/5)
  * To use voice chat functionality you must first have a contact, see *Contacts*
  * 
* Maps (Feature 3)
  * Upon signing in you will be redirected to the maps tab
  * Your current location is specified by a blue dot on the map interface
  * You can search for destinations and begin routing by typing them in the search bar located at the top of the screen
* Contacts (Feature 4)
  * When opening this section, a list of contacts are drawn to the screen
  * Scroll down to view multiple contacts if necessary
  * By pressing the '+' button you will prompt a contact addition
  * Fill in the email of the contact you wish to add and press send to make the request
  * If you need a contact for testing purposes, you may use *aduffy1@student.unimelb.edu.au*
* Request carer (Feature 7)
  * 
* Broadcast SOS (Feature 8)
  *
* Fall detection (Feature 9)
  *
* Toggle POI on map interface (Feature 10)
  * Check the boxes for the POI types you would like to remove from your map interface
  * Press send to confirm the change
* Favourite places (Feature 13)
  *
* Tabular interface
  * The bottom bar has a set of main features for ease of access
  * To access these feature, simply press them on the bar
  * The order of access from left to right is as follows:
                                                        * Maps
                                                        * Contacts
                                                        * Call
                                                        * Favourite Places
                                                        * Settings
* Drop down settings
  * By pressing the three dots in the top right corner of the application you enter a menu that allows you to sign out

## Members:
* Omja Das
* Xinwei Ding
* Brody Taylor
* Ashley Duffy
* Sameer Asim

## Notes
* Currently works better with devices rather than emulators
