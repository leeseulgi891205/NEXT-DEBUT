$ErrorActionPreference = "Stop"

# 스크립트 위치 기준으로 프로젝트 루트를 계산해, 다른 PC/경로에서도 그대로 동작
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$launcherBat = Join-Path $projectRoot "RUN_PROJECTX1.bat"
$launcherGuiPs1 = Join-Path $projectRoot "tools\launch_projectx1_gui.ps1"
$launcherSilentVbs = Join-Path $projectRoot "RUN_PROJECTX1_SILENT.vbs"
$powershellExe = Join-Path $env:WINDIR "System32\WindowsPowerShell\v1.0\powershell.exe"
$wscriptExe = Join-Path $env:WINDIR "System32\wscript.exe"
$desktopCandidates = @(
    [Environment]::GetFolderPath("Desktop"),
    (Join-Path $env:USERPROFILE "Desktop")
) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique

if (-not (Test-Path $launcherBat)) {
    throw "Launcher file not found: $launcherBat"
}
if (-not (Test-Path $launcherGuiPs1)) {
    throw "GUI launcher script not found: $launcherGuiPs1"
}
if (-not (Test-Path $launcherSilentVbs)) {
    throw "Silent launcher script not found: $launcherSilentVbs"
}

$wsh = New-Object -ComObject WScript.Shell

$iconCandidates = @(
    (Join-Path $projectRoot "assets\projectx1.ico"),
    (Join-Path $projectRoot "icon.ico"),
    (Join-Path $projectRoot "src\main\resources\static\favicon.ico")
)

$iconLocation = $null

foreach ($icon in $iconCandidates) {
    if (Test-Path $icon) {
        $iconLocation = "$icon,0"
        break
    }
}

if (-not $desktopCandidates -or $desktopCandidates.Count -eq 0) {
    throw "Desktop path not found."
}

foreach ($desktopPath in $desktopCandidates) {
    $launcherShortcutPath = Join-Path $desktopPath "ProjectX1 Launcher.lnk"
    $launcherShortcut = $wsh.CreateShortcut($launcherShortcutPath)
    $launcherShortcut.TargetPath = $wscriptExe
    $launcherShortcut.Arguments = "`"$launcherSilentVbs`""
    $launcherShortcut.WorkingDirectory = $projectRoot
    $launcherShortcut.Description = "ProjectX1 Auto Launcher"
    $launcherShortcut.WindowStyle = 1
    if ($iconLocation) { $launcherShortcut.IconLocation = $iconLocation }
    $launcherShortcut.Save()
    Write-Output "Created shortcut: $launcherShortcutPath"
}

# 과거 분기형 바로가기는 정리
$oldShortcuts = @(
    (Join-Path ([Environment]::GetFolderPath("Desktop")) "ProjectX1 Launcher (Silent).lnk"),
    (Join-Path ([Environment]::GetFolderPath("Desktop")) "ProjectX1 Launcher (Console).lnk"),
    (Join-Path (Join-Path $env:USERPROFILE "Desktop") "ProjectX1 Launcher (Silent).lnk"),
    (Join-Path (Join-Path $env:USERPROFILE "Desktop") "ProjectX1 Launcher (Console).lnk"),
    (Join-Path $projectRoot "ProjectX1 Launcher (user).lnk"),
    (Join-Path $projectRoot "ProjectX1 Launcher (Console).lnk")
)
foreach ($old in $oldShortcuts) {
    if (Test-Path $old) {
        Remove-Item $old -Force -ErrorAction SilentlyContinue
    }
}
