$ErrorActionPreference = "Stop"

$projectRoot = "C:\Users\KOSMO\Desktop\projectx1"
$targetBat = Join-Path $projectRoot "PROJECTX1_LAUNCHER.bat"
$desktop = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktop "ProjectX1 Launcher.lnk"

if (-not (Test-Path $targetBat)) {
    throw "Launcher bat not found: $targetBat"
}

$wsh = New-Object -ComObject WScript.Shell
$sc = $wsh.CreateShortcut($shortcutPath)
$sc.TargetPath = $targetBat
$sc.WorkingDirectory = $projectRoot
$sc.Description = "ProjectX1 Launcher"
$sc.WindowStyle = 1
$iconPath = Join-Path $projectRoot "assets\projectx1.ico"
if (Test-Path $iconPath) {
    $sc.IconLocation = "$iconPath,0"
}
$sc.Save()

Write-Output "Fixed shortcut: $shortcutPath"
