# test-structure.ps1 - 验证项目目录结构
# AGENTS.md 要求: 改动后必须测试通过

$pass = $true

Write-Host "=== Test 1: .trae/rules/AGENTS.md exists ==="
if (Test-Path .\.trae\rules\AGENTS.md) {
    Write-Host "[PASS] .trae/rules/AGENTS.md exists"
} else {
    Write-Host "[FAIL] .trae/rules/AGENTS.md not found"
    $pass = $false
}

Write-Host ""
Write-Host "=== Test 2: AGENTS.md content has 'AGENTS' header ==="
$content = Get-Content .\.trae\rules\AGENTS.md -Raw
if ($content -match "# AGENTS") {
    Write-Host "[PASS] AGENTS.md contains '# AGENTS'"
} else {
    Write-Host "[FAIL] AGENTS.md missing '# AGENTS' header"
    $pass = $false
}

Write-Host ""
Write-Host "=== Test 3: Old AGENTS.md removed from root ==="
if (-not (Test-Path .\AGENTS.md)) {
    Write-Host "[PASS] root AGENTS.md removed"
} else {
    Write-Host "[FAIL] root AGENTS.md still exists"
    $pass = $false
}

Write-Host ""
Write-Host "=== Test 4: settings.xml exists at project root ==="
if (Test-Path .\settings.xml) {
    Write-Host "[PASS] settings.xml exists at root"
} else {
    Write-Host "[FAIL] settings.xml missing at root"
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
