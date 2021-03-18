#!/bin/bash
set -e

case "$1" in
	java11)
		 echo "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.10_9.tar.gz"
	;;
	*)
		echo $"Unknown java version"
		exit 1
esac
