# Smart Indian Poultry Farm Management System - Startup Script (PowerShell)

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "   Smart Indian Poultry Farm Management System (Spring Boot 3)   " -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Cyan

# Load local .env (gitignored) if present (e.g., MongoDB Atlas MONGODB_URI)
$envFile = Join-Path $ProjectDir ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.+)$") {
            [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim(), "Process")
        }
    }
}

# 1. Check MongoDB (Cloud MONGODB_URI or localhost:27017)
if ($env:MONGODB_URI) {
    Write-Host "`n[1/3] Using Cloud MongoDB Atlas URI from MONGODB_URI (.env)!" -ForegroundColor Green
} else {
    Write-Host "`n[1/3] Checking MongoDB on localhost:27017..." -ForegroundColor Yellow
$mongoRunning = $false
try {
    $tcp = New-Object System.Net.Sockets.TcpClient("127.0.0.1", 27017)
    if ($tcp.Connected) {
        $mongoRunning = $true
        $tcp.Close()
    }
} catch {
    $mongoRunning = $false
}

if ($mongoRunning) {
    Write-Host "  -> MongoDB is RUNNING on port 27017." -ForegroundColor Green
} else {
    Write-Host "  -> MongoDB is NOT running on port 27017." -ForegroundColor Red
    $mongoExe = "C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe"
    if (Test-Path $mongoExe) {
        $dataDir = Join-Path $ProjectDir ".mongodb-data"
        New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
        Write-Host "  -> Starting local MongoDB using data folder: $dataDir" -ForegroundColor Cyan
        Start-Process $mongoExe -ArgumentList "--dbpath=""$dataDir""" -WindowStyle Minimized
        Start-Sleep -Seconds 3
        Write-Host "  -> MongoDB started." -ForegroundColor Green
    } else {
        Write-Host "  -> Please start MongoDB service ('net start MongoDB' in Admin CMD)." -ForegroundColor Yellow
    }
}
}

# 2. Check Java
Write-Host "`n[2/3] Checking Java JDK..." -ForegroundColor Yellow
$javaCmd = Get-Command "java" -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "  -> ERROR: Java 17+ / 21 is not in PATH!" -ForegroundColor Red
    exit 1
}
Write-Host "  -> Java detected." -ForegroundColor Green

# 3. Locate Maven (PATH -> IntelliJ Bundled -> Local .tools)
Write-Host "`n[3/3] Locating Apache Maven..." -ForegroundColor Yellow
$mvnCmd = Get-Command "mvn" -ErrorAction SilentlyContinue
$ijMaven = Get-ChildItem "C:\Program Files\JetBrains\IntelliJ*\plugins\maven\lib\maven3\bin\mvn.cmd" -ErrorAction SilentlyContinue | Select-Object -Last 1

if ($mvnCmd) {
    $mvnPath = "mvn"
} elseif ($ijMaven) {
    $mvnPath = $ijMaven.FullName
} else {
    $toolsDir = Join-Path $ProjectDir ".tools"
    $mvnLocal = Join-Path $toolsDir "apache-maven-3.9.9\bin\mvn.cmd"
    if (-not (Test-Path $mvnLocal)) {
        New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
        $zipPath = Join-Path $toolsDir "maven.zip"
        Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip" -OutFile $zipPath
        Expand-Archive -Path $zipPath -DestinationPath $toolsDir -Force
        Remove-Item $zipPath -Force
    }
    $mvnPath = $mvnLocal
}

Write-Host "  -> Using Maven: $mvnPath" -ForegroundColor Green
Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "  Web App URL  : http://localhost:8080/index.html" -ForegroundColor Magenta
Write-Host "  Login Screen : http://localhost:8080/login.html" -ForegroundColor Magenta
Write-Host "  Swagger Docs : http://localhost:8080/swagger-ui.html" -ForegroundColor Magenta
Write-Host "=================================================================`n" -ForegroundColor Cyan

& $mvnPath spring-boot:run
