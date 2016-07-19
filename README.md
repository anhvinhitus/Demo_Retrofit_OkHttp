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

Build APK for release
=====================

Cập nhật version
----------------

* nếu chỉ cập nhật không liên quan đến payment apps, internal react native module: tăng versionCode
* ngược lại: reset versionCode = 1, tăng số cuối của versionName

Thêm tag
--------

* commit phần vừa sửa cho thay đổi versionCode, versionName, commit message: "Bump version"
* tag commit theo cú pháp v$(versionName)

Build APK
---------

* build apk từ command line:

``` bash
$ cd ZaloPay
$ ./gradlew assembleProductionRelease -PCI_BUILD=YES
$ ./gradlew assembleStagingRelease -PCI_BUILD=YES
$ ./gradlew assembleSandboxRelease -PCI_BUILD=YES

```

sau khi build, APK sẽ được tạo ở thư mục `../releases` với tên là `ZaloPay-production-release-v$(versionName).apk`
