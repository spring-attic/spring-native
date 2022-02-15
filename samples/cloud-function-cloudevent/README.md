### CloudEvents integration with spring-cloud-function 

This example demonstrates [CloudEvents](https://cloudevents.io/) integration with Spring Cloud Function.
In this example we will send CloudEvent over http having it being processed and returned back by Spring Cloud Function.
For additional details on CloudEvent integration in Spring Cloud Function please read these two blog posts - 
[CE Part One](https://spring.io/blog/2020/12/10/cloud-events-and-spring-part-1) and [CE Part Two](https://spring.io/blog/2020/12/23/cloud-events-and-spring-part-2) 

### Build project 
Builds a native-zip ZIP file in target by executing provided `build.sh` script

```
$ ./build.sh
```

Once built you can run the native image 

```
$> target/cloud-function-cloudevent
```

This will start the application inside the docker container. The application will listen on port 8081. 
Open up another CLI to the same docker image and execute the following curl command

```
curl -v "http://localhost:8081/" \
-H "Content-Type:application/json; charset=UTF-8" \
-H "X-Request-Id:bc3d6628-6bce-4beb-8c0a-e06212782934" \
-H "timestamp:1644485549319" \
-H "Forwarded-for:10.40.1.20" \
-H "Traceparent:00-534443e9aaa8e0ff7f8d9492af7df915-243e7c6d7d9e0a0e-00" \
-H "X-Forwarded-For:10.40.1.20" \
-H "K-Proxy-Request:activator" \
-H "User-Agent:Go-http-client/1.1" \
-H "Accept-Encoding:gzip" \
-H "Prefer:reply" \
-H "Host:level-1.default.svc.cluster.local" \
-H "X-Forwarded-Proto:http" \
-H "id:d1dec962-0704-3ca7-fcf8-1f8656a95909" \
-H "Ce-Id:1" \
-H "Ce-Subject:test" \
-H "Ce-Time:2022-02-10T09:32:27.875Z" \
-H "Ce-Source:cloud-event-example" \
-H "Ce-Type:KeyPressedEvent" \
-H "Ce-Specversion:1.0" \
-d "{\"key\": \"a\", \"position\": \"0\", \"timestamp\": \"1644312102224\"}\""
```

You shoudl see a response similar to this

```
< HTTP/1.1 200 
< x-request-id: bc3d6628-6bce-4beb-8c0a-e06212782934
< timestamp: 1644936144418
< forwarded-for: 10.40.1.20
< traceparent: 00-534443e9aaa8e0ff7f8d9492af7df915-243e7c6d7d9e0a0e-00
< x-forwarded-for: 10.40.1.20
< k-proxy-request: activator
< user-agent: Go-http-client/1.1
< accept-encoding: gzip
< prefer: reply
< x-forwarded-proto: http
< ce-id: 6a2a18c4-62ee-41a8-8786-5746d3964c3d
< ce-subject: test
< ce-time: 2022-02-10T09:32:27.875Z
< ce-source: https://key.pressed
< ce-type: KeyPressed-Echo
< ce-specversion: 1.0
< uri: /
< message-type: cloudevent
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 15 Feb 2022 14:42:24 GMT
```
