@echo off
setlocal EnableExtensions

set "ROOT=%~dp0"
set "SILENT_VBS=%ROOT%RUN_PROJECTX1_SILENT.vbs"

if not exist "%SILENT_VBS%" (
  echo [ERROR] Silent launcher not found.
  echo         %SILENT_VBS%
  pause
  exit /b 1
)

start "" wscript.exe "%SILENT_VBS%"
exit /b 0

