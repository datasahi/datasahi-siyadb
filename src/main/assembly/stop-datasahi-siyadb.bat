@echo off
setlocal enabledelayedexpansion

rem Set environment variables if not already set
if not defined DATASAHI_WORK_DIR set DATASAHI_WORK_DIR=work
if not defined DATASAHI_PORT set DATASAHI_PORT=8080

rem PID file location
set "PID_FILE=%DATASAHI_WORK_DIR%\datasahi-siyadb.pid"

rem Function to check if process is running
:check_process
set "CHECK_PID=%~1"
wmic process where "ProcessId='%CHECK_PID%'" get ProcessId >nul 2>&1
if %ERRORLEVEL% equ 0 (
    exit /b 0
) else (
    exit /b 1
)

rem Function to make API call for graceful shutdown
:call_stop_api
echo Calling shutdown API endpoint...

rem Make API call with timeout and proper error handling
set "API_URL=http://localhost:%DATASAHI_PORT%/shutdown"

rem Try the API call with a 30-second timeout
curl -s -w "%%{http_code}" --max-time 30 "%API_URL%" > "%TEMP%\api_response.txt" 2>"%TEMP%\curl_error.txt"
set "CURL_EXIT=%ERRORLEVEL%"

if %CURL_EXIT% neq 0 (
    if %CURL_EXIT% equ 7 (
        echo Error: Failed to connect to the server. Connection refused.
    ) else if %CURL_EXIT% equ 28 (
        echo Error: Request timed out
    ) else (
        echo Error: API call failed with curl exit code: %CURL_EXIT%
    )
    exit /b 1
)

rem Get HTTP status code and response
for /f "usebackq delims=" %%a in ("%TEMP%\api_response.txt") do set "API_RESPONSE=%%a"
set "HTTP_CODE=!API_RESPONSE:~-3!"
set "API_RESPONSE=!API_RESPONSE:~0,-3!"

if "!HTTP_CODE!" equ "200" (
    echo Shutdown API call successful
    echo API Response: !API_RESPONSE!
    exit /b 0
) else (
    echo Error: API call failed with HTTP status !HTTP_CODE!
    echo API Response: !API_RESPONSE!
    exit /b 1
)

rem Function to shutdown the server
:shutdown_server
rem Check if PID file exists
if not exist "%PID_FILE%" (
    echo Error: PID file not found at %PID_FILE%
    echo Server might not be running or PID file was deleted
    exit /b 1
)

rem Read PID from file
set /p PID=<"%PID_FILE%"

rem Validate PID
echo %PID%| findstr /r "^[0-9]*$" >nul
if %ERRORLEVEL% neq 0 (
    echo Error: Invalid PID found in %PID_FILE%
    del /f "%PID_FILE%" 2>nul
    exit /b 1
)

rem Check if process is running
call :check_process %PID%
if %ERRORLEVEL% neq 0 (
    echo Process with PID %PID% is not running
    del /f "%PID_FILE%" 2>nul
    exit /b 1
)

echo Initiating graceful shutdown sequence...

rem Try API shutdown
call :call_stop_api
if %ERRORLEVEL% equ 0 (
    echo API shutdown signal sent successfully, terminating process...

    rem Send CTRL+C signal (similar to SIGTERM)
    taskkill /PID %PID% /F >nul 2>&1

    rem Wait for up to 10 seconds for the process to end
    set TIMEOUT=10
    :wait_loop1
    call :check_process %PID%
    if %ERRORLEVEL% equ 0 (
        if %TIMEOUT% gtr 0 (
            echo Waiting for process to end... (!TIMEOUT! seconds remaining^)
            timeout /t 1 /nobreak >nul
            set /a TIMEOUT-=1
            goto :wait_loop1
        )
    )

    rem If process is still running, force kill
    call :check_process %PID%
    if %ERRORLEVEL% equ 0 (
        echo Process did not terminate gracefully. Using force kill...
        taskkill /F /PID %PID% >nul 2>&1
        timeout /t 1 /nobreak >nul
    )

    rem Final check
    call :check_process %PID%
    if %ERRORLEVEL% equ 0 (
        echo Error: Failed to terminate process
        exit /b 1
    ) else (
        echo Server shutdown successfully
        del /f "%PID_FILE%" 2>nul
        exit /b 0
    )
) else (
    echo API shutdown failed, attempting direct process termination...

    rem Try graceful shutdown first
    taskkill /PID %PID% >nul 2>&1

    rem Wait for up to 10 seconds
    set TIMEOUT=10
    :wait_loop2
    call :check_process %PID%
    if %ERRORLEVEL% equ 0 (
        if %TIMEOUT% gtr 0 (
            echo Waiting for server to shutdown... (!TIMEOUT! seconds remaining^)
            timeout /t 1 /nobreak >nul
            set /a TIMEOUT-=1
            goto :wait_loop2
        )
    )

    rem If process is still running, force kill
    call :check_process %PID%
    if %ERRORLEVEL% equ 0 (
        echo Server did not shutdown gracefully. Forcing shutdown...
        taskkill /F /PID %PID% >nul 2>&1
        timeout /t 1 /nobreak >nul
    )

    rem Final check
    call :check_process %PID%
    if %ERRORLEVEL% equ 0 (
        echo Error: Failed to shutdown server
        exit /b 1
    ) else (
        echo Server shutdown successfully
        del /f "%PID_FILE%" 2>nul
        exit /b 0
    )
)

rem Main execution
:main
echo Initiating Datasahi Siyadb shutdown...
echo Using port: %DATASAHI_PORT%
call :shutdown_server
exit /b %ERRORLEVEL%