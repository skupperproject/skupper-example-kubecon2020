# throttle-service sub-project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Overview

throttle-service is a simple web application that when queried at path "/hello", returns the host/pod (HOSTNAME env variable)
on which the service is running and the number of requests handled by this service instance.

To aid in demonstrating load balancing, the service limits the rate of requests that it can handle.  The environment variable
RATE_LIMIT sets the number of requests that can be handled in one-tenth of a second.  So a value of 1 yields 10/sec, 2 yields 20/sec, etc.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application is packageable using `./mvnw package`.
It produces the executable `throttle-service-1.0-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/throttle-service-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or you can use Docker to build the native executable using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your binary: `./target/throttle-service-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide .
