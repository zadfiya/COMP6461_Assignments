
# ********    Assignment 1 : Computer Networks    *********

## Compile the java file with below Command :  
- javac Httpc.java Constant.java

## Run the file with below command : 
- java Httpc

## Test the code with following http request :--

## General Usage:
- httpc help

## Get Usage:
- httpc help get

## Post Usage:
- httpc help post


## GET Request
- httpc get http://httpbin.org/get
## Header
- httpc get -h Authorization:token http://httpbin.org/get
## Post Request
- httpc post -d '{userName:NayanSorathiya}' http://httpbin.org/post


# Get Requests

### with verbose option
- httpc get -v http://httpbin.org/get

### with Query Parameters
- httpc get -v http://httpbin.org/get?marker=mario

### with Header Option
- httpc get -h Authorizarion:token name:naren http://httpbin.org/get


# Post Requests

### with file.json
- httpc post -v -f file.json http://httpbin.org/post

### with inline Data
- httpc post -d '{userName:NayanSorathiya}' -v http://httpbin.org/post
- httpc post -h Content-Type:application/json -d '{userName:Naren}' -v http://httpbin.org/post

## with -o option
- httpc post -h Content-Type:application/json -d '{userName:Naren}' -v http://httpbin.org/post -o post.txt

# Bonus part Redirection:
- httpc get -v http://httpbin.org/redirect/1
- httpc get -v http://httpbin.org/redirect/1 -o demo.txt

# ****************************************************





