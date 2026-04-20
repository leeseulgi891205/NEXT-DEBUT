@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "PY_DIR=%ROOT%python-ml"
set "URL_ROOT=http://127.0.0.1:8181/"
set "PORTABLE_PY=%ROOT%runtime\python\python.exe"
set "APP_JAR=%ROOT%projectx1.jar"

set "PORTABLE_JAVA="
if exist "%ROOT%runtime\jre\bin\java.exe" (
  if exist "%ROOT%runtime\jre\lib\modules" set "PORTABLE_JAVA=%ROOT%runtime\jre\bin\java.exe"
  if exist "%ROOT%runtime\jre\lib\jvm.cfg" set "PORTABLE_JAVA=%ROOT%runtime\jre\bin\java.exe"
)
if not defined PORTABLE_JAVA (
  for /d %%D in ("%ROOT%runtime\jre\jdk*") do (
    if exist "%%D\bin\java.exe" (
      if exist "%%D\lib\modules" set "PORTABLE_JAVA=%%D\bin\java.exe"
      if exist "%%D\lib\jvm.cfg" set "PORTABLE_JAVA=%%D\bin\java.exe"
    )
  )
)

echo [ProjectX1] Launching from %ROOT%

if not exist "%PY_DIR%\app.py" (
  echo [ERROR] python-ml\app.py not found.
  pause
  exit /b 1
)

set "PY_LAUNCH="
if exist "%PORTABLE_PY%" (
  set "PY_LAUNCH="%PORTABLE_PY%" -m uvicorn app:app --host 127.0.0.1 --port 8000"
) else (
  where python >nul 2>nul && set "PY_LAUNCH=python -m uvicorn app:app --host 127.0.0.1 --port 8000"
  if not defined PY_LAUNCH (
    where py >nul 2>nul && set "PY_LAUNCH=py -3 -m uvicorn app:app --host 127.0.0.1 --port 8000"
  )
)
if not defined PY_LAUNCH (
  echo [ERROR] Python 3 not found.
  echo         (portable: runtime\python\python.exe OR system python/py)
  pause
  exit /b 1
)

set "SPRING_LAUNCH="
if exist "%APP_JAR%" (
  if exist "%PORTABLE_JAVA%" (
    set "SPRING_LAUNCH="%PORTABLE_JAVA%" -jar "%APP_JAR%""
  ) else (
    where java >nul 2>nul && set "SPRING_LAUNCH=java -jar "%APP_JAR%""
  )
) else (
  if exist "%ROOT%gradlew.bat" (
    set "SPRING_LAUNCH=gradlew.bat bootRun"
  )
)
if not defined SPRING_LAUNCH (
  echo [ERROR] Spring startup target not found.
  echo         Need either:
  echo         - projectx1.jar (+ Java or runtime\jre), or
  echo         - gradlew.bat (dev mode)
  pause
  exit /b 1
)

echo [1/3] Starting Python ML server...
start "ProjectX1 Python ML (8000)" cmd /k "cd /d "%PY_DIR%" && %PY_LAUNCH%"

echo [2/3] Starting Spring server...
start "ProjectX1 Spring (8181)" cmd /k "cd /d "%ROOT%" && %SPRING_LAUNCH%"

echo [3/3] Waiting for 127.0.0.1:8181...
set /a WAIT_COUNT=0
:WAIT_LOOP
set /a WAIT_COUNT+=1
powershell -NoProfile -Command "try { $null = Invoke-WebRequest -Uri 'http://127.0.0.1:8181/' -UseBasicParsing -TimeoutSec 2; exit 0 } catch { if ($_.Exception.Response) { $c=[int]$_.Exception.Response.StatusCode; if ($c -gt 0) { exit 0 } }; exit 1 }" >nul 2>nul
if !errorlevel! == 0 goto OPEN_BROWSER
if !WAIT_COUNT! geq 120 goto OPEN_BROWSER
timeout /t 1 /nobreak >nul
goto WAIT_LOOP

:OPEN_BROWSER
echo [ProjectX1] Opening browser...
start "" "%URL_ROOT%"
echo [ProjectX1] If page is not ready, press F5 after a few seconds.
exit /b 0

