@echo off
setlocal
set "PROJECT_DIR=%~dp0"
set "MAVEN_VERSION=3.9.16"
set "MAVEN_HOME=%PROJECT_DIR%.mvn\wrapper\dists\apache-maven-%MAVEN_VERSION%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_EXE=%MAVEN_HOME%\bin\mvn.cmd"

if not "%JAVA_HOME%"=="" if exist "%JAVA_HOME%\bin\java.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
where java >nul 2>nul
if errorlevel 1 (
    echo Java was not found. Install JDK 21 and set JAVA_HOME or add java.exe to PATH.
    exit /b 1
)

if not exist "%MAVEN_EXE%" (
    set "MAVEN_ARCHIVE=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip"
    set "MAVEN_CHECKSUM=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip.sha512"
    echo Downloading Apache Maven %MAVEN_VERSION% on first use...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $base='https://dlcdn.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri $base -OutFile $env:TEMP\apache-maven-%MAVEN_VERSION%-bin.zip; Invoke-WebRequest -Uri ($base + '.sha512') -OutFile $env:TEMP\apache-maven-%MAVEN_VERSION%-bin.zip.sha512; $expected=(Get-Content $env:TEMP\apache-maven-%MAVEN_VERSION%-bin.zip.sha512 -Raw).Trim().Split(' ')[0].ToLowerInvariant(); $actual=(Get-FileHash $env:TEMP\apache-maven-%MAVEN_VERSION%-bin.zip -Algorithm SHA512).Hash.ToLowerInvariant(); if ($actual -ne $expected) { throw 'Apache Maven checksum verification failed.' }; Expand-Archive -Path $env:TEMP\apache-maven-%MAVEN_VERSION%-bin.zip -DestinationPath '%PROJECT_DIR%.mvn\wrapper\dists\apache-maven-%MAVEN_VERSION%' -Force"
    if errorlevel 1 (
        echo Maven download or checksum verification failed.
        exit /b 1
    )
)

call "%MAVEN_EXE%" %*
exit /b %ERRORLEVEL%
