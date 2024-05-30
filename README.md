## Introduction to Grandcentrix UWB Android Library
This project is all about making it super easy to create an Ultra-Wideband (UWB) session to a controller, 
acting as a controlee. We're using Bluetooth Low Energy (BLE) as an out-of-band (OOB) method to kick things off.

> [!IMPORTANT]
> Our Android UWB Library requires at least **Android 14**, so make sure your project's minimum SDK is set up to be at least API 34 ("UpsideDownCake", Android 14.0)

Add below UWB library reference to the dependencies section of your build.gradle.kts file, to use the library in your project.

```kotlin
dependencies {
    implementation("net.grandcentrix.lib:uwb:0.0.1")
}
```

## Getting Started 

To get started create an instance of the ```UwbBleLibrary```

```kotlin
    val gcxUwbBleLibrary: UwbBleLibrary = GcxUwbBleLibrary(context = context)
```
### Scanning for nearby devices over BLE

The scanning process ues the Bluetooth API to search for available BLE devices.

```kotlin
    gcxUwbBleLibrary.startScan().collect { gcxScanResult ->  }
```

### Establish a connection to nerby device via BLE 

Upon successful discovery, the ```gcxUwbBleLibrary.startScan().collect``` lambda receives ```GcxScanResult``` objects. 
On this object its possible to perform a ```gcxScanResult.connect(uuidProvider)``` call with a ```UUIDProvider``` as an argument to establish a Bluetooth connection.
The ```UUIDProvider``` class supplies the UUIDs for the service and characteristics required to communicate, by default we are using the **Nordic UART Service**
with the RX and TX characteristic.

```kotlin
    gcxUwbBleLibrary.startScan().collect { gcxScanResult ->
        gcxScanResult.connect(
            uuidProvider
        ).collect { connectionState -> }
    }
```

### Start UWB ranging

The ```gcxScanResult.connect(uuidProvider).collect``` lamba receives ```ConnectionState``` object with following fields:
- **Connectionstate.Connected** - represents the state when the connection to the bluetooth device is successfully established
- **Connectionstate.Disconnected** - represents the state when the connection to the bluetooth device is disconnected.
- **Connectionstate.ServicesDiscovered** - represents the state when the services of the bluetooth device have been discovered, contains a object of `GcxUwbDevice`
  
When the ```ConnectionState.ServicesDiscovered``` state is reached, it contains a object of **GcxUwbDevice** on this object 
its possible to perform a ```gcxUwbDevice.startRanging(deviceConfigInterceptor, phoneConfigInterceptor, rangingConfig``` call with
```DeviceConfigInterceptor```, ```PhoneConfigInterceptor``` and ```RangingConfig```
to start the UWB ranging session. 
After all data between controller and controlee is successfully transferd, ```UwbResult.PositionResult``` will return fields like:
- **distance** - the measured distance from the controller, or null if not available
- **azimuth** - the measured azimuth from the controller, or null if not available
- **elevation** - the measured elevation from the controller, or null if not available
- **elapsedRealtimeNanos** - the elapsed real-time in nanoseconds since the ranging measurement was taken

```kotlin
    gcxUwbBleLibrary.startScan().collect { gcxScanResult ->
        gcxScanResult.connect(
            uuidProvider
        ).collect { connectionState ->
            when (connectionState) {
                ConnectionState.Connected -> print("Connected")
                ConnectionState.Disconnected -> print("Disconnected")
                is ConnectionState.ServicesDiscovered -> {
                    connectionState.gcxUwbDevice.startRanging(
                        deviceConfigInterceptor,
                        phoneConfigInterceptor,
                        rangingConfig
                    ).collect { uwbResult ->
                        when (uwbResult) {
                            UwbResult.Disconnected -> print("Peer disconnected")
                            UwbResult.RangingStarted -> print("Ranging started")
                            UwbResult.RangingStopped -> print("Ranging stopped")
                            is UwbResult.PositionResult -> print(uwbResult)
                            UwbResult.UnknownResult -> throw Exception()
                        }
                    }
                }
            }
        }
    }
```

### Interceptor

We currently provide two interceptor interfaces for interpreting custom configurations between the device (controller) and the phone (controlee). For sample implementations, check out the interceptor directory in our sample project.
