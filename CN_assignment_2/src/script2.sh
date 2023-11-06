#!/bin/bash

# Commands
READ_COMMAND="httpc http://localhost:8080/get/test.txt"
WRITE_COMMAND="httpc http://localhost:8080/post/test.txt -d '{\"test\":123}'"

# Start the write client in the background
echo "$WRITE_COMMAND" | java FtpClient &

# Sleep for a bit to ensure the read happens while the write is occurring
sleep 1

# Start the read client in the background
echo "$READ_COMMAND" | java FtpClient &

# Wait for all background processes to finish
wait