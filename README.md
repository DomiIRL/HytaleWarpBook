# HytaleMod

A Hytale server plugin/mod built with Gradle.

## Setup

Place the following files in the `libs/` directory:
- `HytaleServer.jar` - Used as compile dependency and to run the server
- `Assets.zip` - Required to start the Hytale server

## Building

Build the plugin JAR:
```bash
./gradlew build
```

The JAR will be created at `build/libs/HytaleMod-1.0-SNAPSHOT.jar`

## Running the Server

Start the server (automatically builds and deploys the plugin):
```bash
./gradlew runServer
```

This will:
1. Build the plugin
2. Copy it to `Server/mods/`
3. Start the Hytale server from the `Server/` directory
4. Use `libs/HytaleServer.jar` and `libs/Assets.zip`

## Deploy Plugin Only

Just copy the built plugin to the server's mods folder without starting:
```bash
./gradlew deployPlugin
```

## Project Structure

```
HytaleMod/
├── build.gradle              # Gradle build configuration
├── libs/
│   ├── HytaleServer.jar     # Server JAR (not in Git)
│   └── Assets.zip           # Server assets (not in Git)
├── Server/                   # Server runtime directory (not in Git)
│   └── mods/                # Plugin deployment location
│       └── HytaleMod-1.0-SNAPSHOT.jar
├── src/
│   └── main/
│       ├── java/
│       │   └── dev/svrt/dominik/
│       │       ├── HytaleMod.java
│       │       ├── commands/
│       │       └── events/
│       └── resources/
│           └── manifest.json
└── build/
    └── libs/                 # Built JARs
```

## Requirements

- Java 25 (automatically downloaded by Gradle toolchain - no manual setup needed)
- HytaleServer.jar and Assets.zip in `libs/` directory

## Notes

- `libs/` and `Server/` directories are excluded from Git
- The HytaleServer.jar is configured as a compile-only dependency
- Server is started from `Server/` directory with: `java -jar ../libs/HytaleServer.jar --assets ../libs/Assets.zip`
- Gradle automatically downloads and uses Java 25 via toolchain, independent of JAVA_HOME

