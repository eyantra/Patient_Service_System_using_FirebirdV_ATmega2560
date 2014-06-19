#!/bin/bash
ARRAY[0]=0
i=0
for JAVADIR in `ls -l /usr/lib/jvm/ | grep "^d" | awk '{ print $9 }'`; do
	URL="/usr/lib/jvm/"$JAVADIR;
	ARCH=`find $URL/. -name *.so | head -n 6 | tail -n 1 | cut -f 9 -d \/`;
	PATH1="/usr/lib/jvm/"$JAVADIR"/jre/lib/"$ARCH"/";
	i=$(($i+1));
	ARRAY[$i]=$PATH1
done
j=1;
i=$(($i+1))
while [[ $j < $i ]]; do
	sudo cp librxtxSerial.so ${ARRAY[$j]};
	j=$(($j+1));
done


