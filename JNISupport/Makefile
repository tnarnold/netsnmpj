
include local.mk

include include.mk


OBJS =	pdu_jobject.o \
	jni_proxy.o \
	joid_proxy.o \
	sysprintf.o \
	jthrow.o 


OBJECTS=$(addprefix $(OBJ_DIR)/, $(OBJS))

SOURCES=$(OBJS:.o=.cc)

ifeq ($(CCC),g++)
	CFLAGS+= -fno-gnu-linker
endif


lib: $(OBJ_DIR)/libjnisupport.a 

$(OBJ_DIR)/libjnisupport.a: Makefile $(OBJECTS)
	ar crsuv $(OBJ_DIR)/libjnisupport.a $(OBJECTS)

##
## TBD moves these targets into a make include
##
%.class: %.java
	javac -classpath $(CLASSPATH) $<

$(OBJ_BASE)-dbg/%.o: %.cc
	$(CCC) -c $(DEFINES) $(INCLUDES) $(DEBUG_FLAG) $(CFLAGS) -o $@ $< 

$(OBJ_BASE)-opt/%.o: %.cc
	$(CCC) -c $(DEFINES) $(INCLUDES) $(OPT_FLAGS) $(CFLAGS) -o $@ $< 

dirs: 
	- mkdir $(OBJ_BASE)-opt $(OBJ_BASE)-dbg

clean:
	rm */*.o */*.a 

##
## TBD come up with a better method for dependency generatons
##
depends: $(HOST)-depends.mk $(HOST)-depends.mk

$(HOST)-depends.mk:
	$(CCC) $(DEPENDS_FLAG) $(DEFINES) $(SOURCES) $(INCLUDES) >$(HOST)-depends.mk
	perl -i -p \
	-e '($$str, $$deps) = /(^[\w\-_]+.o):\s*(.*)/ ;' \
	-e 'next unless $$str ;' \
	-e '$$str2 = join " ", map "$$_/$$str", qw/$(OBJ_DIR_LIST)/ ;' \
	-e '$$_ = "$$str2: $$deps\n" ;' \
	 $(HOST)-depends.mk


include $(HOST)-depends.mk
