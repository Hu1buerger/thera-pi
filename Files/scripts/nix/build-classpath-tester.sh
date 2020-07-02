#!/bin/bash
##########
#
# Small helper script to check if libs mentioned in .classpath are in build.xml section
#
# Usage: build-classpath-tester.sh DIR
# Parameters: the Project-dir in question to check
# Example: Files/scripts/nix/build-classpath-tester.sh OpRgaf
#
# PRE:
#	- there's a project.properties file containing a line "projektname" that is equal to the relevant classpath-entry in build.xml
# POST:
#	- Will give false positives on the test-libs, since they're in BUILD/test.xml (or some place like that...)

DEBUG=0

_dir="$1"
_project="$(cat ${_dir}/project.properties |grep projektname|sed 's/.*=\(.*\)/\1/g' )"
[ $DEBUG -gt 0 ] && echo "ProjectName: $_project"
_cpf="${_dir}/.classpath"
_bxf="${_dir}/build.xml"

function getCPStart() {
	awk '/ id="'${_project}.classpath'"/ {print NR}' $_bxf
}

libs="$(cat $_cpf |grep 'classpathentry kind="lib"'|sed 's/.*path="\/[^/]*\/\([^"]*\)".*/\1/g' )"
[ $DEBUG -gt 0 ] && echo "DEBUG: libs=\"$libs\""
buildf="$(cat ${_bxf})"
cpStart=$(getCPStart)
let cpStart++
[ $DEBUG -gt 0 ] && echo "DEBUG: Starting at line: $cpStart"
for i in $( awk '/<\/path>/ {print NR}' $_bxf )
do
	[ $i -gt $cpStart ] && break
done
[ $DEBUG -gt 0 ] && echo "DEBUG: Until $i"
cpEnd=$i
let cpEnd--
bxSection="$(sed -n -e "$cpStart,$cpEnd p" -e "$cpEnd q" $_bxf )"
for lib in $libs
do
	echo "$bxSection" |grep -q $lib && echo "Found $lib" || echo "$lib is not in build.xml"
done
