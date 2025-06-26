# CloudWatch Appender Demo Project

This project demonstrates how to integrate the `kdgregory-log4j2-aws-appender` with a Java application to send logs to AWS CloudWatch Logs. It also addresses common configuration challenges and provides a simplified logging example.

## Project Structure

The project uses Maven for build automation and has the following key files:

*   `pom.xml`: Maven project configuration, including dependencies and build plugins.
*   `src/main/java/com/example/cloudwatch/Main.java`: The main application class that generates log messages.
*   `src/main/resources/log4j2.xml`: Log4j2 configuration for routing logs to the CloudWatch Appender and Console.
*   `run.bat`: A Windows batch script to build and run the application.

## Setup and Configuration

### 1. `pom.xml` Configuration

The `pom.xml` file includes the necessary dependencies for Log4j2, the `kdgregory-log4j2-aws-appender`, AWS SDK v2 for CloudWatch Logs, and Jackson for JSON layout.

Key configurations in `pom.xml`:

*   **Dependencies**:
    *   `log4j-api` and `log4j-core`: Core Log4j2 libraries.
    *   `log4j2-aws-appenders`, `aws-facade-v2`, `logwriters`: The `kdgregory` appender and its required components.
    *   `jackson-databind`, `jackson-core`, `jackson-annotations`: For JSON log formatting.
    *   `cloudwatchlogs`: AWS SDK v2 for CloudWatch Logs.
    *   `log4j-slf4j-impl`: Bridges SLF4J calls to Log4j2, resolving SLF4J warnings.

*   **`maven-shade-plugin`**: This plugin is used to create a single, executable "uber-JAR" that includes all dependencies.
    *   It uses `Log4j2PluginCacheFileTransformer` to correctly discover Log4j2 plugins within the shaded JAR, preventing "package scanning" warnings.
    *   It includes `Multi-Release: true` in the JAR's manifest to correctly handle multi-release JARs (like those from AWS SDK and Jackson), resolving related warnings.
    *   The `mainClass` is set to `com.example.cloudwatch.Main`.

```xml
<!-- Relevant section from pom.xml -->
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <log4j.version>2.23.1</log4j.version>
    <kdgregory.log4j2.aws.appender.version>3.2.1</kdgregory.log4j2.aws.appender.version>
    <jackson.version>2.17.1</jackson.version>
    <aws-sdk-v2.version>2.21.42</aws-sdk-v2.version>
</properties>

<dependencies>
    <!-- Log4j2 Core -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>${log4j.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>${log4j.version}</version>
        <scope>runtime</scope>
    </dependency>

    <!-- kdgregory-log4j2-aws-appender -->
    <dependency>
        <groupId>com.kdgregory.logging</groupId>
        <artifactId>log4j2-aws-appenders</artifactId>
        <version>${kdgregory.log4j2.aws.appender.version}</version>
    </dependency>
    <dependency>
        <groupId>com.kdgregory.logging</groupId>
        <artifactId>aws-facade-v2</artifactId>
        <version>${kdgregory.log4j2.aws.appender.version}</version>
    </dependency>
    <dependency>
        <groupId>com.kdgregory.logging</groupId>
        <artifactId>logwriters</artifactId>
        <version>${kdgregory.log4j2.aws.appender.version}</version>
    </dependency>

    <!-- Jackson for JSON layout -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-annotations</artifactId>
        <version>${jackson.version}</version>
    </dependency>

    <!-- AWS SDK v2 for CloudWatch Logs -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>cloudwatchlogs</artifactId>
        <version>${aws-sdk-v2.version}</version>
    </dependency>
    <!-- SLF4J binding for Log4j2 -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>${log4j.version}</version>
    </dependency>

</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>${maven.compiler.source}</source>
                <target>${maven.compiler.target}</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.4.1</version>
            <dependencies>
                <dependency>
                    <groupId>org.apache.logging.log4j</groupId>
                    <artifactId>log4j-transform-maven-shade-plugin-extensions</artifactId>
                    <version>0.1.0</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <createDependencyReducedPom>false</createDependencyReducedPom>
                        <transformers>
                            <transformer implementation="org.apache.logging.log4j.maven.plugins.shade.transformer.Log4j2PluginCacheFileTransformer"/>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.example.cloudwatch.Main</mainClass>
                                <manifestEntries>
                                    <Multi-Release>true</Multi-Release>
                                </manifestEntries>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 2. `log4j2.xml` Configuration

The `log4j2.xml` file configures the Log4j2 logging framework. It defines a `CloudWatchAppender` to send logs to AWS CloudWatch Logs and a `Console` appender for local output.

Key configurations in `log4j2.xml`:

*   **`CloudWatchAppender`**:
    *   `name`: "CloudWatch"
    *   `logGroup`: `your-cloudwatch-log-group-name` (placeholder - **update this to your desired CloudWatch Log Group**)
    *   `logStream`: `your-cloudwatch-log-stream-name` (placeholder - **update this to your desired CloudWatch Log Stream**)
    *   `clientRegion`: `ap-southeast-2` (the AWS region for your CloudWatch Logs)
    *   `JsonLayout`: Configured to output logs in JSON format with additional properties.
*   **`Console` Appender**: Outputs logs to the console with a specified pattern.
*   **Loggers**:
    *   `Root` logger: Set to `warn` level, sending messages to both `Console` and `CloudWatch` appenders.
    *   `com.kdgregory` logger: Set to `debug` level, also sending messages to both appenders. This is crucial for seeing the internal workings of the `kdgregory` appender.

```xml
<!-- Relevant section from src/main/resources/log4j2.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn">

    <Appenders>

        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%t] %c - %m%n" />
        </Console>

        <CloudWatchAppender name="CloudWatch"
                            logGroup="your-cloudwatch-log-group-name"
                            logStream="your-cloudwatch-log-stream-name"
                            clientRegion="ap-southeast-2">
            <JsonLayout compact="true" eventEol="true" properties="true" stacktraceAsString="true">
                <KeyValuePair key="service" value="CloudWatchAppenderDemo"/>
                <KeyValuePair key="environment" value="dev"/>
            </JsonLayout>
        </CloudWatchAppender>

    </Appenders>

    <Loggers>
        <Root level="warn">
            <AppenderRef ref="Console" />
            <AppenderRef ref="CloudWatch"/>
        </Root>
        <Logger name="com.kdgregory" level="debug" additivity="false">
            <AppenderRef ref="Console" />
            <AppenderRef ref="CloudWatch"/>
        </Logger>
    </Loggers>
</Configuration>
```

### 3. `run.bat` Script

The `run.bat` script is used to execute the compiled Java application. It sets AWS credentials as Java system properties and then runs the shaded JAR.

**How to change AWS Keys:**

The AWS access key, secret key, and region are passed as Java system properties directly in the `java` command within `run.bat`. These properties are: `aws.accessKeyId`, `aws.secretKey`, and `aws.region`.

To change the AWS credentials or region, you need to modify the `java` command line in `run.bat`:

*   `-Daws.accessKeyId=YOUR_AWS_ACCESS_KEY`: Replace `YOUR_AWS_ACCESS_KEY` with your AWS Access Key ID.
*   `-Daws.secretKey=YOUR_AWS_SECRET_KEY`: Replace `YOUR_AWS_SECRET_KEY` with your AWS Secret Access Key.
*   `-Daws.region=ap-southeast-2`: Replace `ap-southeast-2` with your desired AWS region (e.g., `us-east-1`, `eu-west-1`).

```batch
@echo off
echo Running the application...
java -Dlog4j2.disable.jmx=true -Daws.accessKeyId=YOUR_AWS_ACCESS_KEY -Daws.secretKey=YOUR_AWS_SECRET_KEY -Daws.region=ap-southeast-2 -jar target/CloudWatchAppenderDemo-1.0-SNAPSHOT.jar
rem -Dlog4j.debug


IF %ERRORLEVEL% NEQ 0 (
    echo Application execution failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Application finished.
pause
```

## How to Run

1.  **Build the project**: Open a terminal in the project root directory and run:
    ```bash
    mvn clean install
    ```
    This will compile the code, resolve dependencies, and create the executable shaded JAR (`CloudWatchAppenderDemo-1.0-SNAPSHOT.jar`) in the `target` directory.

2.  **Run the application**: Execute the `run.bat` script:
    ```bash
    run.bat
    ```
    The application will start logging messages to the console and attempt to send them to AWS CloudWatch Logs. You should see debug output from the `kdgregory` appender indicating its activity.

## Testing Large Log Messages

The `Main.java` includes a test case that logs a message larger than the 256KB CloudWatch Logs maximum size. Observe the console output and CloudWatch Logs (if configured correctly) to see how the appender handles such messages. The `kdgregory` appender is designed to split large messages into multiple smaller events if necessary.

## Troubleshooting

*   **"package scanning" warnings**: Ensure the `packages` attribute is removed from the `<Configuration>` tag in `log4j2.xml`.
*   **"multi-release JARs" warnings**: Ensure `Multi-Release: true` is added to the `ManifestResourceTransformer` in `pom.xml` and rebuild.
*   **JMX errors (`InstanceAlreadyExistsException`)**: Ensure `Main.java` does not explicitly register JMX MBeans and that `-Dlog4j2.disable.jmx=true` is present in `run.bat`.
*   **Logs not appearing in CloudWatch**:
    *   Verify your AWS credentials and region in `run.bat`.
    *   Check the `logGroup` and `logStream` names in `log4j2.xml`.
    *   Ensure your AWS IAM user/role has the necessary permissions (`logs:CreateLogGroup`, `logs:CreateLogStream`, `logs:PutLogEvents`).
    *   Check the debug output from `com.kdgregory` for any errors or warnings related to publishing.
