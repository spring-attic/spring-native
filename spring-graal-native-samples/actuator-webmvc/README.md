# actuator-webmvc

## Using VSCode and Remote Containers

If you open this folder in VSCode and you have the Remote Container extension installed, you can open it up in a container and get the GraalVM build tools in whatever flavour you need.

* Build the dev containers locally (`docker/build-dev-images.sh`)
* Open this folder in VSCode
* `CTRL-SHIFT-p` and `Remote Containers: Open Folder in Container...`

You get a shell (and can launch additional terminals) and you can edit the code in the container and the changes get mirrored on the host.