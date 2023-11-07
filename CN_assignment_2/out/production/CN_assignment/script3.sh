#!/bin/bash

N_CLIENTS=$1
COMMAND="httpc http://localhost:8080/post/test.txt -d"

for ((i=0; i<N_CLIENTS; i++)); do
    PAYLOAD="{\"test\":$i}"
    echo "$COMMAND '$PAYLOAD'" | java FtpClient &
done

wait
