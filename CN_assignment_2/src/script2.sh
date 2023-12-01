#!/bin/bash

# Commands
READ_COMMAND="httpfs get/test.txt http://localhost:8080/get"
WRITE_COMMAND="httpfs post/test.txt http://localhost:8080/post -d '{\"test\":123}'"

# Start the write client in the background
echo "$WRITE_COMMAND" | java FtpClient &

# Sleep for a bit to ensure the read happens while the write is occurring
sleep 1

# Start the read client in the background
echo "$READ_COMMAND" | java FtpClient &

# Wait for all background processes to finish
wait