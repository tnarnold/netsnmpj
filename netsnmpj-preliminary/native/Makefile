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
include local.mk

.SUFFIXES: .java .class

ifeq ($(JNI_SUPPORT_DIR),)
errdefault:
	@echo "###"
	@echo "### JNI_SUPPORT_DIR not set"
	@echo "###"
	@echo "### Set this in local.mk to the location where the"
	@echo "### the JNISupport module is checked out"
	@echo "###"
	@exit 2  ;
endif

include $(JNI_SUPPORT_DIR)/include.mk

HOST := $(shell hostname)
JINCLUDE := $(shell $(JAVA) propertyquery java.home)/../include

CLASSPATH=../jsrc

TOOL_LIST=dbg opt

SESSION_OBJS =  NetSNMP.o \
		nativeThread.o \
		exception.o \
		lock.o \
		NetSNMPjClasses.o \
		NetSNMPjSession_read.o \
		NetSNMPjSession_send.o \
		NetSNMPPDU.o \
		NetSNMPjSession.o  \
		session_to_jobject.o \
		agentX.o \
		NetSNMPOID.o \
		NetSNMPMIB.o


##
## Note with gcc 3.2 and linux it seems that link order
## does make a difference with shared libs.  If this
## order is not observed we get core dumps on the static
## intializers.
##
MIB_OBJS = 

ifeq ($(CCC),g++)
	LINK_OPT_START=-Xlinker -O -Xlinker 1 --startgroup
	LINK_OPT_END=--endgroup
endif

NATIVE_CLASSES=org.netsnmp.MIB org.netsnmp.NativeOID org.netsnmp.NetSNMPSession org.netsnmp.PDU org.netsnmp.NetSNMP

LIBS=$(SNMPLIBS) -L$(JNI_SUPPORT_DIR)/$(OBJ_DIR) -ljnisupport -lpthread

SESSION_OBJECTS=$(addprefix $(OBJ_DIR)/, $(SESSION_OBJS))

MIB_OBJECTS=$(addprefix $(OBJ_DIR)/, $(MIB_OBJS))

SOURCES=$(MIB_OBJS:.o=.cc) $(SESSION_OBJS:.o=.cc)

libs:  $(OBJ_DIR)/libnetsnmpj.so

$(OBJ_DIR)/$(LIBNAME): $(SESSION_OBJECTS) Makefile $(JNI_SUPPORT_DIR)/include.mk support 
	$(CCC) $(DEBUG_FLAG) $(SHARED_FLAG) -o $@ $(LINK_OPT_START)  $(SESSION_OBJECTS) \
	 $(LIBS) $(LINK_OPT_END)


myreq: $(OBJECTS)
	$(CC) $(DEBUG_FLAG) -o myreq $(OBJECTS) $(LIBS)

org_netsnmp_NetSNMPSession.h:: $(CLASSPATH)/org/netsnmp/NetSNMPSession.class
	$(JAVAH)  -classpath $(CLASSPATH) org.netsnmp.NetSNMPSession

org_netsnmp_NetSNMP.h:: $(CLASSPATH)/org/netsnmp/NetSNMP.class $(CLASSPATH)/org/netsnmp/agentx/AgentX.class
	$(JAVAH)  -classpath $(CLASSPATH) org.netsnmp.NetSNMP

org_netsnmp_NativeOID.h:: $(CLASSPATH)/org/netsnmp/NativeOID.class
	$(JAVAH)  -classpath $(CLASSPATH) org.netsnmp.NativeOID

org_netsnmp_MIB.h:: $(CLASSPATH)/org/netsnmp/MIB.class
	$(JAVAH) -classpath $(CLASSPATH) org.netsnmp.MIB

org_netsnmp_PDU.h:: $(CLASSPATH)/org/netsnmp/PDU.class
	$(JAVAH)  -classpath $(CLASSPATH) org.netsnmp.PDU

headers:: org_netsnmp_NetSNMPSession.h org_netsnmp_NativeOID.h org_netsnmp_MIB.h org_netsnmp_NetSNMP.h org_netsnmp_PDU.h 

depends: headers $(HOST)-depends.mk

make_dirs:
	 for D in $(TOOL_LIST) ; do \
		if [ ! -d $(OBJ_BASE)-$$D ] ; then mkdir $(OBJ_BASE)-$$D  ; fi  ; \
		done

clean:
	rm -f */*.o */*.so org_netsnmp_NetSNMPSession.h org_netsnmp_NativeOID.h org_netsnmp_MIB.h $(HOST)-depends.mk
	cd $(JNI_SUPPORT_DIR) ; $(MAKE) clean 
##
## TBD come up with a better method for dependency generatons
##
$(HOST)-depends.mk:
	$(JAVAH) -classpath $(CLASSPATH) $(NATIVE_CLASSES)
	$(CCC) $(DEPENDS_FLAG) $(DEFINES) $(SOURCES) $(INCLUDES) >$(HOST)-depends.mk
	perl -i -p \
	-e '($$str, $$deps) = /(^[\w\-_]+.o):\s*(.*)/ ;' \
	-e 'next unless $$str ;' \
	-e '$$str2 = join " ", map "$$_/$$str", qw/$(OBJ_DIR_LIST)/ ;' \
	-e '$$_ = "$$str2: $$deps\n" ;' \
	 $(HOST)-depends.mk


support:
	cd $(JNI_SUPPORT_DIR) ; $(MAKE) lib TOOL=$(TOOL)

%.class: %.java
	javac -classpath $(CLASSPATH) $<

$(OBJ_BASE)-dbg/%.o: %.cc
	$(CCC) -c $(DEFINES) $(INCLUDES) $(DEBUG_FLAG) $(CFLAGS) -o $@ $< 

$(OBJ_BASE)-opt/%.o: %.cc
	$(CCC) -c $(DEFINES) $(INCLUDES) $(OPT_FLAGS) $(CFLAGS) -o $@ $< 

dirs: 
	- mkdir $(OBJ_BASE)-opt $(OBJ_BASE)-dbg



include $(HOST)-depends.mk

##
## $Log: Makefile,v $
## Revision 1.17  2003/06/07 15:57:22  aepage
## AgentX fixes
##
## Revision 1.16  2003/06/01 16:03:49  aepage
## merget from release-0.2.1
##
## Revision 1.15.2.2  2003/05/21 00:47:25  aepage
## fixes to absorb MIB functions into the main library in order to
## support RH7.3 and perhaps other flavors as well.
##
## Revision 1.15.2.1  2003/05/16 17:56:10  aepage
## Changes for makefile fixes
##
## Revision 1.15  2003/05/03 23:37:30  aepage
## changes to support agentX subagents
##
## Revision 1.14  2003/04/30 21:22:27  aepage
## header generation
##
## Revision 1.13  2003/04/18 01:45:01  aepage
## new session_to_jsession object file
##
## Revision 1.12  2003/03/31 17:43:58  aepage
## fixes for shared lib issues
##
## Revision 1.11  2003/03/29 17:22:48  aepage
## SUNpro cc fixes and various corrections
##
## Revision 1.10  2003/03/29 00:13:38  aepage
## new thread architecture
##
## Revision 1.9  2003/03/24 13:31:55  aepage
## migration issue for JNISupport Module
##
## Revision 1.8  2003/03/22 00:22:17  aepage
## reorg for JNISupport
##
## Revision 1.7  2003/03/18 20:25:53  aepage
## removal of TEMPLATE_FLAG, added SHARED_FLAG movement of various vars into include.mk
##
## Revision 1.6  2003/03/14 16:13:29  aepage
## movement of settings to the ../include.mk file so that others may make
## use of them.  New object files added.
##
## Revision 1.5  2003/02/27 17:31:12  aepage
## added pthread lib for 1.2.2 solaris jvm
##
## Revision 1.4  2003/02/12 13:51:14  aepage
## Fix that removes the -g flag from an opt/release build
##
## Revision 1.3  2003/02/10 21:20:27  aepage
## *** empty log message ***
##
## Revision 1.2  2003/02/10 00:13:38  aepage
## variables migrated to include.mk and parameterization of some tools
##
## Revision 1.1.1.1  2003/02/07 23:56:53  aepage
## Migration Import
##
## Revision 1.5  2003/02/07 22:06:29  aepage
## refactoring migration
##
## Revision 1.4  2003/02/06 18:07:30  aepage
## Fixes to various dependencies and moved OID support into the MIB library.
##
## Revision 1.3  2003/02/05 15:46:06  aepage
## fixes for intial building
##
## Revision 1.2  2003/02/05 15:16:40  aepage
## Makefile fixes for integrated and multi-platform make preparations
##
## Revision 1.1  2003/02/04 23:29:46  aepage
## Initial Checkins
##
##