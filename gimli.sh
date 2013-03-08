#!/bin/bash
cp=target/gimli-1.0.2-jar-with-dependencies.jar:$CLASSPATH
MEMORY=5G
JAVA_COMMAND="java -Xmx$MEMORY -classpath $cp"
CMD=$1
shift

help() {
cat << EOF
Gimli commands: 
	convert [BC2|JNLPBA]	convert IeXML file to the CoNNL format
	model			train or test model using the generated CoNNL file
	annotate [BC2|JNLPBA]		annotate IeXML file using the trained model

Include -h or --help with any option for more information
EOF
echo $HELP
}
CLASS=

case $CMD in
	convert)
		CMD2=$1
		shift
		case $CMD2 in
			BC2)
				CLASS=pt.ua.tm.gimli.reader.BCReader;;
			JNLPBA)
				CLASS=pt.ua.tm.gimli.reader.JNLPBAReader;;
			*)
				echo "Unrecognized corpus to perform conversion: $CMD2. Should be BC2 or JNLPBA."; help; exit 1;;
		esac
		;;
	model)
		CLASS=pt.ua.tm.gimli.util.Model;;
	annotate)
		CMD2=$1
		shift
		case $CMD2 in
			BC2)
				CLASS=pt.ua.tm.gimli.writer.BCWriter;;
			JNLPBA)
				CLASS=pt.ua.tm.gimli.writer.JNLPBAWriter;;
			*)
				echo "Unrecognized corpus to annotate: $CMD2. Should be BC2 or JNLPBA."; help; exit 1;;
		esac
		;;
	*)
		echo "Unrecognized command: $CMD"; help; exit 1;;
esac

$JAVA_COMMAND $CLASS $*
