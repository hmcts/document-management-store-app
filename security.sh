#!/usr/bin/env bash
echo ${TEST_URL}
echo "ZAP has successfully started"
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL -J report.json -r api-report.html
mkdir -p functional-output
chmod a+wx functional-output
cp api-report.html functional-output/
