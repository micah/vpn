#!/bin/bash
set -e
source secrets.properties

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

if [[ -z $USER_NAME || -z $ACCESS_KEY ]]; then
  quit "Missing secrets.properties or missing environment variables USER_NAME and ACCESS_KEY"
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
-d "{\"app\": \"${APP_URL}\", \"testSuite\": \"${TEST_APP_URL}\", \"devices\": [\"Samsung Galaxy S9-8.0\"]}" \
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



