# Nayankumar Sorathiya (40227432)
# Naren Zadfiya (40232646)

Phase :1
# HTTP Server Implementation
    httpc get -v http://localhost:8080/get?course=networking&assignment=1

    httpc get -h Content-Type:application/json http://localhost:8080/get?course=networking

    httpc get -v -h Content-Type:application/json http://localhost:8080/get?course=networking&assignment=1

    ### For POST request ====>>
    httpc post -h Content-Type:application/json -d '{"Assignment":1}' http://localhost:8080/post

    httpc post -h Content-Type:application/json -h Content-Type:application/json -d '{"Assignment":1}' http://localhost:8080/post

    httpc post -v -d '{"Assignment":1}' http://localhost:8080/post

    httpc post -v -h Content-Type:application/json -d '{"Assignment":1}' http://localhost:8080/post

    httpc post -h Content-Type:application/json -d '{"Assignment":1}' http://localhost:8080/post?course=networking&assignment=1

    httpc post -v -h Content-Type:application/json -d '{"Assignment":1}' http://localhost:8080/post?course=networking&assignment=1

    httpc post -h Content-Type:application/json -d '{"Assignment":1}' http://localhost:8080/post?course=networking&assignment=1 -o result.txt

	httpc post -v -f file.json http://localhost:8080/post?course=networking&assignment=1

## Run the server file
    httpfs -v -p 8080 -d /src

    httpfs -v -p 8080 -d E:\Concordia\banking

    httpfs -v

## Run FTPClient (httpc option)
	httpfs http://localhost:8080/get/
	httpfs http://localhost:8080/get/test.txt
	httpfs http://localhost:8080/post/test.txt -d '{"Assignment":2}'
	httpfs http://localhost:8080/get/test99.txt
	httpfs -h overwrite:true http://localhost:8080/post/test.txt -d '{"test":"nayan}'
	httpfs http://localhost:8080/post/123.txt -d naren
    httpfs -h overwrite:true http://localhost:8080/post/123.txt -d nayan
    httpfs -h overwrite:false http://localhost:8080/post/123.txt -d naren

## Content-Type Filteration
	httpc -h Content-Type:json http://localhost:8080/get/
    httpc -h Content-Type:html http://localhost:8080/get/
    httpc -h Content-Type:txt http://localhost:8080/get/

## Content-Disposition
     httpc -h Content-Disposition:attachment http://localhost:8080/get/test.txt

## Concurrent Scripts
    ./script.sh 10
    ./script2.sh 
    ./script3.sh 10