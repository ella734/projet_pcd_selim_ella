@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "LOCAL_MAVEN_CMD=C:\Users\ellaa\Downloads\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin\mvn.cmd"

if exist "%LOCAL_MAVEN_CMD%" (
    call "%LOCAL_MAVEN_CMD%" %*
    exit /b %ERRORLEVEL%
)

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

echo Maven executable not found.
echo Install Maven or update backend\mvnw.cmd with the correct local path.
exit /b 1
