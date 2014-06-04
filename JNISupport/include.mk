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

RELEASE=$Name:  $
##
## If the release name is not set(as a result of an cvs export)
## set the release 'devel'
##
ifeq ($(RELEASE),)
	RELEASE=devel
endif
ifeq ($(RELEASE),$Name:  $)
	RELEASE=devel
endif

##
## Default C compiler
##
ifeq ($(CCC),)
	CCC=g++
endif

HOST := $(shell hostname)


JAVAHOME=

JDK_HOME=

ifeq ($(JAVAC),)
	JAVAC=javac
endif

##
## necessary to work with older jvms
##
JAVAC_OPT=-target 1.2 -O
JAVA=java
JAVAH=javah
JAVAOPTS=-sourcepath .

##
## Name of the netsnmp native library.
## (Will need to be changed for windows)
##
LIBNAME=libnetsnmpj.so
MIBLIBNAME=libnetsnmpjmib.so


JLIBNAME=netsnmpj.jar

SYSTEM := $(shell $(JAVA) -cp .:../native:../../native:native propertyquery os.name)
JINCLUDE := $(shell $(JAVA) -cp .:../native:../../native:native propertyquery java.home)/../include

ifeq ($(CCC),g++)
	CFLAGS+=-Wall -fpic
else
	CFLAGS+=-KPIC -mt
endif

SHARED_FLAG=-shared
DEPENDS_FLAG=-M


ifeq ($(SYSTEM),sunos)
	SYSTEM=solaris
ifneq ($(CCC),CC)
	DEFINES+=-Dsolaris2
else
	SHARED_FLAG=-G
	DEPENDS_FLAG=-xM
endif
endif

ifeq ($(TOOL),)
	TOOL=dbg
endif

ifeq ($(TOOL),dbg)
ifneq ($(CCC),CC)
	DEBUG_FLAG=-g
else
	DEBUG_FLAG=-g0
endif
endif

ifeq ($(TOOL),opt)
	DEBUG_FLAG=
	OPT_FLAGS=-O3 -DNDEBUG
endif

ARCH := $(shell $(JAVA) -cp .:../native:../../native:native propertyquery os.arch)
OBJ_BASE := $(SYSTEM)-$(ARCH)

OBJ_DIR=$(OBJ_BASE)-$(TOOL)

INCLUDES := -I$(JNI_SUPPORT_DIR) -I$(JINCLUDE) -I$(JINCLUDE)/$(SYSTEM) $(NETSNMP_INCLUDE)

##
## used for building dependencies.  List of all possible directories
## where objects may be output
##
OBJ_DIR_LIST = $(OBJ_BASE)-opt \
	$(OBJ_BASE)-dbg

##
## supported platforms
##
PLATFORMS=linux-i386 solaris-sparc win32-i386

##
## for a debug build we really don't want the optimization flags 
## turned on
##
SNMPFLAGS := $(shell net-snmp-config --cflags)
SNMPFLAGS := $(subst -O3,,$(SNMPFLAGS))
SNMPFLAGS := $(subst -O2,,$(SNMPFLAGS))
ifeq ($(TOOL),opt)
	SNMPFLAGS := $(subst -g,,$(SNMPFLAGS))
endif

#STATICLINK_ON =-B static
#STATICLINK_OFF=-B dynamic

SNMPLIBS  := $(STATICLINK_ON) $(shell net-snmp-config --netsnmp-libs) $(shell net-snmp-config --external-libs) $(shell net-snmp-config --agent-libs) $(STATICLINK_OFF)

ifneq ($(STATICLINK_ON),)
	SNMPLIBS := $(subst -lkstat, $(STATICLINK_OFF) -lkstat $(STATICLINK_ON), $(SNMPLIBS))
endif

ifeq ($(SYSTEM),linux)
	SNMPLIBS+=-lwrap
endif

DEFINES+=-DUSE_LIBWRAP


##
## TBD post someting to the net-snmp group about the config module
## reporting -lelf module which linux does not seem to have
##
ifeq ($(SYSTEM),linux)
	SNMPLIBS := $(subst -lelf,,$(SNMPLIBS))
	SNMPLIBS := $(subst -lbz2,,$(SNMPLIBS))
endif

##
## For some reason the os.name resource lists the os as sunos and the
## system specific C headers are in the 'solaris' sub directory in the
## jdk home.
##
ifeq ($(SYSTEM),sunos)
	SYSTEM=solaris
endif

