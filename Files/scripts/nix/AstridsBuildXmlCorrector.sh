#!/bin/bash
DEBUG=1

builds="$(ls -1 ./*/build.xml |grep -v Build/build.xml)"
[ $DEBUG -gt 0 ] && echo "DEBUG: build-files: $builds"

for buildFile in $builds
do
	grep -q 'import file="build-user.xml"' $buildFile || continue
	[ $DEBUG -gt 0 ] && echo "Checking $buildFile"
	lineNr=$( awk '/import file="build-user.xml"/ {print NR}' $buildFile )
	[ $DEBUG -gt 1 ] && echo "DEBUG: found import in line: $lineNr"
	firstTarget=$(awk '/target name=/ {print NR}' $buildFile|head -n1)
	[ $DEBUG -gt 1 ] && echo "DEBUG: found first target in line: $firstTarget"
	[ ! $(( $firstTarget - $lineNr )) -gt 1 ] && continue || echo "Problemchild: $buildFile"
	i=1
	cat $buildFile | while read line
	do
		if [[ $line =~ "import file=\"build-user.xml" ]]
		then
			echo "Skipping line $line"
		elif [ $i -eq $firstTarget ]
		then
			echo "<import file=\"build-user.xml\"\/>" >>tmp.xml
			echo "$line" >>tmp.xml
		else
			echo "$line" >>tmp.xml
		fi
		let i++
	done
	mv tmp.xml $buildFile
done

