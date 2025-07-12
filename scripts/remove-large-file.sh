#!/bin/bash

# Script to remove large files from git history

echo "Removing old-modules-backup.zip from git history..."

# Use git filter-branch to remove the file
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch old-modules-backup.zip' \
  --prune-empty --tag-name-filter cat -- --all

# Clean up
echo "Cleaning up..."
rm -rf .git/refs/original/
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo "Done! The repository size should be reduced now."
echo "You'll need to force push to update the remote repository:"
echo "git push --force-with-lease origin main"