#!/bin/bash

N_CLIENTS=$1
COMMAND="httpfs get/test.txt http://localhost:8080/get"

for ((i=0; i<N_CLIENTS; i++)); do
    echo "$COMMAND" | java FtpClient &
done

wait
