# 一键启动排课系统（后端 + 前端）
$ErrorActionPreference = "Stop"

# 项目根目录
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root "backend"
$frontendDir = Join-Path $root "frontend"
$jarPath = Join-Path $backendDir "target\scheduling-1.0.0.jar"

# 环境变量
$env:JWT_SECRET = "your-256-bit-secret-your-256-bit-secret"
$env:DB_PASSWORD = "root123"
$env:DEFAULT_ADMIN_PASSWORD = "admin123"

function Test-PortInUse($port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen, Established -ErrorAction SilentlyContinue
    return $conn -ne $null
}

# 检查端口占用
if (Test-PortInUse -port 8080) {
    Write-Host "错误：端口 8080 已被占用，请先关闭之前运行的后端进程。" -ForegroundColor Red
    Write-Host "可以打开任务管理器结束 java.exe，或关闭之前的 PowerShell 窗口。" -ForegroundColor Yellow
    exit 1
}

if (Test-PortInUse -port 5173) {
    Write-Host "错误：端口 5173 已被占用，请先关闭之前运行的前端进程。" -ForegroundColor Red
    Write-Host "可以关闭之前的 PowerShell/Node 窗口。" -ForegroundColor Yellow
    exit 1
}

# 构建后端 jar（如果不存在或 pom.xml 更新过）
if (-not (Test-Path $jarPath)) {
    Write-Host "首次运行，正在构建后端..." -ForegroundColor Cyan
    & (Join-Path $root "scripts\mvn.bat") package -DskipTests -f (Join-Path $backendDir "pom.xml")
} elseif ((Get-Item (Join-Path $backendDir "pom.xml")).LastWriteTime -gt (Get-Item $jarPath).LastWriteTime) {
    Write-Host "pom.xml 有更新，正在重新构建后端..." -ForegroundColor Cyan
    & (Join-Path $root "scripts\mvn.bat") package -DskipTests -f (Join-Path $backendDir "pom.xml")
} else {
    Write-Host "后端 jar 已是最新，跳过构建。" -ForegroundColor Green
}

# 启动后端
Write-Host "正在启动后端服务（端口 8080）..." -ForegroundColor Cyan
$jwt = $env:JWT_SECRET
$db = $env:DB_PASSWORD
$admin = $env:DEFAULT_ADMIN_PASSWORD
$backendCmd = "cd '$backendDir'; `$env:JWT_SECRET=`"$jwt`"; `$env:DB_PASSWORD=`"$db`"; `$env:DEFAULT_ADMIN_PASSWORD=`"$admin`"; java -jar target\scheduling-1.0.0.jar"
Start-Process powershell -ArgumentList @("-NoExit", "-Command", $backendCmd) -WindowStyle Minimized

# 等待后端就绪
Write-Host "等待后端启动..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 120; $i++) {
    if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 500
}
if (-not $ready) {
    Write-Host "后端启动超时，请检查后端窗口中的错误信息。" -ForegroundColor Red
    exit 1
}
Write-Host "后端已就绪。" -ForegroundColor Green

# 启动前端
Write-Host "正在启动前端服务（端口 5173）..." -ForegroundColor Cyan
$frontendCmd = "cd '$frontendDir'; npm run dev"
Start-Process powershell -ArgumentList @("-NoExit", "-Command", $frontendCmd) -WindowStyle Minimized

# 等待前端就绪
Write-Host "等待前端启动..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 120; $i++) {
    if (Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 500
}
if (-not $ready) {
    Write-Host "前端启动超时，请检查前端窗口中的错误信息。" -ForegroundColor Red
    exit 1
}
Write-Host "前端已就绪。" -ForegroundColor Green

# 打开浏览器
Write-Host "正在打开浏览器..." -ForegroundColor Cyan
Start-Process "http://localhost:5173"

Write-Host "`n启动完成！默认账号：admin / admin123" -ForegroundColor Green
Write-Host "关闭弹出的两个 PowerShell 窗口即可停止服务。" -ForegroundColor Yellow
