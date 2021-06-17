AWS Lambda custom runtime.

```
$ ./build.sh
$ ./mvnw package -P native
```
 
builds a native-zip ZIP file in target. Upload it to AWS and set the handler to "foobar".

To test locally, run the `ContainerTests`. Also the `build.sh` script orchestrates the same test for the native image.
