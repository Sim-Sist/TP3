#!/bin/bash
mainClass=Main

scriptDir=`dirname $0`
classDir=${scriptDir}/../bin;
sourceDir=${scriptDir}/main/java

# if [ ! -d ${scriptDir}/utils ]; then
#     mkdir -p ${scriptDir}/utils
# fi

sourcesFileDir=${scriptDir}/utils/sources.txt

find ${sourceDir} -name '*.java' > ${sourcesFileDir}

javac -d ${classDir} @${sourcesFileDir};
if [ "$1" != "compile" ]; then
    java -Xmx6G -cp ${classDir} ${mainClass} ${@:1};
fi