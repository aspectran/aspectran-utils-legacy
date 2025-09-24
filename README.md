# Aspectran Utilities (Legacy Compatibility)

[![Build Status](https://github.com/aspectran/aspectran-utils-legacy/workflows/Java%20CI/badge.svg)](https://github.com/aspectran/aspectran-utils-legacy/actions?query=workflow%3A%22Java+CI%22)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.aspectran/aspectran-utils-legacy)](https://central.sonatype.com/artifact/com.aspectran/aspectran-utils-legacy)
[![javadoc](https://javadoc.io/badge2/com.aspectran/aspectran-utils-legacy/javadoc.svg)](https://javadoc.io/doc/com.aspectran/aspectran-utils-legacy)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

The `aspectran-utils-legacy` module provides a collection of miscellaneous utility classes designed to support API calls and response handling between legacy systems (JDK 1.6 to JDK 17) and modern Aspectran application servers (JDK 21+). It acts as a compatibility layer, enabling seamless integration without requiring extensive modifications to existing legacy codebases.

## Features

*   **Broad JDK Compatibility:** Supports Java environments from JDK 1.6 up to JDK 17, allowing older systems to interact with modern Aspectran servers.
*   **Simplified API Calls:** Facilitates easy serialization and deserialization of data in JSON or APON (Aspectran Object Notation) formats for efficient API request and response processing.
*   **Secure Communication Support:** Includes utilities like `PBEncryptionUtils` for secure encryption and decryption of sensitive data.
*   **Minimal Legacy Code Changes:** Designed to integrate with minimal impact on existing legacy system code.

## Compatibility

This module is compatible with Java Development Kits (JDKs) ranging from **JDK 1.6 to JDK 17**. It is intended for use in environments where upgrading to JDK 21 or newer is not feasible for the legacy application.

## Usage

To use `aspectran-utils-legacy` in your project, add the appropriate dependency to your build configuration.

### Maven

```xml
<dependency>
    <groupId>com.aspectran</groupId>
    <artifactId>aspectran-utils-legacy</artifactId>
    <version>1.0.0</version> <!-- Use the actual version -->
</dependency>
```

### Gradle

```gradle
implementation 'com.aspectran:aspectran-utils-legacy:1.0.0-SNAPSHOT' // Use the actual version
```

For detailed usage examples and environment configuration, please refer to the [Legacy Integration Guide](docs/practical-guide-to-legacy-integration_en.md).

## Building

To build the `aspectran-utils-legacy` module from source, navigate to the module's root directory and use Maven:

```bash
mvn clean install
```

This will compile the module, run tests, and install the JAR file into your local Maven repository.

## License

Aspectran is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
