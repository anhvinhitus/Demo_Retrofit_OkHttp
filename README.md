ZaloPay Android
===============

Source code for ZaloPay Android

Structure
=========

```
| Project
|-- AddOns
|-- ModuleApi
|-- Core
|-- UI

```

Git rebase flow
===============

``` shell

git checkout master  # Check out the "public" branch 
git pull              # Get the latest version from remote
git checkout -b new_awsome_feature  # topical branch
... # do stuff here.. Make commits.. test...
git fetch origin      # Update your repository's origin/ branches from remote repo
git rebase origin/master  # Plop our commits on top of everybody else's
git checkout master  # Switch to the local tracking branch
git pull              # This won't result in a merge commit
git rebase new_awsome_feature  # Pull those commits over to the "public" branch
git push               # Push the public branch back up, with my stuff on the top

```

