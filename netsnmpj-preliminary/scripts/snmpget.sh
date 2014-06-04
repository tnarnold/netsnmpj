#! /bin/sh
##
## 	       Copyright(c) 2003 by Andrew E. Page
##
## 		      All Rights Reserved
##
## Permission to use, copy, modify, and distribute this software and its
## documentation for any purpose and without fee is hereby granted,
## provided that the above copyright notice appears in all copies and that
## both that copyright notice and this permission notice appear in
## supporting documentation, and that the name Andrew E. Page not be used
## in advertising or publicity pertaining to distribution of the software
## without specific, written prior permission.
##
## ANDREW E. PAGE DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
## INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
## EVENT SHALL ANDREW E. PAGE BE LIABLE FOR ANY SPECIAL, INDIRECT OR
## CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF
## USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
## OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
## PERFORMANCE OF THIS SOFTWARE.
##

##
## launching shell script for swingui version of snmpget
##

jhome=${JAVAHOME:-$JAVA_HOME}
jhome=${jhome:-"NOTFOUND"}

if [  $jhome = "NOTFOUND" ] ; then
    testJ="java"
else
    testJ=$jhome"/bin/java"
fi

testJ=`which $testJ 2>&1`

if [ ! -x "$testJ" ] ; then
    echo "no executable java."
    echo "Please set JAVAHOME or JAVA_HOME to the location of your jdk or jre"
    exit 2 
fi

dir=`dirname $0`

if [ -z $dir ] ; then
    set dir="."
fi

if [ ! -f "$dir/netsnmpj.jar" ] ; then
    echo "# Fatal Error:  netsnmpj.jar file not found.  Please copy to: $dir"
    exit 2 ;
fi

sys=`uname -s`

if [ $sys = "SunOS" ] ; then
  nativeDir=$dir"/solaris-sparc"
elif [ $sys = "Linux" ] ; then
  nativeDir=$dir"/linux-i386"
else
    echo "unsupported platform:"
    uname -a
    exit 2
fi

$testJ -Djava.library.path=$nativeDir -cp $dir/netsnmpj.jar org.netsnmp.swingui.snmpget

