#!/bin/bash

# Ensure we're on dev and up to date
git checkout dev
git pull origin dev

# Array of feature branches in order
branches=(
    "feature/01-scaffold"
    "feature/02-db-schema"
    "feature/03-auth"
    "feature/04-manage-users"
    "feature/05-manage-questions"
    "feature/06-talentup-sync"
    "feature/07-manager-dashboard"
    "feature/08-admin-settings"
)

# Create each branch and push
for branch in "${branches[@]}"
do
    echo "Creating branch: $branch"
    git checkout -b "$branch"
    git push -u origin "$branch"
    
    # Optional: Add initial commit to show branch exists
    echo "# $branch - Work in progress" > "BRANCH_README.md"
    git add BRANCH_README.md
    git commit -m "chore: initialize $branch branch"
    git push origin "$branch"
    
    # Return to dev for next branch
    git checkout dev
    echo "✅ Branch $branch created and pushed"
    echo "---"
done

echo "🎉 All branches created successfully!"
git checkout dev
