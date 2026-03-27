@REM Maven Wrapper startup script for Windows
@setlocal
@set JAVA_HOME=%USERPROFILE%\jdk\jdk-21.0.10+7
@set PATH=%JAVA_HOME%\bin;%PATH%

@set WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
@set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@if not exist %WRAPPER_JAR% (
    powershell -Command "Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR%"
)

@set MAVEN_PROJECTBASEDIR=%~dp0
@java -jar %WRAPPER_JAR% %*
