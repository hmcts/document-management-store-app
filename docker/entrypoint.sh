#!/bin/sh

### Variables ###

wait_for_database=false
wait_for_database_timeout=15

### Main ###

while :; do
  case $1 in
    -w|--wait-for-database)
      wait_for_database=true
      if [ -n "$2" ]; then
        wait_for_database_timeout=$2
        shift
      fi
      shift
      ;;
    --)
      shift
      break
      ;;
    *)
      break
  esac
done

if ${wait_for_database}; then
  database_address=${SPRING_DATASOURCE_URL}
  database_address=${database_address#*//} # remove protocol
  database_address=${database_address%/*} # remove path

  /usr/local/bin/wait-for-it.sh ${database_address} -s -t ${wait_for_database_timeout} -- java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar $@
else
  java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar $@
fi
