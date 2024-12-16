Setup to run the app on Ubuntu
This guide will help you setup and run the Kotlin mobile app that communicates with an API in Laravel

+ Prerequisites
- Have Ubuntu 20.04 or higher installed.
- Android Studio installed and configured.
- Have access to the API in Laravel (with URL) example: "http://192.168.43.24:8000/api/"
- Have a device or emulator with GPS support.

+ Steps to run the app
1. Clone the repository
Open the terminal in Ubuntu.
Clone this repository to your computer:

Copy code
git clone https://github.com/juliangithub2024/app.git

2. Setup Android Studio
Open Android Studio.
Click File > Open... and select the project folder you cloned.
Allow Android Studio to download the necessary dependencies (it may take a few minutes).

3. Configure the connection with the API

Modify lines 7 and 8 of the Constants.kt file which is located in app>kotlin+java>com.u.pdp>util>, change the API_BASE_URL of the API to which it will be used

line 7:  const val API_BASE_URL = "http://192.168.43.24:8000/api/"
line 8:  const val API_BASE_URL_IMG = "http://192.168.43.24:8000/image/"




4. Run the application

Connect your Android device to the computer with the USB cable or start an Android emulator from Android Studio.
Make sure that location (GPS) is enabled on your device or emulator.
Click the Run button (or use the shortcut Shift+F10) to compile and run the app.

5. Test sending coordinates

Once the app is installed, open the app.
Check that GPS is enabled and grant the necessary permissions.
Press the "OBTAIN OFFERS" button
Watch how the app obtains the geographic coordinates and sends them to the API.
