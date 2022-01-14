# ve-direct-reader-service

### Still work in progress! Will be available soon.

| branch | CI build | test coverage |
|--------|:--------:|--------------:|
| master  | [![CircleCI](https://circleci.com/gh/logreposit/ve-direct-reader-service/tree/master.svg?style=shield)](https://circleci.com/gh/logreposit/ve-direct-reader-service/tree/master)   | [![codecov.io](https://codecov.io/gh/logreposit/ve-direct-reader-service/branch/master/graphs/badge.svg)](https://codecov.io/gh/logreposit/ve-direct-reader-service/branch/master/graphs/badge.svg)   |
| develop | [![CircleCI](https://circleci.com/gh/logreposit/ve-direct-reader-service/tree/develop.svg?style=shield)](https://circleci.com/gh/logreposit/ve-direct-reader-service/tree/develop) | [![codecov.io](https://codecov.io/gh/logreposit/ve-direct-reader-service/branch/develop/graphs/badge.svg)](https://codecov.io/gh/logreposit/ve-direct-reader-service/branch/develop/graphs/badge.svg) |


## Service Description

The ve-direct-reader-service reads measurement and fault data from Victron Energy [VE.Direct](https://www.victronenergy.com/live/vedirect_protocol:faq) devices and pushes it to the 
Logreposit API.

The implementation has been tested with a [BMV-700 battery monitor](https://www.victronenergy.com/battery-monitors/bmv-700) and a [SmartSolar MPPT 100/50](https://www.victronenergy.com/solar-charge-controllers/smartsolar-100-30-100-50) solar charge controller along with a [VE.Direct to USB cable](https://www.victronenergy.com/accessories/ve-direct-to-usb-interface), however, all Victron Energy devices with a VE.Direct port should be supported.  
All measurement values described in the official VE.Direct text protocol documentation, as of beginning of about December 2021, have been implemented and can be found [here](https://github.com/logreposit/ve-direct-reader-service/blob/develop/src/main/kotlin/com/logreposit/vedirectreaderservice/communication/vedirect/VeDirectTextModel.kt). 

The `ve-direct-reader-service` is a Spring Boot project and the library [`com.fazecast.jSerialComm`](https://github.com/Fazecast/jSerialComm) 
is in use for the serial communication.


## Configuration

This service ships as a docker image and has to be configured via environment variables. 

|Environment Variable Name                 | default value              |                                                                                             |
|------------------------------------------|----------------------------|---------------------------------------------------------------------------------------------|
| VEDIRECT_COMPORT                         | /dev/ttyUSB0               |                                                                                             |
| LOGREPOSIT_APIBASEURL                    | https://api.logreposit.com |                                                                                             |
| LOGREPOSIT_DEVICETOKEN                   | **INVALID**                | needs to be changed!                                                                        | 
| LOGREPOSIT_INCLUDELEGACYFIELDS           | false                      | set to true to enable backwards-compatibility to bmv-reader-service                         | 
| LOGREPOSIT_IGNOREDFIELDS                 | *empty*                    | comma separated list of logreposit field names to ignore / not report at all. The names are referring to the `logrepositName` of the `VeDirectField` in the file which can be found [here](https://github.com/logreposit/ve-direct-reader-service/blob/develop/src/main/kotlin/com/logreposit/vedirectreaderservice/communication/vedirect/VeDirectTextModel.kt). |
| LOGREPOSIT_MINIMUMUPDATEINTERVALINMILLIS | 10000                      | minimum update interval in milliseconds, set to `0` to push on every VE.Direct Text update. |
| LOGREPOSIT_ADDRESS                       | 1                          | set to some other value if you have multiple devices                                        |


## Docker

The latest images can be found on [Dockerhub](https://hub.docker.com/r/logreposit/ve-direct-reader-service/tags).

Place the following `docker-compose.yml` file in a new folder, then run `docker-compose up -d`.

```yaml
version: '2.4'

services:
  ve-direct-reader-service:
    container_name: logreposit-ve-direct-reader-service
    image: logreposit/ve-direct-reader-service:<VERSION>
    restart: always
    devices:
      - "/dev/ttyUSB0:/dev/ttyUSB0"
    environment:
      LOGREPOSIT_DEVICETOKEN: your-logreposit-device-token
      # uncomment the next line in order to prevent publishing rather static fields
      #LOGREPOSIT_IGNOREDFIELDS: "bmv_model,firmware_version_16,firmware_version_24,product_id,serial_number,bluetooth_cap"
```
