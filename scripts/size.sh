EXECUTABLE=$1
SIZE=`wc -c <"${EXECUTABLE}"`/1024
SIZE=`bc <<< "scale=1; ${SIZE}/1024"`
echo "Image size: ${SIZE}M"
