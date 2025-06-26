@echo off
echo Running the application...
java -Dlog4j2.disable.jmx=true -Daws.accessKeyId=YOUR_AWS_KEY -Daws.secretKey=YOUR_AWS_SECRET -Daws.region=ap-southeast-2 -jar target/CloudWatchAppenderDemo-1.0-SNAPSHOT.jar
rem -Dlog4j.debug


IF %ERRORLEVEL% NEQ 0 (
    echo Application execution failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Application finished.
pause
