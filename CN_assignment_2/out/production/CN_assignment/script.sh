#!/bin/bash

N_CLIENTS=$1
COMMAND="httpc http://localhost:8080/get/test.txt"

for ((i=0; i<N_CLIENTS; i++)); do
    echo "$COMMAND" | java FtpClient &
done

wait
