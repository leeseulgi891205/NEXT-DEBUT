$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class Win32Drag {
    [DllImport("user32.dll")]
    public static extern bool ReleaseCapture();
    [DllImport("user32.dll")]
    public static extern IntPtr SendMessage(IntPtr hWnd, int msg, int wParam, int lParam);
}
"@

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$pyDir = Join-Path $projectRoot "python-ml"
$logsDir = Join-Path $projectRoot "logs"
$springLog = Join-Path $logsDir "spring.log"
$pythonLog = Join-Path $logsDir "python-ml.log"
$launcherLog = Join-Path $logsDir "launcher-gui.log"
$userUrl = "http://127.0.0.1:8181/"
$adminUrl = "http://127.0.0.1:8181/admin"
$iconPath = Join-Path $projectRoot "assets\projectx1.ico"
$portablePythonPath = Join-Path $projectRoot "runtime\python\python.exe"
$appJarPath = Join-Path $projectRoot "projectx1.jar"
$script:serverStatus = "OFF"
$script:isBooting = $false

# Prevent duplicate launcher windows.
$createdNewMutex = $false
$launcherMutex = New-Object System.Threading.Mutex($true, "Local\ProjectX1LauncherGuiMutex", [ref]$createdNewMutex)
if (-not $createdNewMutex) {
    [System.Windows.Forms.MessageBox]::Show(
        "Launcher is already running.",
        "ProjectX1 Launcher",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Information
    ) | Out-Null
    exit 0
}

New-Item -ItemType Directory -Path $logsDir -Force | Out-Null

function Write-LauncherLog([string]$msg) {
    "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $msg" | Out-File -FilePath $launcherLog -Encoding utf8 -Append
}

function Show-Info([string]$msg) {
    [System.Windows.Forms.MessageBox]::Show(
        $msg,
        "ProjectX1 Launcher",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Information
    ) | Out-Null
}

function Show-Error([string]$msg) {
    [System.Windows.Forms.MessageBox]::Show(
        $msg,
        "ProjectX1 Launcher",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error
    ) | Out-Null
}

function Get-LauncherIcon {
    if (-not (Test-Path $iconPath)) {
        return $null
    }
    try {
        return New-Object System.Drawing.Icon($iconPath)
    } catch {
        return $null
    }
}

function New-ProgressForm([string]$titleText) {
    $form = New-Object System.Windows.Forms.Form
    $form.Text = "ProjectX1 Launcher"
    $form.StartPosition = "CenterScreen"
    $form.Size = New-Object System.Drawing.Size(520, 200)
    $form.FormBorderStyle = "FixedDialog"
    $form.MaximizeBox = $false
    $form.MinimizeBox = $false
    $form.TopMost = $true
    $form.ShowIcon = $true
    $icon = Get-LauncherIcon
    if ($icon -ne $null) {
        $form.Icon = $icon
    }

    $title = New-Object System.Windows.Forms.Label
    $title.Text = $titleText
    $title.Font = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
    $title.AutoSize = $true
    $title.Location = New-Object System.Drawing.Point(20, 20)

    $status = New-Object System.Windows.Forms.Label
    $status.Text = "Initializing..."
    $status.AutoSize = $true
    $status.Location = New-Object System.Drawing.Point(20, 58)

    $bar = New-Object System.Windows.Forms.ProgressBar
    $bar.Style = [System.Windows.Forms.ProgressBarStyle]::Continuous
    $bar.Minimum = 0
    $bar.Maximum = 100
    $bar.Value = 5
    $bar.Size = New-Object System.Drawing.Size(470, 22)
    $bar.Location = New-Object System.Drawing.Point(20, 88)

    $hint = New-Object System.Windows.Forms.Label
    $hint.Text = "Log: logs\launcher-gui.log"
    $hint.AutoSize = $true
    $hint.ForeColor = [System.Drawing.Color]::DimGray
    $hint.Location = New-Object System.Drawing.Point(20, 122)

    $form.Controls.AddRange(@($title, $status, $bar, $hint))
    return @{
        Form = $form
        Status = $status
        Bar = $bar
    }
}

function Set-Step($ui, [string]$text, [int]$percent) {
    $ui.Status.Text = $text
    $ui.Bar.Value = [Math]::Max($ui.Bar.Minimum, [Math]::Min($ui.Bar.Maximum, $percent))
    [System.Windows.Forms.Application]::DoEvents()
}

function Test-HttpReady([string]$url) {
    if (Test-PortOpen 8181 300) {
        return $true
    }
    try {
        $null = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
        return $true
    } catch {
        if ($_.Exception.Response) {
            try {
                $code = [int]$_.Exception.Response.StatusCode
                return ($code -gt 0)
            } catch {
                return $false
            }
        }
        return $false
    }
}

function Test-PortOpen([int]$port, [int]$timeoutMs = 250) {
    $client = $null
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect("127.0.0.1", $port, $null, $null)
        if (-not $iar.AsyncWaitHandle.WaitOne($timeoutMs, $false)) {
            return $false
        }
        $client.EndConnect($iar) | Out-Null
        return $true
    } catch {
        return $false
    } finally {
        if ($client -ne $null) {
            try { $client.Close() } catch {}
        }
    }
}

function Resolve-PythonCommand {
    if (Test-Path $portablePythonPath) { return "`"$portablePythonPath`"" }
    if (Get-Command python -ErrorAction SilentlyContinue) { return "python" }
    if (Get-Command py -ErrorAction SilentlyContinue) { return "py -3" }
    return $null
}

function Test-JdkRootLayout([string]$root) {
    if (-not $root) { return $false }
    $javaExe = Join-Path $root "bin\java.exe"
    $libDir = Join-Path $root "lib"
    if (-not ((Test-Path $javaExe) -and (Test-Path $libDir))) {
        return $false
    }
    $mods = Join-Path $libDir "modules"
    $cfg = Join-Path $libDir "jvm.cfg"
    return ((Test-Path $mods) -or (Test-Path $cfg))
}

function Get-PortableJavaExe {
    $jreRoot = Join-Path $projectRoot "runtime\jre"
    if (-not (Test-Path $jreRoot)) {
        return $null
    }
    if (Test-JdkRootLayout $jreRoot) {
        return (Join-Path $jreRoot "bin\java.exe")
    }
    try {
        foreach ($dir in (Get-ChildItem $jreRoot -Directory -ErrorAction SilentlyContinue)) {
            if ($dir.Name -like "jdk*" -and (Test-JdkRootLayout $dir.FullName)) {
                return (Join-Path $dir.FullName "bin\java.exe")
            }
        }
    } catch {
    }
    return $null
}

function Resolve-JavaCommand {
    $portableJava = Get-PortableJavaExe
    if ($portableJava -and (Test-Path $portableJava)) {
        return "`"$portableJava`""
    }
    if (Get-Command java -ErrorAction SilentlyContinue) { return "java" }
    return $null
}

function Ensure-Java21 {
    $java = Get-Command java -ErrorAction SilentlyContinue
    if (-not $java) { return $false }
    $v = (cmd /c "java -version 2>&1" | Select-Object -First 1)
    if (-not $v) { return $false }
    return ([string]$v -match 'version\s+"21(\.|")')
}

function Get-PortPids([int]$port) {
    $rows = netstat -ano -p tcp | Select-String -Pattern (":$port\s")
    $out = @()
    foreach ($row in $rows) {
        $line = ($row.Line -replace '\s+', ' ').Trim()
        $parts = $line.Split(' ')
        if ($parts.Length -lt 5) { continue }
        $procIdRaw = $parts[4]
        $procId = 0
        if ([int]::TryParse($procIdRaw, [ref]$procId) -and $procId -gt 0) {
            if ($out -notcontains $procId) {
                $out += $procId
            }
        }
    }
    return $out
}

function Stop-ProjectX1Servers {
    $killed = @()
    foreach ($port in @(8181, 8000)) {
        $portPids = Get-PortPids $port
        foreach ($procId in $portPids) {
            try {
                Stop-Process -Id $procId -Force -ErrorAction Stop
                $killed += $procId
            } catch {
            }
        }
    }
    return ($killed | Sort-Object -Unique)
}

function Ensure-ProjectFiles {
    if (-not (Test-Path (Join-Path $pyDir "app.py"))) { throw "python-ml\app.py not found." }
    $hasJar = Test-Path $appJarPath
    $hasGradle = Test-Path (Join-Path $projectRoot "gradlew.bat")
    if (-not $hasJar -and -not $hasGradle) {
        throw "Missing Spring startup target: projectx1.jar or gradlew.bat"
    }
}

function Resolve-SpringStartCommand {
    if (Test-Path $appJarPath) {
        $javaCmd = Resolve-JavaCommand
        if (-not $javaCmd) {
            throw "Java runtime not found. Put runtime\jre\bin\java.exe or install Java 21."
        }
        return "$javaCmd -jar `"$appJarPath`""
    }
    if (-not (Test-Path (Join-Path $projectRoot "gradlew.bat"))) {
        throw "gradlew.bat not found."
    }
    if (-not (Ensure-Java21)) { throw "Java 21 is required." }
    return "gradlew.bat bootRun"
}

function Get-ServerStatus {
    if ($script:isBooting) {
        return "BOOTING"
    }
    # Keep UI smooth: use a fast TCP probe for periodic status checks.
    if (Test-PortOpen 8181 180) {
        return "ON"
    }
    return "OFF"
}

function Get-StatusColor([string]$status) {
    switch ($status) {
        "ON" { return [System.Drawing.Color]::FromArgb(87, 255, 197) }
        "BOOTING" { return [System.Drawing.Color]::FromArgb(255, 226, 120) }
        default { return [System.Drawing.Color]::FromArgb(255, 118, 150) }
    }
}

function New-LauncherButton([string]$text, [int]$x, [int]$y, [int]$w, [int]$h, [string]$variant = "default") {
    $btn = New-Object System.Windows.Forms.Button
    $btn.Text = $text
    $btn.Size = New-Object System.Drawing.Size($w, $h)
    $btn.Location = New-Object System.Drawing.Point($x, $y)
    $btn.FlatStyle = [System.Windows.Forms.FlatStyle]::Flat
    $btn.FlatAppearance.BorderSize = 1
    $btn.FlatAppearance.BorderColor = [System.Drawing.Color]::FromArgb(255, 82, 92, 124)
    $btn.BackColor = [System.Drawing.Color]::FromArgb(255, 35, 41, 66)
    $btn.ForeColor = [System.Drawing.Color]::FromArgb(255, 237, 240, 255)
    $btn.Font = New-Object System.Drawing.Font("Segoe UI Semibold", 10, [System.Drawing.FontStyle]::Regular)
    $hoverBack = [System.Drawing.Color]::FromArgb(255, 50, 56, 86)
    $hoverBorder = [System.Drawing.Color]::FromArgb(255, 128, 142, 190)
    if ($variant -eq "primary") {
        $btn.BackColor = [System.Drawing.Color]::FromArgb(255, 84, 86, 214)
        $btn.FlatAppearance.BorderColor = [System.Drawing.Color]::FromArgb(255, 122, 124, 255)
        $btn.ForeColor = [System.Drawing.Color]::White
        $hoverBack = [System.Drawing.Color]::FromArgb(255, 104, 106, 234)
        $hoverBorder = [System.Drawing.Color]::FromArgb(255, 166, 168, 255)
    } elseif ($variant -eq "danger") {
        $btn.BackColor = [System.Drawing.Color]::FromArgb(255, 78, 36, 60)
        $btn.FlatAppearance.BorderColor = [System.Drawing.Color]::FromArgb(255, 166, 92, 132)
        $hoverBack = [System.Drawing.Color]::FromArgb(255, 96, 44, 72)
        $hoverBorder = [System.Drawing.Color]::FromArgb(255, 200, 112, 156)
    } elseif ($variant -eq "subtle") {
        $btn.BackColor = [System.Drawing.Color]::FromArgb(255, 30, 35, 56)
        $btn.FlatAppearance.BorderColor = [System.Drawing.Color]::FromArgb(255, 64, 72, 98)
        $hoverBack = [System.Drawing.Color]::FromArgb(255, 42, 48, 76)
        $hoverBorder = [System.Drawing.Color]::FromArgb(255, 108, 124, 168)
    }
    $baseBack = $btn.BackColor
    $baseBorder = $btn.FlatAppearance.BorderColor
    $baseFore = $btn.ForeColor
    $btn.Tag = [PSCustomObject]@{
        BaseBack = $baseBack
        BaseBorder = $baseBorder
        BaseFore = $baseFore
        HoverBack = $hoverBack
        HoverBorder = $hoverBorder
    }
    $btn.Add_MouseEnter({
        try {
            $meta = $this.Tag
            if ($meta -ne $null) {
                $this.BackColor = $meta.HoverBack
                $this.FlatAppearance.BorderColor = $meta.HoverBorder
                $this.ForeColor = [System.Drawing.Color]::White
            }
        } catch {
        }
    })
    $btn.Add_MouseLeave({
        try {
            $meta = $this.Tag
            if ($meta -ne $null) {
                $this.BackColor = $meta.BaseBack
                $this.FlatAppearance.BorderColor = $meta.BaseBorder
                $this.ForeColor = $meta.BaseFore
            }
        } catch {
        }
    })
    return $btn
}

function Start-ProjectX1AndOpen([string]$modeName, [string]$targetUrl) {
    $script:isBooting = $true
    $script:serverStatus = "BOOTING"
    $ui = New-ProgressForm("Starting ProjectX1 ($modeName)")
    $ui.Form.Show()
    [System.Windows.Forms.Application]::DoEvents()

    try {
        # Fast path: if web is already up, open immediately.
        if (Test-HttpReady $userUrl) {
            Set-Step $ui "Server already running. Opening page..." 100
            Start-Process $targetUrl | Out-Null
            Start-Sleep -Milliseconds 150
            $ui.Form.Close()
            $script:isBooting = $false
            $script:serverStatus = "ON"
            Write-LauncherLog "$modeName mode opened (fast path)"
            return
        }

        Set-Step $ui "Checking files..." 10
        Ensure-ProjectFiles

        Set-Step $ui "Starting Python ML server..." 48
        if (-not (Test-PortOpen 8000 180)) {
            Set-Step $ui "Checking Python 3..." 36
            $pyCmd = Resolve-PythonCommand
            if (-not $pyCmd) { throw "Python 3 is required." }
            $pyVer = & cmd /c "$pyCmd --version" 2>&1
            if ($pyVer -notmatch "Python 3\.") { throw "Python 3 is required. Current: $pyVer" }
            $pyCommandLine = "cd /d `"$pyDir`" && $pyCmd -m uvicorn app:app --host 127.0.0.1 --port 8000 >> `"$pythonLog`" 2>&1"
            Start-Process -FilePath "cmd.exe" -ArgumentList "/c $pyCommandLine" -WindowStyle Hidden | Out-Null
        }

        Set-Step $ui "Starting Spring server..." 62
        if (-not (Test-PortOpen 8181 180)) {
            Set-Step $ui "Preparing Spring runtime..." 54
            $springStartCommand = Resolve-SpringStartCommand
            $springCommandLine = "cd /d `"$projectRoot`" && $springStartCommand >> `"$springLog`" 2>&1"
            Start-Process -FilePath "cmd.exe" -ArgumentList "/c $springCommandLine" -WindowStyle Hidden | Out-Null
        }

        Set-Step $ui "Waiting for service..." 72
        $ready = $false
        for ($i = 0; $i -lt 120; $i++) {
            if (Test-HttpReady $userUrl) {
                $ready = $true
                break
            }
            Start-Sleep -Seconds 1
            $pct = [Math]::Min(95, 72 + [int](($i / 120.0) * 23))
            Set-Step $ui "Waiting for server... ($($i + 1)s)" $pct
        }

        Set-Step $ui "Opening page..." 100
        Start-Process $targetUrl | Out-Null
        Start-Sleep -Milliseconds 250
        $ui.Form.Close()

        if (-not $ready) {
            Show-Info "Server startup is delayed. Browser opened first.`nPlease wait a little and press F5."
        }
        $script:isBooting = $false
        $script:serverStatus = Get-ServerStatus
        Write-LauncherLog "$modeName mode started"
    } catch {
        Write-LauncherLog "launcher error: $($_.Exception.Message)"
        $script:isBooting = $false
        $script:serverStatus = Get-ServerStatus
        try { $ui.Form.Close() } catch {}
        Show-Error $_.Exception.Message
    }
}

function Show-MainLauncher {
    $form = New-Object System.Windows.Forms.Form
    $form.Text = "ProjectX1 Launcher"
    $form.StartPosition = "CenterScreen"
    $form.Size = New-Object System.Drawing.Size(540, 390)
    $form.FormBorderStyle = "None"
    $form.MaximizeBox = $false
    $form.TopMost = $true
    $form.ShowIcon = $true
    $form.BackColor = [System.Drawing.Color]::FromArgb(255, 17, 20, 34)
    $icon = Get-LauncherIcon
    if ($icon -ne $null) {
        $form.Icon = $icon
    }

    $titleBar = New-Object System.Windows.Forms.Panel
    $titleBar.Location = New-Object System.Drawing.Point(0, 0)
    $titleBar.Size = New-Object System.Drawing.Size(540, 34)
    $titleBar.BackColor = [System.Drawing.Color]::FromArgb(255, 27, 31, 50)
    $titleBar.Add_MouseDown({
        [Win32Drag]::ReleaseCapture() | Out-Null
        [Win32Drag]::SendMessage($form.Handle, 0xA1, 0x2, 0) | Out-Null
    })

    $titleIcon = New-Object System.Windows.Forms.PictureBox
    $titleIcon.Location = New-Object System.Drawing.Point(10, 8)
    $titleIcon.Size = New-Object System.Drawing.Size(18, 18)
    $titleIcon.SizeMode = [System.Windows.Forms.PictureBoxSizeMode]::StretchImage
    if (Test-Path $iconPath) {
        try {
            $titleIcon.Image = [System.Drawing.Icon]::ExtractAssociatedIcon($iconPath).ToBitmap()
        } catch {
        }
    }
    $titleIcon.Add_MouseDown({
        [Win32Drag]::ReleaseCapture() | Out-Null
        [Win32Drag]::SendMessage($form.Handle, 0xA1, 0x2, 0) | Out-Null
    })

    $titleText = New-Object System.Windows.Forms.Label
    $titleText.Text = "ProjectX1 Launcher"
    $titleText.Font = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Regular)
    $titleText.AutoSize = $true
    $titleText.ForeColor = [System.Drawing.Color]::FromArgb(255, 216, 222, 245)
    $titleText.Location = New-Object System.Drawing.Point(34, 9)
    $titleText.Add_MouseDown({
        [Win32Drag]::ReleaseCapture() | Out-Null
        [Win32Drag]::SendMessage($form.Handle, 0xA1, 0x2, 0) | Out-Null
    })

    $btnMin = New-LauncherButton "-" 462 4 34 26 "subtle"
    $btnMin.Font = New-Object System.Drawing.Font("Segoe UI", 8, [System.Drawing.FontStyle]::Bold)
    $btnMin.Add_Click({ $form.WindowState = [System.Windows.Forms.FormWindowState]::Minimized })

    $btnClose = New-LauncherButton "X" 500 4 34 26 "danger"
    $btnClose.Font = New-Object System.Drawing.Font("Segoe UI", 8, [System.Drawing.FontStyle]::Bold)
    $btnClose.Add_Click({ $form.Close() })

    $panel = New-Object System.Windows.Forms.Panel
    $panel.Location = New-Object System.Drawing.Point(18, 46)
    $panel.Size = New-Object System.Drawing.Size(488, 322)
    $panel.BackColor = [System.Drawing.Color]::FromArgb(255, 24, 28, 45)
    $panel.BorderStyle = [System.Windows.Forms.BorderStyle]::FixedSingle

    $logo = New-Object System.Windows.Forms.PictureBox
    $logo.Location = New-Object System.Drawing.Point(154, 16)
    $logo.Size = New-Object System.Drawing.Size(32, 32)
    $logo.SizeMode = [System.Windows.Forms.PictureBoxSizeMode]::StretchImage
    if (Test-Path $iconPath) {
        try {
            $logo.Image = [System.Drawing.Icon]::ExtractAssociatedIcon($iconPath).ToBitmap()
        } catch {
        }
    }

    $title = New-Object System.Windows.Forms.Label
    $title.Text = "PROJECTX1"
    $title.Font = New-Object System.Drawing.Font("Segoe UI", 22, [System.Drawing.FontStyle]::Bold)
    $title.AutoSize = $true
    $title.ForeColor = [System.Drawing.Color]::FromArgb(255, 242, 245, 255)
    $title.Location = New-Object System.Drawing.Point(193, 16)

    $sub = New-Object System.Windows.Forms.Label
    $sub.Text = "Launcher Console"
    $sub.Font = New-Object System.Drawing.Font("Segoe UI Semibold", 9, [System.Drawing.FontStyle]::Regular)
    $sub.AutoSize = $true
    $sub.ForeColor = [System.Drawing.Color]::FromArgb(255, 157, 166, 196)
    $sub.Location = New-Object System.Drawing.Point(180, 58)

    $statusTitle = New-Object System.Windows.Forms.Label
    $statusTitle.Text = "SERVER STATUS"
    $statusTitle.Font = New-Object System.Drawing.Font("Segoe UI Semibold", 9, [System.Drawing.FontStyle]::Regular)
    $statusTitle.AutoSize = $true
    $statusTitle.ForeColor = [System.Drawing.Color]::FromArgb(255, 162, 170, 196)
    $statusTitle.Location = New-Object System.Drawing.Point(33, 98)

    $statusValue = New-Object System.Windows.Forms.Label
    $script:serverStatus = Get-ServerStatus
    $statusValue.Text = $script:serverStatus
    $statusValue.Font = New-Object System.Drawing.Font("Consolas", 16, [System.Drawing.FontStyle]::Bold)
    $statusValue.AutoSize = $true
    $statusValue.Location = New-Object System.Drawing.Point(32, 118)
    $statusValue.ForeColor = Get-StatusColor $script:serverStatus

    $btnUser = New-LauncherButton "USER MODE" 33 167 202 52 "primary"

    $btnAdmin = New-LauncherButton "ADMIN MODE" 252 167 202 52 "default"

    $btnStop = New-LauncherButton "STOP SERVERS" 33 235 421 44 "danger"

    $footer = New-Object System.Windows.Forms.Label
    $footer.Text = "Logs: logs\launcher-gui.log"
    $footer.AutoSize = $true
    $footer.ForeColor = [System.Drawing.Color]::FromArgb(255, 135, 142, 166)
    $footer.Location = New-Object System.Drawing.Point(33, 290)

    $btnLog = New-LauncherButton "LOG VIEW" 354 285 100 28 "subtle"
    $btnLog.Font = New-Object System.Drawing.Font("Segoe UI", 8, [System.Drawing.FontStyle]::Bold)

    $statusTimer = New-Object System.Windows.Forms.Timer
    $statusTimer.Interval = 2500
    $statusTimer.Add_Tick({
        $script:serverStatus = Get-ServerStatus
        $statusValue.Text = $script:serverStatus
        $statusValue.ForeColor = Get-StatusColor $script:serverStatus
    })
    $statusTimer.Start()

    $btnUser.Add_Click({
        Start-ProjectX1AndOpen "User" $userUrl
    })
    $btnAdmin.Add_Click({
        Start-ProjectX1AndOpen "Admin" $adminUrl
    })
    $btnStop.Add_Click({
        $killed = Stop-ProjectX1Servers
        if ($killed.Count -gt 0) {
            Write-LauncherLog "servers stopped (pid: $($killed -join ', '))"
            Show-Info "ProjectX1 servers stopped.`nPID: $($killed -join ', ')"
        } else {
            Show-Info "No running ProjectX1 servers found."
        }
        $script:isBooting = $false
        $script:serverStatus = Get-ServerStatus
    })
    $btnLog.Add_Click({
        if (-not (Test-Path $launcherLog)) {
            New-Item -ItemType File -Path $launcherLog -Force | Out-Null
        }
        Start-Process notepad.exe $launcherLog | Out-Null
    })

    $titleBar.Controls.AddRange(@($titleIcon, $titleText, $btnMin, $btnClose))
    $panel.Controls.AddRange(@($logo, $title, $sub, $statusTitle, $statusValue, $btnUser, $btnAdmin, $btnStop, $footer, $btnLog))
    $form.Controls.Add($titleBar)
    $form.Controls.Add($panel)
    try {
        [void]$form.ShowDialog()
    } finally {
        $statusTimer.Stop()
        $statusTimer.Dispose()
    }
}

Write-LauncherLog "launcher open"
try {
    Show-MainLauncher
} finally {
    try {
        $launcherMutex.ReleaseMutex() | Out-Null
        $launcherMutex.Dispose()
    } catch {
    }
}

