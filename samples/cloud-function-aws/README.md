### AWS Lambda custom runtime example

This example demonstrates using Spring Cloud Function with AWS Custom Runtime. Custom Runtime provided by Spring Cloud Function. 

### Build project (non-MAC users)
Builds a native-zip ZIP file in target by executing provided `build.sh` script

```
$ ./build.sh
```

### Build project (MAC users)
If you are running on Mac, you should build in docker image to ensure AWS compatible binary. To do that, navigate to the root 
of the native project and execute `run-dev-container.sh` script:

```
$> cd ../../
$> ./run-dev-container.sh
```

That will download and start the docker container where you can now build the project:

```
$> cd samples/cloud-function-aws
$> ./build.sh

```

### Deploy and test

- Navigate to AWS Lambda dashboard and create a new function (name it anyway you want).
- In "Runtime" go to `Custom Runtime` and select one of the options available.
- Upload generated `cloud-function-aws-0.0.1-SNAPSHOT-native-zip.zip`.
- Finally test by providing JSON-style string value via 'Test' tab. For example `"John"`. The output should be `"Hi John!"`