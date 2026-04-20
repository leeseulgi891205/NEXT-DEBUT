$ErrorActionPreference = "Stop"

$projectRoot = "C:\Users\KOSMO\Desktop\projectx1"
$shortcutPath = Join-Path $projectRoot "ProjectX1 Launcher (user).lnk"
$powershellExe = Join-Path $env:WINDIR "System32\WindowsPowerShell\v1.0\powershell.exe"
$launcherScript = Join-Path $projectRoot "tools\launch_projectx1_gui.ps1"

if (-not (Test-Path $launcherScript)) {
    throw "Launcher script not found: $launcherScript"
}

$wsh = New-Object -ComObject WScript.Shell
$sc = $wsh.CreateShortcut($shortcutPath)
$sc.TargetPath = $powershellExe
$sc.Arguments = "-NoProfile -ExecutionPolicy Bypass -Sta -File `"$launcherScript`""
$sc.WorkingDirectory = $projectRoot
$sc.Description = "ProjectX1 Auto Launcher (User)"
$iconPath = Join-Path $projectRoot "assets\projectx1.ico"
if (Test-Path $iconPath) {
    $sc.IconLocation = "$iconPath,0"
}
$sc.Save()

Write-Output "Fixed shortcut: $shortcutPath"
