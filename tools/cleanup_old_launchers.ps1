$ErrorActionPreference = "SilentlyContinue"

$desktop = [Environment]::GetFolderPath("Desktop")
$targets = @(
  (Join-Path $desktop "ProjectX1 Launcher (Silent).lnk"),
  (Join-Path $desktop "ProjectX1 Launcher (Console).lnk"),
  (Join-Path $desktop "ProjectX1 Launcher (user).lnk"),
  "C:\Users\KOSMO\Desktop\projectx1\ProjectX1 Launcher (Silent).lnk",
  "C:\Users\KOSMO\Desktop\projectx1\ProjectX1 Launcher (Console).lnk",
  "C:\Users\KOSMO\Desktop\projectx1\ProjectX1 Launcher (user).lnk"
)

foreach ($t in $targets) {
  if (Test-Path $t) {
    Remove-Item $t -Force
    Write-Output "Removed: $t"
  }
}

$main = Join-Path $desktop "ProjectX1 Launcher.lnk"
if (Test-Path $main) {
  Write-Output "Main remains: $main"
} else {
  Write-Output "Main launcher missing: $main"
}
