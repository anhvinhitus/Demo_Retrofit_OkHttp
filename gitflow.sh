#!/bin/sh

git fetch origin
git rebase origin/master
read -n1 -r -p "Press any key to continue..." key
git checkout master
git pull
git rebase wip
git push origin master
git branch -D wip
git checkout -b wip

