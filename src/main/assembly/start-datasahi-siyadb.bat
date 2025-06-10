@echo off
setlocal enabledelayedexpansion

rem Set environment variables with defaults
if not defined DATASAHI_PORT set DATASAHI_PORT=8080
if not defined DATASAHI_CONFIG_PATHS set DATASAHI_CONFIG_PATHS=config
if not defined DATASAHI_WORK_DIR set DATASAHI_WORK_DIR=work
if not defined APP_VERSION set APP_VERSION=0.1.2
set AWS_JAVA_V1_DISABLE_DEPRECATION_ANNOUNCEMENT=true

rem Create logs directory if it doesn't exist
set LOGS_DIR=%DATASAHI_WORK_DIR%\logs
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"

rem Set Java memory and GC logging options
set "JAVA_OPTS=-Xms128m -Xmx512m"
set "JAVA_OPTS=!JAVA_OPTS! -Xlog:gc*=info:file=%LOGS_DIR%\datasahi-siyadb.gc.log:time,uptime,level,tags:filecount=5,filesize=100m"
set "JAVA_OPTS=!JAVA_OPTS! -XX:+HeapDumpOnOutOfMemoryError"
set "JAVA_OPTS=!JAVA_OPTS! -XX:HeapDumpPath=%LOGS_DIR%"

rem Function to check Java version
:check_java_version
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Java is not installed or not in PATH
    exit /b 1
)

for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do (
    set "JAVA_VERSION=%%j"
)

if !JAVA_VERSION! LSS 17 (
    echo Error: Java version must be 17 or higher. Current version: !JAVA_VERSION!
    exit /b 1
)

echo Java version !JAVA_VERSION! detected

rem Function to start the server
:start_server
set "jar_file=datasahi-siyadb-%APP_VERSION%-all.jar"
set "log_file=%LOGS_DIR%\datasahi-siyadb.log"

if not exist "%jar_file%" (
    echo Error: %jar_file% not found in current directory
    exit /b 1
)

echo Starting Datasahi Siyadb server...
echo Using JAVA_OPTS: %JAVA_OPTS%
echo Port: %DATASAHI_PORT%
echo Config Paths: %DATASAHI_CONFIG_PATHS%
echo Work Directory: %DATASAHI_WORK_DIR%
echo Log file: %log_file%
echo GC log file: %LOGS_DIR%\datasahi-siyadb.gc.log

rem Start the server and redirect output to log file
start "Datasahi Siyadb" /B cmd /c "java %JAVA_OPTS% -jar "%jar_file%" > "%log_file%" 2>&1"

rem Save the PID (Windows version using wmic)
for /f "tokens=2 delims==" %%a in ('wmic process where "commandline like '%%datasahi-siyadb-0.1-all.jar%%' and name like '%%java%%'" get processid /value') do (
    set PID=%%a
    echo !PID! > "%DATASAHI_WORK_DIR%\datasahi-siyadb.pid"
)

echo Server started with PID !PID!
echo Log file: %log_file%
echo To follow logs: type %log_file%
echo Health endpoint: http://localhost:%DATASAHI_PORT%/health
echo Health check to see all datastores are setup: http://localhost:%DATASAHI_PORT%/health/check

exit /b 0

rem Main execution
:main
echo Initializing Datasahi Siyadb...
call :check_java_version
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
call :start_server