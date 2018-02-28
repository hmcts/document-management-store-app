#!/bin/sh
IDAM_USER_BASE_URL=http://localhost:4501
IDAM_S2S_BASE_URL=http://localhost:4502
DIR="$( cd "$( dirname "$0" )" && pwd )/"

CREATE_IDAM_USER="${DIR}idam-create-user.sh"
GET_IDAM_USER_TOKEN="${DIR}idam-get-user-token.sh"
GET_IDAM_S2S_TOKEN="${DIR}idam-get-s2s-token.sh"

${CREATE_IDAM_USER} user1a@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user2a@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user3a@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user1b@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user2b@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user3b@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user1c@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user2c@test.com 123 ${IDAM_USER_BASE_URL}
${CREATE_IDAM_USER} user3c@test.com 123 ${IDAM_USER_BASE_URL}

${GET_IDAM_USER_TOKEN} user1a@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user2a@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user3a@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user1b@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user2b@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user3b@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user1c@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user2c@test.com 123 ${IDAM_USER_BASE_URL}
${GET_IDAM_USER_TOKEN} user3c@test.com 123 ${IDAM_USER_BASE_URL}

${CREATE_IDAM_USER} test@TEST.COM 123 ${IDAM_USER_BASE_URL}
echo "Authorization:"$(${GET_IDAM_USER_TOKEN} test@TEST.COM 123 ${IDAM_USER_BASE_URL})
echo "ServiceAuthorization:"$(${GET_IDAM_S2S_TOKEN} sscs ${IDAM_S2S_BASE_URL})
