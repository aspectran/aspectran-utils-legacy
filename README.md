# Aspectran Utilities (Legacy Compatibility)

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
    <version>1.0.0-SNAPSHOT</version> <!-- Use the actual version -->
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

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
