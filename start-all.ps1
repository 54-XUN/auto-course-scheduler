# One-click startup for the scheduling system (backend + frontend)
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root "backend"
$frontendDir = Join-Path $root "frontend"
$jarPath = Join-Path $backendDir "target\scheduling-1.0.0.jar"

$env:JWT_SECRET = "your-256-bit-secret-your-256-bit-secret"
$env:DB_PASSWORD = "root123"
$env:DEFAULT_ADMIN_PASSWORD = "admin123"

function Test-PortInUse($port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen, Established -ErrorAction SilentlyContinue
    return $conn -ne $null
}

function Wait-PortListening($port, $name) {
    $ready = $false
    for ($i = 0; $i -lt 120; $i++) {
        if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
            $ready = $true
            break
        }
        Start-Sleep -Milliseconds 500
    }
    if (-not $ready) {
        Write-Host "$name failed to start. Check the window above for errors." -ForegroundColor Red
        exit 1
    }
    Write-Host "$name is ready." -ForegroundColor Green
}

if (Test-PortInUse -port 8080) {
    Write-Host "ERROR: port 8080 is already in use. Close the previous backend first." -ForegroundColor Red
    exit 1
}
if (Test-PortInUse -port 5173) {
    Write-Host "ERROR: port 5173 is already in use. Close the previous frontend first." -ForegroundColor Red
    exit 1
}

# Build backend jar if missing or outdated
if (-not (Test-Path $jarPath)) {
    Write-Host "Building backend (first run)..." -ForegroundColor Cyan
    & (Join-Path $root "scripts\mvn.bat") package -DskipTests -f (Join-Path $backendDir "pom.xml")
} elseif ((Get-Item (Join-Path $backendDir "pom.xml")).LastWriteTime -gt (Get-Item $jarPath).LastWriteTime) {
    Write-Host "pom.xml updated, rebuilding backend..." -ForegroundColor Cyan
    & (Join-Path $root "scripts\mvn.bat") package -DskipTests -f (Join-Path $backendDir "pom.xml")
} else {
    Write-Host "Backend jar is up to date, skip build." -ForegroundColor Green
}

# Start backend
Write-Host "Starting backend on port 8080..." -ForegroundColor Cyan
$jwt = $env:JWT_SECRET
$db = $env:DB_PASSWORD
$admin = $env:DEFAULT_ADMIN_PASSWORD
$backendCmd = "cd '$backendDir'; `$env:JWT_SECRET=`"$jwt`"; `$env:DB_PASSWORD=`"$db`"; `$env:DEFAULT_ADMIN_PASSWORD=`"$admin`"; java -jar target\scheduling-1.0.0.jar"
Start-Process powershell -ArgumentList @("-NoExit", "-Command", $backendCmd) -WindowStyle Minimized
Wait-PortListening -port 8080 -name "Backend"

# Start frontend
Write-Host "Starting frontend on port 5173..." -ForegroundColor Cyan
$frontendCmd = "cd '$frontendDir'; npm run dev"
Start-Process powershell -ArgumentList @("-NoExit", "-Command", $frontendCmd) -WindowStyle Minimized
Wait-PortListening -port 5173 -name "Frontend"

# Open browser
Write-Host "Opening browser..." -ForegroundColor Cyan
Start-Process "http://localhost:5173"

Write-Host ""
Write-Host "All started! Default login: admin / admin123" -ForegroundColor Green
Write-Host "Close the two minimized PowerShell windows to stop services." -ForegroundColor Yellow
