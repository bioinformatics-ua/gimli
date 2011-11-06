#!/bin/bash
cp=target/gimli-1.1-SNAPSHOT-jar-with-dependencies.jar:$CLASSPATH
MEMORY=1G
JAVA_COMMAND="java -Xmx$MEMORY -classpath $cp"

CLASS=pt.ua.tm.gimli.util.Splitter

$JAVA_COMMAND $CLASS $*
