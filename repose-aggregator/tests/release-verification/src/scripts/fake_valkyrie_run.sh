echo "-------------------------------------------------------------------------------------------------------------------"
echo "Starting Fake Valkyrie"
echo "-------------------------------------------------------------------------------------------------------------------"

cd /opt/fake-valkyrie
node app.js 2>&1 >> /release-verification/fake-valkyrie.log &
sleep 2

echo "-------------------------------------------------------------------------------------------------------------------"
echo "Testing Fake Valkyrie"
echo "-------------------------------------------------------------------------------------------------------------------"
curl -vs http://127.0.0.1:6060
echo ""
