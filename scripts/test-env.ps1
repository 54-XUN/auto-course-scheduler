# test-env.ps1 - 验证 java/mvn wrapper 是否工作
# AGENTS.md 要求: 改动后必须测试通过

$pass = $true

Write-Host "=== Test 1: java.bat -version ==="
$out = & ".\scripts\java.bat" -version 2>&1 | Out-String
if ($out -match "openjdk" -and $LASTEXITCODE -eq 0) {
    Write-Host "[PASS] Java version output contains 'openjdk' (exit code 0)"
} else {
    Write-Host "[FAIL] exit=$LASTEXITCODE, output=$out"
    $pass = $false
}

Write-Host ""
Write-Host "=== Test 2: mvn.bat -version ==="
$out = & ".\scripts\mvn.bat" -version 2>&1 | Out-String
if ($out -match "Apache Maven" -and $LASTEXITCODE -eq 0) {
    Write-Host "[PASS] Maven version output contains 'Apache Maven' (exit code 0)"
} else {
    Write-Host "[FAIL] exit=$LASTEXITCODE, output=$out"
    $pass = $false
}

Write-Host ""
if ($pass) {
    Write-Host "All tests passed."
    exit 0
} else {
    Write-Host "Some tests failed."
    exit 1
}
