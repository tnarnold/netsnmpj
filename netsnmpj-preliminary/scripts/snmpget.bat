@ECHO OFF
REM
REM 	       Copyright(c) 2003 by Andrew E. Page
REM
REM 		      All Rights Reserved
REM
REM Permission to use, copy, modify, and distribute this software and its
REM documentation for any purpose and without fee is hereby granted,
REM provided that the above copyright notice appears in all copies and that
REM both that copyright notice and this permission notice appear in
REM supporting documentation, and that the name Andrew E. Page not be used
REM in advertising or publicity pertaining to distribution of the software
REM without specific, written prior permission.
REM
REM ANDREW E. PAGE DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
REM INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
REM EVENT SHALL ANDREW E. PAGE BE LIABLE FOR ANY SPECIAL, INDIRECT OR
REM CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF
REM USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
REM OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
REM PERFORMANCE OF THIS SOFTWARE.
REM

REM
REM launching script for swingui version of snmpget
REM


SETLOCAL & PUSHD & set RET=

IF defined JDKHOME (
	set JAVACMD=%JDKHOME%\bin\java
) ELSE IF defined JDK_HOME (
	set JAVACMD=%JDK_HOME%\bin\java
) ELSE IF defined JAVA_HOME ( 
	set JAVACMD=%JAVA_HOME%\bin\java
) ELSE IF defined JAVAHOME else (
	set JAVACMD=%JAVAHOME%\bin\java
) else (
	SET JAVACMD=java
)

set SCRIPTDIR=%~p0
set SCRIPTDRV=%~d0

set MIBDIRS=%SCRIPTDRV%%SCRIPTDIR%\mibs
%JAVACMD% -Djava.library.path=%SCRIPTDRV%%SCRIPTDIR%\Win32-%PROCESSOR_ARCHITECTURE% -cp %SCRIPTDRV%%SCRIPTDIR%\netsnmpj.jar org.netsnmp.swingui.snmpget


popd & endlocal & set RET=%RET%
