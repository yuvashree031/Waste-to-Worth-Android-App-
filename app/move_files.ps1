# Create target directory if it doesn't exist
$targetDir = "src\main\java\com\example\wastetoworth"
New-Item -ItemType Directory -Force -Path $targetDir

# List of files to move
$filesToMove = @(
    "src\main\java\DonationModel.java",
    "src\main\java\DonationAdapter.java",
    "src\main\java\ReceiveActivity.java"
)

# Move each file to the target directory
foreach ($file in $filesToMove) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination $targetDir -Force
        Write-Host "Moved $file to $targetDir"
    } else {
        Write-Host "File not found: $file"
    }
}

Write-Host "File move operation completed."
