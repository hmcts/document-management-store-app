#!/bin/sh
echo $(ps -aux | grep gradle | grep -v grep | awk '{print $2}');
kill -9 $(ps -aux | grep gradle| grep -v grep | awk '{print $2}');
