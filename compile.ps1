# compile.ps1 - Compila Problema 1 y Problema 2 (PowerShell)

if (-not (Test-Path bin)) {
    New-Item -ItemType Directory -Path bin
}

Write-Host "`nCompilando Problema 1 (First, Follow, GrammarLoader)..." -ForegroundColor Cyan
javac -encoding UTF-8 -d bin `
    Problema1/First.java `
    Problema1/Follow.java `
    Problema1/GrammarLoader.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR en la compilacion del Problema 1" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Problema 1 compilado correctamente." -ForegroundColor Green

Write-Host "`nCompilando Problema 2 (TableBuilder, TableDisplay)..." -ForegroundColor Cyan
javac -encoding UTF-8 -d bin -cp bin `
    Problema2/TableBuilder.java `
    Problema2/TableDisplay.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR en la compilacion del Problema 2" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Problema 2 compilado correctamente." -ForegroundColor Green

Write-Host "`nCompilacion exitosa. Usa:"
Write-Host "  .\run_p1.ps1    -> Ejecuta Problema 1"
Write-Host "  .\run_p2.ps1    -> Ejecuta Problema 2`n"
