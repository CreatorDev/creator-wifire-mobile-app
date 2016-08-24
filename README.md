
![](img.png)
----

## The Creator mobile application 
The Creator WiFire mobile application is is part of the Creator project which aims to demonstrate IoT protocols and capabilities. The application is designed to both configure and manage [ChipKIT WiFire](http://chipkit.net/wpcproduct/chipkit-wi-fire/) devices running the [Creator WiFire application](https://github.com/CreatorDev/creator-wifire-app) using an intermediate [device management server](https://github.com/Creatordev/DeviceServer).  

The application targets Android SDK 24 and is compatible back to API 14 (Ice Cream Sandwich).  

**Note.**  
*During the provisioning process (explained below) the mobile application will configure the WiFire device WiFi to change networks. This requires the user to grant Location permissions to the application. The mobile application **does not use  location services** but is required by the Android permission model to acquire Location permissions.*



## Dependencies  
The complete IoT demonstration requires:
* a device, or several devices, to monitor and manage (see the [Creator Wifire application](https://github.com/CreatorDev/creator-wifire-app))  
* a device management server supporting suitable Lightweight machine to machine (LWM2M) protocols (see the [Creator device server](https://github.com/CreatorDev/DeviceServer)). *Note that an instance of the Creator device server is hosted on the [creatordev.io](http://creatordev.io/) site*.  
* a mobile application to configure and manage devices remotely via the device server  


The main functions of the mobile application are:  

- to open a Creator developer account for [creatordev.io](https://console.creatordev.io/#/login)  
- to connect the WiFire device to your Wi-Fi network. The mobile application uses the creatordev.io developer account credentials gained above to access the device server developer API and to retrieve the PSK that will in turn be passed to the WiFire board as part of its configuration process.  
- to obtain a PSK (currently this application is designed to use only PSKs), and then provision the WiFire device to a Creator device server. *Note the bootstrap server URL is hardcoded into the application and points to the creatordev.io device server instance.*  
- to interact with the WiFire device application objects and resources via the device server to:  
    - set/reset the WiFire's LEDs  
    - read the WiFire's CPU core temperature  
    - read the WiFire's analogue input value (simulated by an onboard potentiometer)  
    - read the WiFire's pushbuttons state  

**Important.** In order to interact with the WiFire board this application requires the WiFire board's softAP password which is retrieved by using the command line console with the WiFire board in config mode. See [the WiFire hardware setup page](https://github.com/CreatorDev/creator-wifire-app/blob/master/doc/wiFireHardwareSetup.md). Note that the WiFire password will change on every hex upload to the WiFire device.   

### Creating a developer account
In order to use the mobile application you'll need to log into it. This requires a *Creator developer account*. On the application login screen there is a *Create Account* button which navigates to the [*creator.io*](https://console.creatordev.io/#/login) account sign up page. To create a developer account a unique user name, email address and password are required.   

### Logging into the mobile application
Once a developer account has been created, you can log in to the application by providing your username and password. Selecting the *keep me logged in* option will cause the application to autologin on the next launch.  

**Note.**  
*The mobile application does not store user login credentials. Autologin is performed using temporary access and refresh tokens.*

### Listing connected devices
The Creator device server provides a list of WiFire devices that are already provisioned and currently connected under your developer account. This list is shown on the mobile application's *Connected Devices* page. Provisioned boards that are not currently connected are not shown in the list. Each time the page is refreshed an updated list is requested from the device server. If no devices are detected the option is presented to power up a pre-provisioned board, or to enter the setup process to provision a new device.

### Device setup (provisioning)
Provisioning is the process of assigning a WiFire device to a Creator account and allowing the the device to connect to a Creator Device Server. The mobile application guides you through the provisioning process.

To connect to a device server the WiFire board needs:  

- Wi-Fi access point details (ssid, password, encryption)  
- a bootstrap server URI  
- a PSK that will identify the device to the server  

All of the above must be provided by the mobile application during the device provisioning process.

To begin provisioning a device select the *Setup Device* option from the navigation drawer menu and follow the instructions.

### Communicating with a WiFire device

There are two ways in which the mobile application communicates with a WiFire device:  

1. via the provisioning API exposed by WiFire device. An unconfigured WiFire device powers up into softAP mode and exposes its own Access Point named *WiFire_XXXX* where *XXXX* is the device's id. For configuration and provisioning the mobile application connects to the WiFire's AP to provide the device with configuration data.
  
2. via the Creator device server's REST API. Once a WiFire device is configured and has succesfully connected to a Creator device server, the mobile application switches to the Wi-Fi Access Point and no longer communicates with the WiFire device directly, instead using the Creator device server REST API to manage the WiFire device remotely.
  
Both of the above methods use JSON as payload format.


Once a WiFire device is configured and has succesfully connected to a Creator device server, the mobile application switches to the Wi-Fi Access Point and no longer communicates with the WiFire device directly, instead using the Creator device server REST API to manage the WiFire device remotely.

### Interacting with WiFire board
Once a device has been provisioned it will appear on the mobile application's *Connected devices* list. Any further devices that are provisioned under the same developer account will also be visible. Selecting any device on the list, and clicking the *Interact with selected* button will put the mobile application into *interactive mode*. This means that the selected device is now the focus of the mobile application's operations. 

The Interactive screen contains:  

- led icons x 4. These icons are clickable and any state change created on the mobile app will be reflected by the WiFire device.
- button icons x 2. These represent the state of the WiFire device's buttons. If the buttons are pressed, their state will be shown on the interaction screen.
- the current CPU temperature in degrees Celsius (°C)
- the analogue input value in volts. This value can be changed by adjusting potentiometer on the WiFire device.

**Note.**  
*Because the mobile application uses a polling mechanism to refresh the screen by performing an HTTPS request to the device server every two seconds, an operation performed on the WiFire device (pressing a button for example) could encounter a propagation delay and may not be immediately apparent on the mobile application screen.*  

While in interactive mode it's possible to fetch basic information about a selected device by going to the *Device Info* page, which will display any device information represented by IPSO object 3, (the *Device* object).

----


## License  
 Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies.
 All rights reserved.  
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 following conditions are met:  
 
 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
following disclaimer.  
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
following disclaimer in the documentation and/or other materials provided with the distribution.  
3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
products derived from this software without specific prior written permission.  

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


----


----
