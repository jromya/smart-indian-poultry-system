# Smart Indian Poultry - One-Click GitHub Repo Creation & Push Script
$ErrorActionPreference = "Continue"
Set-Location (Split-Path -Parent $MyInvocation.MyCommand.Path)

$gh = "C:\Program Files\GitHub CLI\gh.exe"
if (-not (Test-Path $gh)) {
    $gh = "gh"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Smart Indian Poultry -> GitHub + Render Cloud Publisher " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Authenticate GitHub CLI if not logged in
& $gh auth status >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[1/3] Logging in to GitHub in your browser..." -ForegroundColor Yellow
    & $gh auth login --hostname github.com --git-protocol https --web --skip-ssh-key
    & $gh auth status >$null 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`nERROR: GitHub sign-in did not complete due to network timeout. Please re-run .\push-to-github.bat" -ForegroundColor Red
        exit 1
    }
}

& $gh auth setup-git

# 2. Get GitHub username
$ghUserRaw = (& $gh api user --jq ".login" 2>$null)
if (-not $ghUserRaw) {
    Write-Host "ERROR: Unable to read GitHub username. Please check internet connection and retry." -ForegroundColor Red
    exit 1
}
$ghUser = $ghUserRaw.Trim()
$repoName = "smart-indian-poultry-system"
Write-Host "`n[2/3] Authenticated as GitHub user: $ghUser" -ForegroundColor Green

# 3. Create repo (if needed) and push main
& $gh repo view "$ghUser/$repoName" >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[3/3] Creating public repository $ghUser/$repoName and pushing main..." -ForegroundColor Yellow
    & $gh repo create $repoName --public --source=. --remote=origin --push
} else {
    Write-Host "[3/3] Repository exists. Pushing latest main branch..." -ForegroundColor Yellow
    git remote remove origin 2>$null
    git remote add origin "https://github.com/$ghUser/$repoName.git"
    git push -u origin main
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to push repository to GitHub." -ForegroundColor Red
    exit 1
}

$repoUrl = "https://github.com/$ghUser/$repoName"
$renderDeployUrl = "https://render.com/deploy?repo=$repoUrl"

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  SUCCESS! Code pushed to GitHub:" -ForegroundColor Green
Write-Host "  Repo URL         : $repoUrl" -ForegroundColor Cyan
Write-Host "  1-Click Render   : $renderDeployUrl" -ForegroundColor Magenta
Write-Host "==========================================================" -ForegroundColor Green
Start-Process $renderDeployUrl
