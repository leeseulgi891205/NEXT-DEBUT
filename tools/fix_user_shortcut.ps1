$ErrorActionPreference = "Stop"

$projectRoot = "C:\Users\KOSMO\Desktop\projectx1"
$target = Join-Path $projectRoot "RUN_PROJECTX1_SILENT.vbs"
$shortcutPath = Join-Path $projectRoot "ProjectX1 Launcher (user).lnk"

if (-not (Test-Path $target)) {
    throw "Target not found: $target"
}

$wsh = New-Object -ComObject WScript.Shell
$sc = $wsh.CreateShortcut($shortcutPath)
$sc.TargetPath = $target
$sc.WorkingDirectory = $projectRoot
$sc.Description = "ProjectX1 Auto Launcher (User)"
$iconPath = Join-Path $projectRoot "assets\projectx1.ico"
if (Test-Path $iconPath) {
    $sc.IconLocation = "$iconPath,0"
}
$sc.Save()

Write-Output "Fixed shortcut: $shortcutPath"
