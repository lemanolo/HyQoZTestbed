DS=$1
FILE=${DS}.resume
cat ${DS}_QWs.txt | grep "create_" | sed -e "s/ *$/\|ok/" > ${FILE}
cat ${DS}_QWs.txt.cyclic | grep "create_" | sed -e "s/ *$/\|cyclic/" >> ${FILE}
sort ${FILE} | awk -F "|" '{print $2"|"$1}' > $$.tmp
mv $$.tmp ${FILE}
cat ${FILE}
echo "[${FILE}]"
