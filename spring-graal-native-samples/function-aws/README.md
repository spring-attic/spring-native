AWS Lambda custom runtime.

```
$ ./build.sh
$ ./mvnw package -P native
```
 
builds a native-zip ZIP file in target. Upload it to AWS and set the handler to "foobar".

To test locally, run the `TestServer` and then the `DemoApplication` (either in a JVM or natively). Then POST some data into the test server:

```
$ curl localhost:8000/add -d world -H "Content-Type: text/plain"
```

There is a unit test that does the same thing. Also the `build.sh` script orchestrates the same test for the native image.
