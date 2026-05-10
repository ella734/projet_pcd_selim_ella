# Add Maven to PATH
$mavenBin = "C:\Users\ellaa\Downloads\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin"
$mavenCmd = Join-Path $mavenBin "mvn.cmd"
$npmCmd = Join-Path $env:ProgramFiles "nodejs\npm.cmd"
$dockerCmd = "docker"

$env:PATH = "$mavenBin;" + $env:PATH

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $baseDir ".env"

function Wait-ForTcpPort {
    param(
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-NetConnection -ComputerName $HostName -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue) {
            return $true
        }
        Start-Sleep -Seconds 2
    }

    throw "Timed out waiting for $HostName`:$Port after $TimeoutSeconds seconds."
}

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if (-not $_ -or $_.Trim().StartsWith("#")) { return }
        $parts = $_ -split "=", 2
        if ($parts.Count -eq 2) {
            [System.Environment]::SetEnvironmentVariable($parts[0], $parts[1], "Process")
        }
    }
}

Write-Host "==== STARTING DATABASE SERVICES ===="
& $dockerCmd compose up -d mysql neo4j
if ($LASTEXITCODE -ne 0) {
    throw "Failed to start mysql/neo4j containers with docker compose."
}

Write-Host "Waiting for MySQL on localhost:3307..."
Wait-ForTcpPort -HostName "localhost" -Port 3307
Write-Host "Waiting for Neo4j on localhost:7687..."
Wait-ForTcpPort -HostName "localhost" -Port 7687

Write-Host "==== STARTING BACKEND ===="
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\backend'; & { Get-Content '$envFile' | ForEach-Object { if (`$_ -and -not `$_.Trim().StartsWith('#')) { `$parts = `$_ -split '=', 2; if (`$parts.Count -eq 2) { [System.Environment]::SetEnvironmentVariable(`$parts[0], `$parts[1], 'Process') } } }; `$env:PATH = '$mavenBin;' + `$env:PATH; & '$mavenCmd' clean spring-boot:run }" -WindowStyle Normal

Write-Host "==== STARTING FRONTEND ===="
Start-Sleep -Seconds 5
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\frontend'; & '$npmCmd' run dev" -WindowStyle Normal

Write-Host ""
Write-Host "Applications starting..."
Write-Host "Backend will be available at: http://localhost:8080"
Write-Host "Frontend will be available at: http://localhost:3000"

