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
## copy the local.mk.template file to local.mk and adjust
## the values according to your own local system as necessary
##
##  local.mk should always preceed include.mk
##
include local.mk

ifeq ($(JNI_SUPPORT_DIR),)

local.mk: errdefault

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


SYSTEM := $(shell $(JAVA) -cp native propertyquery os.name)
##
## For some reason the os.name resource lists the os as sunos and the
## system specific C headers are in the 'solaris' sub directory in the
## jdk home.
##
ifeq ($(SYSTEM),sunos)
	SYSTEM=solaris
endif

ARCH := $(shell $(JAVA) -cp native propertyquery os.arch)

ARCH_NAME=$(SYSTEM)-$(ARCH)-opt

INSTALLED_ARCHITECTURES=$(ARCH_NAME)

RELEASE_FILES=jsrc/netsnmpj.jar \
	scripts/snmpget.bat \
	scripts/snmpget.sh  \
	docs/COPYING \
	docs/INSTALL \
	docs/RELEASE \
	README

ifeq ($(RELEASE_NAME),)
	RELEASE_NAME=netsnmpj-$(shell $(JAVA) -cp native propertyquery os.name)-$(shell $(JAVA) -cp native propertyquery os.arch)-$(RELEASE)
endif


OBJ_BASE := $(SYSTEM)-$(shell $(JAVA) -cp native propertyquery os.arch)

CLASSES=

ifeq ($(TEST_JVMS),)
	TEST_JVMS=java
endif


##
## RELEASE_TREE_NAME and RELEASE_FILE_NAME
## are specified here so that the builder
## can specify these from eithe the command line
## or the local.mk parameter file without having
## to make changes to the Makefile
##
##
ifeq ($(RELEASE_TREE_NAME),)
	RELEASE_TREE_NAME=$(RELEASE_NAME)
endif

ifeq ($(RELEASE_FILE_NAME),)
	RELEASE_FILE_NAME=$(RELEASE_TREE_NAME).tar.gz
endif


all: test

jar: 
	cd jsrc ; $(MAKE) lib

nativeLib:
	cd native ; $(MAKE) dirs libs TOOL=opt

test: jar nativeLib netsnmpjTest.properties
ifeq ($(JUNIT_CLASSPATH),)
	@echo "##"
	@echo "## JUNIT_CLASSPATH in local.mk must be set in order to perform the unit tests"
	@echo "## for now we bring you the swing version of snmpget."
	@echo "## Junit can be found at: http://junit.sourceforge.net"
	@echo "##"
	$(JAVA) -Djava.library.path=native/$(OBJ_BASE)-opt -cp jsrc/$(JLIBNAME) \
	org.netsnmp.swingui.snmpget
else
	for testjvm in $(TEST_JVMS) ; do \
		echo "##" ; echo "## Testing against " $$testjvm ; echo "##" ; $$testjvm -version ; \
		$$testjvm -Djava.library.path=native/$(OBJ_BASE)-opt -cp jsrc/$(JLIBNAME):$(JUNIT_CLASSPATH) \
	org.netsnmp.junittests.AllSuites ; \
	done
endif

snmpget: jar nativeLib 
	$(JAVA) -Xcheck:jni -Djava.library.path=native/$(OBJ_BASE)-opt -cp jsrc/$(JLIBNAME):$(JUNIT_CLASSPATH) \
	org.netsnmp.swingui.snmpget $(HOST) $(COMMUNITY)

clean:
	cd native ; $(MAKE) clean 
	cd jsrc ; $(MAKE) clean
	cd $(JNI_SUPPORT_DIR) ; $(MAKE) clean

release:
	cd jsrc ; $(MAKE)  lib ; 
	cd native ; $(MAKE) TOOL=opt make_dirs ; $(MAKE) TOOL=opt
	- mkdir -p tmp/$(RELEASE_TREE_NAME)/$(SYSTEM)-$(ARCH)
	- mkdir -p tmp/$(RELEASE_TREE_NAME)/docs
	- mkdir -p tmp/$(RELEASE_TREE_NAME)/examples
	cp jsrc/org/netsnmp/examples/*.java tmp/$(RELEASE_TREE_NAME)/examples
	cp $(RELEASE_FILES) tmp/$(RELEASE_TREE_NAME)
	cd jsrc ; $(MAKE) docs DOC_DIR=../tmp/$(RELEASE_TREE_NAME)/docs
	cp native/$(ARCH_NAME)/$(LIBNAME) tmp/$(RELEASE_TREE_NAME)/$(SYSTEM)-$(ARCH)
	cd tmp ; tar cvzf ../$(RELEASE_FILE_NAME) $(RELEASE_TREE_NAME)
	rm -r tmp

bindist:
	cd native ; $(MAKE) TOOL=opt
	- mkdir -p tmp/$(RELEASE_TREE_NAME)/$(SYSTEM)-$(ARCH)
	cp native/$(ARCH_NAME)/$(LIBNAME) tmp/$(RELEASE_TREE_NAME)/$(SYSTEM)-$(ARCH)
	cd tmp ; tar cvf ../$(RELEASE_TREE_NAME)-$(SYSTEM)-$(ARCH).tar $(RELEASE_TREE_NAME) ; cd .. ; gzip -f --best $(RELEASE_TREE_NAME)-$(SYSTEM)-$(ARCH).tar
	rm -r tmp


local.mk:
	cp local.mk.template local.mk
	@echo "##"
	@echo "## A copy of local.mk.template has been copied for you to local.mk."
	@echo "## Adjust the values within this file to suit your local system;"
	@echo "## you may then re-run make"
	@echo "##"
	exit 2

netsnmpjTest.properties:
	cp netsnmpjTest.properties.template netsnmpjTest.properties
	@echo "##"
	@echo "## A copy of netsnmpjTest.properties has been copied for you ."
	@echo "## Adjust the values within this file to suit your local system;"
	@echo "## you may then re-run make"
	@echo "##"
	exit 2


##
## Makefile,v
## Revision 1.12.2.1  2003/05/21 00:47:26  aepage
## fixes to absorb MIB functions into the main library in order to
## support RH7.3 and perhaps other flavors as well.
##
## Revision 1.12  2003/05/08 16:33:35  aepage
## pre branch checkins
##
## Revision 1.11  2003/04/19 01:47:32  aepage
## removed -Xcheck:jni since many of our test JVM's can't take it.
## Option has been moved into the local specication for TEST_JVMS
##
## Revision 1.10  2003/03/31 17:43:59  aepage
## fixes for shared lib issues
##
## Revision 1.9  2003/03/29 23:23:54  aepage
## add JNISupport to clean target
##
## Revision 1.8  2003/03/29 00:58:55  aepage
## JNISupport integration
##
## Revision 1.7  2003/03/02 16:59:18  aepage
## pre release checkin
##
## Revision 1.6  2003/02/27 18:35:15  aepage
## added version to testjvm output
##
## Revision 1.5  2003/02/27 18:01:16  aepage
## Added TEST_JVMs list to test under multiple JVMS
##
## Revision 1.4  2003/02/10 21:32:00  aepage
## update of message to include where to find junit
##
## Revision 1.3  2003/02/10 21:18:52  aepage
## fix for solaris object directory
##
## Revision 1.2  2003/02/10 00:46:47  aepage
## make test target improved to include junit tests
##
## Revision 1.1.1.1  2003/02/07 23:56:48  aepage
## Migration Import
##
## Revision 1.4  2003/02/07 22:06:10  aepage
## release target
##
## Revision 1.3  2003/02/05 15:46:06  aepage
## fixes for intial building
##
## Revision 1.2  2003/02/05 15:16:40  aepage
## Makefile fixes for integrated and multi-platform make preparations
##
##