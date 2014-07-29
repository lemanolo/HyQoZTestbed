#!/usr/bin/env bash
source config.sh 

while read -r SIGNATURE; do
       DERIVATION_OUTPUT_FILE="$DERIVATION_OUTPUT_DIR/$DERIVATION_OUTPUT_FILE_PREFIX$SIGNATURE.txt";    rm $DERIVATION_OUTPUT_FILE; java $JVM_PARAMS -jar $JAR -derive -hqsignature "$SIGNATURE"              -outputfile "$DERIVATION_OUTPUT_FILE"
       GENERATION_OUTPUT_FILE="$GENERATION_OUTPUT_DIR/$GENERATION_CF_OUTPUT_FILE_PREFIX$SIGNATURE.txt"; rm $GENERATION_OUTPUT_FILE; java $JVM_PARAMS -jar $JAR -generate -inputfile "$DERIVATION_OUTPUT_FILE" -outputfile "$GENERATION_OUTPUT_FILE" -controlflow
       #GENERATION_OUTPUT_FILE="$GENERATION_OUTPUT_DIR/$GENERATION_DF_OUTPUT_FILE_PREFIX$SIGNATURE.txt"; rm $GENERATION_OUTPUT_FILE; java $JVM_PARAMS -jar $JAR -generate -inputfile "$DERIVATION_OUTPUT_FILE" -outputfile "$GENERATION_OUTPUT_FILE" -dataflow
echo ""
echo "_____________________________________________________________________________________________________________________________________________"
echo ""
done <<< "$HQSIGNATURES"


