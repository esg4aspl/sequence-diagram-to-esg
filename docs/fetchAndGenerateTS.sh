REPO_URL=$1
dir_name=temp_$(uuidgen)
git clone "$REPO_URL" "$dir_name"
cd "$dir_name" || exit
find . -name '*.puml' -print0 |
  xargs -0 grep --null -L -e '\[\*\]' -e class -e '^(' |
  xargs -0 -I{} java -jar ../our.jar {} -t2 -t3 -t4 -x10 -pPSEUDO --test-sequence-output={}-tsf.txt
fileCount=$(find . -name '*tsf\.txt*' | wc -l)
printf "%s \nfileCount:%s\ntupleLength seqCount evCount\n" "$REPO_URL" "$fileCount" > ./sd2esgresults.txt
find . -name '*tsf\.txt' -print0 |
  xargs -0 grep -h 'TupleLength' | tr : ' ' |
  awk '{ seqCounts[$2] += $4; evCounts[$2] += $6 } END { for (key in seqCounts) print key, seqCounts[key], evCounts[key]  }' | sort >> ./sd2esgresults.txt
