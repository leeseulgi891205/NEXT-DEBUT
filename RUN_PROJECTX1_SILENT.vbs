Option Explicit

Dim fso, shell, root, psScript, psExe, logsDir, vbsLog, cmd, fh
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")

root = fso.GetParentFolderName(WScript.ScriptFullName)
psScript = root & "\tools\launch_projectx1_gui.ps1"
psExe = shell.ExpandEnvironmentStrings("%WINDIR%") & "\System32\WindowsPowerShell\v1.0\powershell.exe"
logsDir = root & "\logs"
vbsLog = logsDir & "\launcher-vbs.log"

If Not fso.FolderExists(logsDir) Then
    fso.CreateFolder logsDir
End If

Set fh = fso.OpenTextFile(vbsLog, 8, True)
fh.WriteLine "[" & Now & "] vbs start"
fh.WriteLine "[" & Now & "] script=" & psScript
fh.WriteLine "[" & Now & "] powershell=" & psExe
fh.Close

cmd = """" & psExe & """" & " -NoProfile -ExecutionPolicy Bypass -Sta -WindowStyle Hidden -File """ & psScript & """"
shell.Run cmd, 0, False

