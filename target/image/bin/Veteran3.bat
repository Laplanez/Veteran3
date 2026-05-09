@echo off
set JLINK_VM_OPTIONS=--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow
set DIR=%~dp0
"%DIR%\java" %JLINK_VM_OPTIONS% -m Veteran3/org.example.JavaFXMain %*
