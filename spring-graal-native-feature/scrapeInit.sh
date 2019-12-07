

rpkgs=`cat src/main/resources/initialization.json | grep -v "^ *\/\/" | jq '.runtimeInitialization' | jq '.[] | .package' | grep -v "null" | tr '\n' ',' | sed 's/,$//'`
rclzs=`cat src/main/resources/initialization.json | grep -v "^ *\/\/" | jq '.runtimeInitialization' | jq '.[] | .class' | grep -v "null" | tr '\n' ',' | sed 's/,$//'`	
bpkgs=`cat src/main/resources/initialization.json | grep -v "^ *\/\/" | jq '.buildTimeInitialization' | jq '.[] | .package' | grep -v "null" | tr '\n' ',' | sed 's/,$//'`
bclzs=`cat src/main/resources/initialization.json | grep -v "^ *\/\/" | jq '.buildTimeInitialization' | jq '.[] | .class' | grep -v "null" | tr '\n' ',' | sed 's/,$//'`

echo "--initialize-at-run-time=$rpkgs,$rclzs --initialize-at-build-time=$bpkgs,$bclzs" | sed 's/"//g' | sed 's/\$/\\\$/g' > ../native-image.properties
