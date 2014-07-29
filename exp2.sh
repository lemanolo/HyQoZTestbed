#!/usr/bin/env bash
source config.sh

while read -r SIGNATURE; do
       GENERATION_OUTPUT_FILE="$GENERATION_OUTPUT_DIR/$GENERATION_CF_OUTPUT_FILE_PREFIX$SIGNATURE.txt"
       if [ ! -f "$GENERATION_OUTPUT_FILE" ]; then
              echo "File $GENERATION_OUTPUT_FILE not found!"
       else
              case `basename $GENERATION_OUTPUT_FILE` in
                 qws_cf_* ) GENERATION_PREFIX="(cf)_";;
                 qws_df_* ) GENERATION_PREFIX="(df)_";;
              esac 
              echo ""
              echo "BUILDTIME"
              echo ""
              WEIGHTING_OUTPUT_FILE="$WEIGHTING_OUTPUT_DIR/$WEIGHTING_BT_OUTPUT_FILE_PREFIX$GENERATION_PREFIX$SIGNATURE.txt"; rm $WEIGHTING_OUTPUT_FILE 2>>/dev/null;
              java $JVM_PARAMS -jar $JAR -weight -inputfile "$GENERATION_OUTPUT_FILE" -outputfile "$WEIGHTING_OUTPUT_FILE" -buildtime

#              echo ""
#              echo "PARTIAL_CF"
#              echo ""
#              WEIGHTING_OUTPUT_FILE="$WEIGHTING_OUTPUT_DIR/$WEIGHTING_RT_PARTIALCF_OUTPUT_FILE_PREFIX$GENERATION_PREFIX$SIGNATURE.txt"; rm $WEIGHTING_OUTPUT_FILE 2>>/dev/null;
#              java $JVM_PARAMS -jar $JAR -weight -inputfile "$GENERATION_OUTPUT_FILE" -outputfile "$WEIGHTING_OUTPUT_FILE" -runtime -estimation_approach partialcf
#
#              echo ""
#              echo "FULL_CF"
#              echo ""
#              WEIGHTING_OUTPUT_FILE="$WEIGHTING_OUTPUT_DIR/$WEIGHTING_RT_FULLCF_OUTPUT_FILE_PREFIX$GENERATION_PREFIX$SIGNATURE.txt"; rm $WEIGHTING_OUTPUT_FILE 2>>/dev/null;
#              java $JVM_PARAMS -jar $JAR -weight -inputfile "$GENERATION_OUTPUT_FILE" -outputfile "$WEIGHTING_OUTPUT_FILE" -runtime -estimation_approach fullcf

              echo ""
              echo "DF"
              echo ""
              WEIGHTING_OUTPUT_FILE="$WEIGHTING_OUTPUT_DIR/$WEIGHTING_RT_DF_OUTPUT_FILE_PREFIX$GENERATION_PREFIX$SIGNATURE.txt"; rm $WEIGHTING_OUTPUT_FILE 2>>/dev/null;
              java $JVM_PARAMS -jar $JAR -weight -inputfile "$GENERATION_OUTPUT_FILE" -outputfile "$WEIGHTING_OUTPUT_FILE" -runtime -estimation_approach df
       fi
       echo ""
       echo "_____________________________________________________________________________________________________________________________________________"
       echo ""
done <<< "$HQSIGNATURES"


