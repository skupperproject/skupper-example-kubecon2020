# load-gen project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Overview

This application runs a variable load against a web service called "greeting" with path "/hello".

To control it, use the "/set_load/<N>" path to this application.  After setting the load, the return will contain information about the number of requests sent, failures, the last error, and the current requests in-flight.  The value <N> is the desired number of in-flight requests (concurrency).

Set N == 0 to stop the load.
Set N == 1 for synchronous load (only one request at a time)
Set N > 1 for parallel load

This is useful for observing the action of Skupper load-balancing across multiple service-instances.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application is packageable using `./mvnw package`.
It produces the executable `load-gen-1.0-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/load-gen-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or you can use Docker to build the native executable using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your binary: `./target/load-gen-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide .
