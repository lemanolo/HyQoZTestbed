#input=""
#while [[ $input != "exit" ]]
#do
#   read input
#   echo "your input: "${input}
#done


while read line
do
    name=$line
    sleep 1s
    echo "[BASH] "$line
done < $1
