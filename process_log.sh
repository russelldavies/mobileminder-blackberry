#!/bin/sh

if [ -z $1 ]; then
	echo "Usage: $0 [filename]"
	exit 0
fi

# Strip out thread number and mobileminder preface text
grep '\*\*\*MobileMinder\*\*\*' $1 | sed 's/\[[0-9]*.[0-9]*\] //g' | cut -d' ' -f2- > tmpfile && mv tmpfile $1
