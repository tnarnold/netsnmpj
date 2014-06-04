
JDKHOME=e:\java\j2sdk1.4.1_01

NET_SNMP_HOME=C:\gnu\net-snmp-5.0.7\

JUNIT_CLASSPATH=e:\java\junit3.8.1\junit.jar

JLIBNAME=netsnmpj.jar

TOOL=dbg

!if "$(TOOL)"=="dbg"
OBJ_DIR=Win32-dbg
CCC_OPTS=/GX /Od /Zi
LIBS=$(LIBS) /LIBPATH:$(NET_SNMP_HOME)\win32\lib libsnmpstatic_d.lib
DEFINES=$(DEFINES) /D_DEBUG
LINK_DEBUG=/debug


!elseif "$(TOOL)"=="opt"
OBJ_DIR=Win32-opt
CCC_OPTS=/GX /Ox
LIBS=$(LIBS) /LIBPATH:$(NET_SNMP_HOME)\win32\lib libsnmpstatic.lib
LINK_DEBUG=


!else


!error TOOL is not defined

!endif



!IFNDEF JAVA
JAVA=java
!ENDIF

!IFNDEF JAVAC
JAVAC=javac
!ENDIF

