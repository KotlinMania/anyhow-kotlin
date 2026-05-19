@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem Project-local Android SDK installer for Windows CI and developer shells.
rem Mirrors setup-android-sdk.sh: no system installs, no shared SDKs from
rem sibling projects, and local.properties always points at this repo's SDK.

set "REPO_ROOT=%~dp0"
if "%REPO_ROOT:~-1%"=="\" set "REPO_ROOT=%REPO_ROOT:~0,-1%"

set "SDK_DIR=%REPO_ROOT%\.android-sdk"
set "SDKMANAGER=%SDK_DIR%\cmdline-tools\latest\bin\sdkmanager.bat"
set "INSTALL_MARKER=%SDK_DIR%\.install-complete"
set "SDK_DIR_PROPERTY=%SDK_DIR:\=/%"

if exist "%INSTALL_MARKER%" if exist "%SDKMANAGER%" (
    > "%REPO_ROOT%\local.properties" echo sdk.dir=%SDK_DIR_PROPERTY%
    echo setup-android-sdk: SDK already installed at %SDK_DIR%
    exit /b 0
)

set "CMDLINETOOLS_REV=14742923"
set "COMPILE_SDK=34"
set "BUILD_TOOLS=36.0.0"
set "ZIP=commandlinetools-win-%CMDLINETOOLS_REV%_latest.zip"
set "URL=https://dl.google.com/android/repository/%ZIP%"

if not exist "%SDKMANAGER%" (
    echo setup-android-sdk: downloading %URL%
    if not exist "%SDK_DIR%\cmdline-tools" mkdir "%SDK_DIR%\cmdline-tools"

    for /f "delims=" %%I in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "[System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), [System.IO.Path]::GetRandomFileName())"') do set "TMPDIR=%%I"
    mkdir "%TMPDIR%" || exit /b 1

    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ErrorActionPreference = 'Stop'; Invoke-WebRequest -Uri '%URL%' -OutFile '%TMPDIR%\%ZIP%'"
    if errorlevel 1 (
        rmdir /s /q "%TMPDIR%" 2>nul
        exit /b 1
    )

    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ErrorActionPreference = 'Stop'; Expand-Archive -LiteralPath '%TMPDIR%\%ZIP%' -DestinationPath '%TMPDIR%' -Force"
    if errorlevel 1 (
        rmdir /s /q "%TMPDIR%" 2>nul
        exit /b 1
    )

    if exist "%SDK_DIR%\cmdline-tools\latest" rmdir /s /q "%SDK_DIR%\cmdline-tools\latest"
    move "%TMPDIR%\cmdline-tools" "%SDK_DIR%\cmdline-tools\latest" >nul
    if errorlevel 1 (
        rmdir /s /q "%TMPDIR%" 2>nul
        exit /b 1
    )
    rmdir /s /q "%TMPDIR%"
)

echo setup-android-sdk: accepting licenses
(for /L %%I in (1,1,200) do @echo y) | "%SDKMANAGER%" --sdk_root="%SDK_DIR%" --licenses >nul
if errorlevel 1 exit /b 1

echo setup-android-sdk: installing platform-tools, android-%COMPILE_SDK%, build-tools;%BUILD_TOOLS%
set "LOGFILE=%SDK_DIR%\sdkmanager-install.log"
"%SDKMANAGER%" --sdk_root="%SDK_DIR%" ^
    "platform-tools" ^
    "platforms;android-%COMPILE_SDK%" ^
    "build-tools;%BUILD_TOOLS%" > "%LOGFILE%" 2>&1
if errorlevel 1 (
    type "%LOGFILE%"
    exit /b 1
)
echo setup-android-sdk: install log at %LOGFILE%

> "%REPO_ROOT%\local.properties" echo sdk.dir=%SDK_DIR_PROPERTY%
break > "%INSTALL_MARKER%"

echo.
echo setup-android-sdk: done
echo   SDK at:     %SDK_DIR%
echo   configured: local.properties -^> %SDK_DIR%
