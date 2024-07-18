## Grandcentrix UWB Android Library
This project is all about making it super easy to create an Ultra-Wideband (UWB) session to a controller, 
acting as a controlee. We're using Bluetooth Low Energy (BLE) as an out-of-band (OOB) method to connect and communicate to nearby devices.

> [!IMPORTANT]
> Our Android UWB Library requires at least **Android 14**, so make sure your project's minimum SDK is set up to be at least API 34 ("UpsideDownCake", Android 14.0)

Add below UWB library reference to the dependencies section of your build.gradle.kts file, to use the library in your project.

```kotlin
dependencies {
    implementation("net.grandcentrix.lib:uwb:0.0.2")
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
its possible to perform a ```gcxUwbDevice.startRanging(deviceConfigInterceptor, phoneConfigInterceptor, rangingConfig)``` call with
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

We currently provide two interceptor interfaces for interpreting custom configurations between the device (*controller*) and the phone (*controlee*). The sample shown below is did with the **Mobile Knowledge UWB Kit**.

#### DeviceConfigInterceptor 

For the controller, we expect a byte array that can be interpreted with our ```DeviceConfigInterceptor``` and returns a ```DeviceConfig``` model. In our sample, we created an additional model for Mobile Knowledge, the ```MKDeviceConfig```, which inherits from our ```DeviceConfig``` object.
We then implemented our ```DeviceConfigInterceptor``` interface in the ```MKDeviceConfigInterceptor``` class and overrode the ```intercept(byteArray)``` method.

```kotlin
object MKDeviceConfigInterceptor : DeviceConfigInterceptor {
    override fun intercept(byteArray: ByteArray): DeviceConfig =
        MKDeviceConfig.fromByteArray(byteArray)
}
```

#### PhoneConfigInterceptor

For the controlee, we provide our ```PhoneConfig``` model with the following fields: 
- **sessionId** - session identifier for the ranging operation (integer)
- **preambleIndex** - index used for transmission (byte)
- **channel** - channel to be used for communication (byte)
- **phoneAddress** - phone's address in byte array format

In our sample, we extended our model with the ```MKPhoneConfig``` model to meet the requirements of the **Mobile Knowledge controller**. We then used our ```PhoneConfigInterceptor``` interface and implemented it in the ```MKPhoneConfigInterceptor``` class, *overriding* the ```intercept(sessionId, complexChannel, phoneAddress)``` method. This method returns a byte array that will be sent to the controller to set up everything needed for the ranging session.

```kotlin
object MKPhoneConfigInterceptor : PhoneConfigInterceptor {
    override fun intercept(
        sessionId: Int,
        complexChannel: UwbComplexChannel,
        phoneAddress: ByteArray
    ): ByteArray = MKPhoneConfig(
        specVerMajor = 0x0100.toShort(),
        specVerMinor = 0x0000.toShort(),
        sessionId = sessionId,
        preambleIndex = complexChannel.preambleIndex.toByte(),
        channel = complexChannel.channel.toByte(),
        profileId = RangingParameters.CONFIG_UNICAST_DS_TWR.toByte(),
        deviceRangingRole = 0x01.toByte(),
        phoneAddress = phoneAddress
    ).toByteArray()
}
```

## Publishing

The library is published as an AAR
to [Github Packages](https://github.com/orgs/GCX-HCI/packages?repo_name=grandcentrix-uwb-ble-android)
via the [publish action](.github/workflows/publish.yml). To publish a new version of the library
bump the [version](https://github.com/GCX-HCI/grandcentrix-uwb-ble-android/blob/main/lib/build.gradle.kts#L50)
inside of `lib/build.gradle.kts` and create a new tag with this
pattern: `release/v<major>.<minor>.<patch>`.

> [!CAUTION]
> The action prefers to use the tag instead of the version string in the gradle file. Make sure to
> use the same version in the gradle file and for the tag.

Additionally, a github release for documentation is created automatically by the same workflow.