#!/bin/bash
set -e

RED='\033[0;31m'
NC='\033[0m'
BOLD='\033[1m'

function quit {
    echo -e "${RED}Task failed. $1 ${NC}"
    exit 1
}

function showtitle {
    echo -e "\n${BOLD}$1 ${NC}"
}


mkdir -p "reports"

if [[ -e secrets.properties ]]; then
  source secrets.properties
else
  echo "Missing secrets.properties file. Make sure you provide \$USER_NAME and \$ACCESS_KEY by other means."
fi

if [[ -z $USER_NAME || -z $ACCESS_KEY ]]; then
  quit "Missing environment variables USER_NAME and ACCESS_KEY."
fi

showtitle "Building APKs"
./gradlew -q clean assembleDebug assembleDebugAndroidTest

showtitle "Uploading Debug APK"
APP_URL=$(curl -u "$USER_NAME:$ACCESS_KEY" \
-X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/app" \
-F "file=@app/build/outputs/apk/debug/app-debug.apk" | jq -r .app_url)
echo $APP_URL

showtitle "Uploading Test APK"
TEST_APP_URL=$(curl -u "$USER_NAME:$ACCESS_KEY" \
-X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/test-suite" \
-F "file=@app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" | jq -r .test_suite_url)
echo $TEST_APP_URL

showtitle "Submit Espresso test"
BUILD_ID=$(curl -s -u "$USER_NAME:$ACCESS_KEY" \
-X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/build" \
-d "{\"project\" : \"TorVPN\", \"app\": \"${APP_URL}\", \"testSuite\": \"${TEST_APP_URL}\", \"deviceLogs\" : true, \"networkLogs\" : true, \"devices\": [\"Samsung Galaxy S9-8.0\", \"Samsung Galaxy Note 20-10.0\", \"Xiaomi Redmi Note 11-11.0\", \"Google Pixel 9 Pro XL-15.0\"]}" \
-H "Content-Type: application/json" | jq -r .build_id)
echo "BUILD_ID: $BUILD_ID"

if [[ -z $BUILD_ID ]]; then
    quit "Failed to fetch build ID"
fi

showtitle "Get SESSION_ID"
SESSION_IDS=$(curl -s -u "$USER_NAME:$ACCESS_KEY" \
  -X GET "https://api-cloud.browserstack.com/app-automate/espresso/v2/builds/$BUILD_ID" | jq -r '.devices[].sessions[].id')
echo "SESSION_ID: $SESSION_IDS"

if [[ -z $SESSION_IDS ]]; then
    quit "Failed to fetch session IDs"
fi

showtitle "Wait until Espresso tests finish"
TEST_FAILED=0
# Loop through each SESSION_ID
for SESSION_ID in $SESSION_IDS; do
    echo "Checking status of test with SESSION_ID: $SESSION_ID"
    # Loop for a maximum of 180 attempts / 30 minutes
    for (( attempt=1; attempt<=180; attempt++ )); do
        # Make the curl request and capture the response
        response=$(curl -s -u "$USER_NAME:$ACCESS_KEY" -X GET "https://api-cloud.browserstack.com/app-automate/espresso/v2/builds/$BUILD_ID/sessions/$SESSION_ID")

        if echo "$response" | jq -e '.error' > /dev/null; then
            # If there's an error field, continue with next SESSION_ID
            echo "Error fetching build result: $response"
            break
        # Check if the response contains an error field
        elif echo "$response" | jq -e '.status' > /dev/null; then
            # If there's no error field, break the loop
            STATUS=$(echo "$response" | jq -r '.status')
            case "$STATUS" in
                running)
                    # Still processing
                    echo -n ".."
                    sleep 10
                    ;;
                success)
                    echo "SUCCESS"
                    break
                    ;;
                failed)
                    echo "FAILED"
                    ((TEST_FAILED++))
                    break
                    ;;
                *)
                    echo "DONE"
                    echo "Result: $response"
                    break
                   ;;
            esac
        else
            # If there's an error, print the error message
            echo "Unknown response: $response"
            break
        fi
    done
    echo "Fetching report and saving to reports/${BUILD_ID}-${SESSION_ID}.xml"
    curl -s -u "$USER_NAME:$ACCESS_KEY" -X GET  "https://api-cloud.browserstack.com/app-automate/espresso/v2/builds/$BUILD_ID/sessions/$SESSION_ID/report" > reports/${BUILD_ID}-${SESSION_ID}.xml
done


if [[ $TEST_FAILED > 0 ]]; then
    quit "At least one test on $TEST_FAILED device(s) failed."
fi
