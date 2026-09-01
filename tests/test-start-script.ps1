# Validate start-all.ps1 / start-all.bat encoding and syntax
# Both files must be pure ASCII so Windows PowerShell 5.1 and cmd parse them correctly.
$fail = $false

# 1. start-all.ps1 must be pure ASCII (no BOM, no multi-byte chars)
$ps1 = [System.IO.File]::ReadAllBytes("$PSScriptRoot\..\start-all.ps1")
$nonAsciiPs1 = @($ps1 | Where-Object { $_ -gt 0x7F })
if ($nonAsciiPs1.Count -gt 0) {
    Write-Host "[FAIL] start-all.ps1 contains $($nonAsciiPs1.Count) non-ASCII bytes" -ForegroundColor Red
    $fail = $true
} else {
    Write-Host "[PASS] start-all.ps1 is pure ASCII" -ForegroundColor Green
}

# 2. start-all.ps1 must parse without syntax errors
$errors = $null
$tokens = $null
[System.Management.Automation.Language.Parser]::ParseFile("$PSScriptRoot\..\start-all.ps1", [ref]$tokens, [ref]$errors) | Out-Null
if ($errors.Count -gt 0) {
    Write-Host "[FAIL] start-all.ps1 syntax errors:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host $_.Message -ForegroundColor Red }
    $fail = $true
} else {
    Write-Host "[PASS] start-all.ps1 syntax OK" -ForegroundColor Green
}

# 3. start-all.bat must be pure ASCII (cmd reads it as GBK; Chinese chars break it)
$bat = [System.IO.File]::ReadAllBytes("$PSScriptRoot\..\start-all.bat")
$nonAsciiBat = @($bat | Where-Object { $_ -gt 0x7F })
if ($nonAsciiBat.Count -gt 0) {
    Write-Host "[FAIL] start-all.bat contains $($nonAsciiBat.Count) non-ASCII bytes" -ForegroundColor Red
    $fail = $true
} else {
    Write-Host "[PASS] start-all.bat is pure ASCII" -ForegroundColor Green
}

if ($fail) { exit 1 } else { exit 0 }
