
MESSAGE=$(git log -1 HEAD --pretty=format:%s)
if [[ "$MESSAGE" == *\[Sandbox\]\ Bump\ version* ]]; then
echo "Build SandBox"
cd ./ZaloPay
./deploy.sh sandbox
else if [[ "$MESSAGE" == *\[Staging\]\ Bump\ version* ]]; then
echo "Build Staging"
cd ./ZaloPay
./deploy.sh sandbox
else if [[ "$MESSAGE" == *\[Production\]\ Bump\ version* ]]; then
echo "Build Production"
cd ./ZaloPay
./deploy.sh sandbox
fi
fi
fi

