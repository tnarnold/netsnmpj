/*

	       Copyright ;

		      All Rights Reserved

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted,
provided that the above copyright notice appears in all copies and that
both that copyright notice and this permission notice appear in
supporting documentation, and that the name Andrew E. Page not be used
in advertising or publicity pertaining to distribution of the software
without specific, written prior permission.

ANDREW E. PAGE DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
EVENT SHALL ANDREW E. PAGE BE LIABLE FOR ANY SPECIAL, INDIRECT OR
CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF
USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.

*/

#ifdef _WIN32
#include <windows.h>
#endif

#include <jni_proxy.hh>
#include <JNISupport.hh>
#include <joid_proxy.hh>
#include "jthrow.hh"
#include "NetSNMPjSessionStruct.hh"
#include "lock.hh"
#include <assert.h>
#include <stdlib.h>

#ifndef WIN32
#include <unistd.h>
#endif

#include "org_netsnmp_NetSNMP.h"
#include <net-snmp/version.h>

/*
 * Certain definition are clashing with VS C++ 6
 */
#ifndef _WIN32
#include <net-snmp/agent/net-snmp-agent-includes.h>
#else

#ifdef __cplusplus
extern "C" {
#endif
	int init_agent(const char *) ;
	int unregister_mib_priority(oid *name, size_t len, int priority) ;

#ifdef __cplusplus
}
#endif

#endif // _WIN32

#include <net-snmp/library/default_store.h>
#include <net-snmp/agent/ds_agent.h>

#include <net-snmp/agent/snmp_agent.h>
#include <net-snmp/agent/agent_handler.h>
#include <net-snmp/agent/instance.h>

extern int ParkNativeRead ;
extern jobject_static_field_proxy intAckField ;
extern jobject_static_field_proxy readLockField ;
extern jclass_proxy ioExceptionClass ;
extern jclass_proxy NetSNMP ;
extern jclass_proxy illegalStateExceptionClass ;
/*
 * Classes for agentx support
 */
extern jclass_proxy SET_ACTION ;
extern jclass_proxy SET_COMMIT ;
extern jclass_proxy SET_FREE ;
extern jclass_proxy SET_RESERVE1 ;
extern jclass_proxy SET_RESERVE2 ;
extern jclass_proxy SET_UNDO ;
extern jclass_proxy GET ;
extern jclass_proxy GETNEXT ;
extern jclass_proxy GETBULK ;

extern jmethodID_proxy SET_ACTION_method ;
extern jmethodID_proxy SET_COMMIT_method ;
extern jmethodID_proxy SET_FREE_method ;
extern jmethodID_proxy SET_RESERVE1_method ;
extern jmethodID_proxy SET_RESERVE2_method ;
extern jmethodID_proxy SET_UNDO_method ;
extern jmethodID_proxy GET_method ;
extern jmethodID_proxy GETNEXT_method ;
extern jmethodID_proxy GETBULK_method ;

struct AgentXData {
  jobject jagentx ;
} ;

int java_handler(netsnmp_mib_handler *handler,
		 netsnmp_handler_registration *reginfo,
		 netsnmp_agent_request_info *reqinfo,
		 netsnmp_request_info *requests) ;


/*
 * constructed from net-snmp "instance.c"
 *
 */
static
netsnmp_handler_registration *
get_reg(const char *name,
        const char *ourname,
        oid * reg_oid, size_t reg_oid_len,
        void *it,
        int modes,
        Netsnmp_Node_Handler * scalarh, Netsnmp_Node_Handler * subhandler)
{
    netsnmp_handler_registration *myreg;
    netsnmp_mib_handler *myhandler;

    if (subhandler) {
        myreg =
            netsnmp_create_handler_registration(name,
                                                subhandler,
                                                reg_oid, reg_oid_len,
                                                modes);
        myhandler = netsnmp_create_handler(ourname, scalarh);
        myhandler->myvoid = (void *) it;
        netsnmp_inject_handler(myreg, myhandler);
    } else {
        myreg =
            netsnmp_create_handler_registration(name,
                                                scalarh,
                                                reg_oid, reg_oid_len,
                                                modes);
        myreg->handler->myvoid = (void *) it;
	// fprintf(stderr, "priority = %d/%d\n", myreg->priority, DEFAULT_MIB_PRIORITY) ;
    }
    return myreg;
}


void init_jagentx(JNIEnv *env, jstring agentPort, int pingInterval)
{
  int err ;
  static int initted = 0 ;
  const char *portName ;
  jstring_proxy jStr(env, agentPort) ;
  jproperty_proxy agentPortProp(env, "org.netsnmp.agentx.agentPort") ;
  jproperty_proxy agentPingProp(env, "org.netsnmp.agentx.pingInterval") ;
  if( initted )
    return ;
  initted = 1 ;

  if( agentPort ) {
    jStr = agentPort ;
    portName = jStr ;
  }
  else if( (bool)agentPortProp ){
    portName = agentPortProp ;
  }
  else
    return ; // no point in continuing

  if( pingInterval == 0 && (bool)agentPingProp )
     netsnmp_ds_set_int(NETSNMP_DS_APPLICATION_ID, NETSNMP_DS_AGENT_AGENTX_PING_INTERVAL, agentPingProp) ;  

  jproperty_proxy agentAppName(env, "org.netsnmp.agentx.appname", "netsnmpjagent") ;

  netsnmp_ds_set_string(NETSNMP_DS_APPLICATION_ID, NETSNMP_DS_AGENT_X_SOCKET, portName) ;

  netsnmp_ds_set_boolean(NETSNMP_DS_APPLICATION_ID, NETSNMP_DS_AGENT_ROLE, 1);

  err = init_agent(agentAppName) ;
  if( err ) {
    JEXCEPTION_CHECK(env) ;
    jthrowable je = newThrowable(env, ioExceptionClass, "init_agent failed") ;
    throw Throwable(je) ;
  }
}

jobject init_AgentXMap(JNIEnv *env)
{
  jclass_proxy TreeMapClass(env, "java/util/TreeMap") ;
  jmethodID_proxy TreeMapCtor(env, TreeMapClass, "<init>", "()V") ;
  jstaticFieldID_proxy mapField(env, NetSNMP, "registeredAgents", "Ljava/util/Map;") ;
  jobject theMap ;

  theMap = env->GetStaticObjectField(NetSNMP, mapField) ;
  if( theMap != 0L )
    return theMap ;

  theMap = env->NewObject(TreeMapClass, TreeMapCtor) ;
  JEXCEPTION_CHECK(env) ;

  env->SetStaticObjectField(NetSNMP, mapField, theMap) ;
  JEXCEPTION_CHECK(env) ;

  return theMap ;
}

jobject getAgentX(JNIEnv *env, jobject theMap, jobject joid)
{
  jclass_proxy TreeMapClass(env, "java/util/TreeMap") ;
  jmethodID_proxy getMethod(env, TreeMapClass, "get", "(Ljava/lang/Object;)Ljava/lang/Object;") ;
  jobject obj ;

  obj = env->CallObjectMethod(theMap, getMethod, joid) ;
  JEXCEPTION_CHECK(env) ;
  
  return obj ;
}

jobject putAgentX(JNIEnv *env, jobject theMap, jobject joid, jobject jagentx)
{
  jobject existingObj ;
  jclass_proxy TreeMapClass(env, "java/util/TreeMap") ;
  jmethodID_proxy putMethod(env, TreeMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") ;

  existingObj = env->CallObjectMethod(theMap, putMethod, joid, jagentx) ;
  JEXCEPTION_CHECK(env) ;

  return existingObj ;
}

jobject deleteAgentX(JNIEnv *env, jobject theMap, jobject joid)
{
  jclass_proxy TreeMapClass(env, "java/util/TreeMap") ;
  jmethodID_proxy removeMethod(env, TreeMapClass, "remove", "(Ljava/lang/Object;)Ljava/lang/Object;") ;
  jobject obj ;

  obj = env->CallObjectMethod(theMap, removeMethod, joid) ;
  JEXCEPTION_CHECK(env) ;
  
  return obj ;
}

JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMP_setAgentXSocket
  (JNIEnv *env, jclass clazz, jstring portName, jint pingInterval)
{

  try {
#ifdef _WIN32
    jthrowable je = newThrowable(env, illegalStateExceptionClass, "Agent X support is not yet available for win32") ;
    throw Throwable(je) ;
#endif // _WIN32
    
    JLocker rLock(env, readLockField) ;
    init_jagentx(env, portName, pingInterval) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception already being thrown
  }
}

JNIEXPORT jobject JNICALL Java_org_netsnmp_NetSNMP_unregisterAgentX
  (JNIEnv *env, jclass clazz, jobject joid)
{
  JLocker lck(env, readLockField, false) ;
  JLocker intAck(env, intAckField) ;

  jobject result ;
  try {
#ifdef _WIN32
    jthrowable je = newThrowable(env, illegalStateExceptionClass, "Agent X support is not yet available for win32") ;
    throw Throwable(je) ;
#endif // _WIN32
    
    ParkNativeRead += 1 ;
    SendInterrupt(env) ;

    intAck.wait() ;
    lck.lock() ;

    jobject theMap = init_AgentXMap(env) ;
    joid_proxy oidProxy(env, joid) ;
    
    unregister_mib_priority(oidProxy, oidProxy.getLen(), 0) ;

    result = deleteAgentX(env, theMap, joid) ;

    intAck.notify() ;

    return result ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return 0L ;
  }
  catch( jthrowable je ) {
    return 0L ; // exception already being thrown
  }
}

JNIEXPORT jobject JNICALL Java_org_netsnmp_NetSNMP_registerAgentX
  (JNIEnv *env, jclass clazz, jobject joid, jobject jagentx, jint flags)
{
  JLocker lck(env, readLockField, false) ;
  JLocker intAck(env, intAckField) ;

  try {
#ifdef _WIN32
    jthrowable je = newThrowable(env, illegalStateExceptionClass, "Agent X support is not yet available for win32") ;
    throw Throwable(je) ;
#endif // _WIN32
    int err ;

    if( netsnmp_ds_get_string(NETSNMP_DS_APPLICATION_ID, NETSNMP_DS_AGENT_X_SOCKET) == 0L ) {
      jthrowable je = newThrowable(env, illegalStateExceptionClass, "agentx socket has not been set") ;
      throw Throwable(je) ; // allows locks to unwind
    }

    jobject obj, theMap = init_AgentXMap(env) ;
    joid_proxy oidProxy(env, joid) ;
    struct AgentXData *agentXData = (struct AgentXData *)calloc(1, sizeof(struct AgentXData)) ;
    netsnmp_handler_registration *myreg;

    agentXData->jagentx = env->NewGlobalRef(jagentx) ;

    ParkNativeRead += 1 ;
    
    SendInterrupt(env) ;
    intAck.wait() ;
    lck.lock() ;    

    obj = putAgentX(env, theMap, joid, jagentx) ;
    if( obj != 0L )
      unregister_mib_priority(oidProxy, oidProxy.getLen(), 0) ;

    myreg = get_reg("netsnmpj", "agentx_handler", oidProxy, oidProxy.getLen(), agentXData,
		    flags, java_handler,
		    0L);


    err = netsnmp_register_handler(myreg);

    intAck.notify() ;
    return obj ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return 0L ;
  }
  catch( jthrowable je ) {
    return 0L ; // exception already being thrown
  }
}

int java_handler(netsnmp_mib_handler *handler,
		 netsnmp_handler_registration *reginfo,
		 netsnmp_agent_request_info *reqinfo,
		 netsnmp_request_info *requests)
{
  struct AgentXData *agentXData = (AgentXData *)handler->myvoid ;
  JNIEnv *env = GetJNIEnv() ;
  jobject jagentx = agentXData->jagentx ;
  jobject asnValue, jasnType ;
  joid_proxy joid(env, (int *)requests->requestvb->name, requests->requestvb->name_length) ;
  bool success = true, jsuccess = true ;
  variable_list *vars = requests->requestvb ;
  
  for( ; requests ; requests = requests->next ) {
    for( vars = requests->requestvb ; vars ; vars = vars->next_variable ) {

      switch( reqinfo->mode ) {
      case MODE_GETNEXT: // ??? are we handling this correctly
	if( !GETNEXT.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	asnValue = env->CallObjectMethod(jagentx, GETNEXT_method, (jobject)joid) ;
	if( asnValue == 0L || env->ExceptionCheck() )
	  return SNMP_ERR_GENERR ;

	success = setNativeValue(env, asnValue, 0L, 0L, vars) ;
	break ;


      case MODE_GETBULK: // ???
	if( !GETBULK.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	asnValue = env->CallObjectMethod(jagentx, GETBULK_method, (jobject)joid) ;
	if( asnValue == 0L || env->ExceptionCheck() )
	  return SNMP_ERR_GENERR ;

	success = setNativeValue(env, asnValue, 0L, 0L, vars) ;
	break ;

      case MODE_GET:
	if( !GET.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	asnValue = env->CallObjectMethod(jagentx, GET_method, (jobject)joid) ;
	if( asnValue == 0L || env->ExceptionCheck() )
	  return SNMP_ERR_GENERR ;
    
	success = setNativeValue(env, asnValue, 0L, 0L, vars) ;
	break ;

      case MODE_SET_RESERVE1:
	if( !SET_RESERVE1.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	jasnType = ClassifyASNType(env, vars->type) ;
	if( !jasnType || env->ExceptionCheck() )
	  return SNMP_ERR_GENERR ;
    
	jsuccess = env->CallBooleanMethod(jagentx, SET_RESERVE1_method, (jobject)joid, (jobject)joid, jasnType) ; 
	break ;
      case MODE_SET_RESERVE2:
	if( !SET_RESERVE2.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	jsuccess = env->CallBooleanMethod(jagentx, SET_RESERVE2_method, (jobject)joid) ;
	break ;

      case MODE_SET_ACTION:
	if( !SET_ACTION.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	asnValue = nativeValueToASNValue(env, vars) ;
	if( !asnValue ) {
	  success = false ;
	  break ;
	}
	jsuccess = env->CallBooleanMethod(jagentx, SET_ACTION_method, (jobject)joid, asnValue) ;
	break ;

      case MODE_SET_UNDO:
	if( !SET_UNDO.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	jsuccess = env->CallBooleanMethod(jagentx, SET_UNDO_method, (jobject)joid) ;
	break ;
      case MODE_SET_COMMIT:
	if( !SET_COMMIT.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	jsuccess = env->CallBooleanMethod(jagentx, SET_COMMIT_method, (jobject)joid) ;
	break ;
      case MODE_SET_FREE:
	if( !SET_FREE.isa(env, jagentx) )
	  return SNMP_ERR_NOERROR; // not handled by this object

	jsuccess = env->CallBooleanMethod(jagentx, SET_FREE_method, (jobject)joid) ;
	break ;
      }

      if( !success || !jsuccess || env->ExceptionCheck() )
	return SNMP_ERR_GENERR ;


    } // vars

  } // requests

  return SNMP_ERR_NOERROR;
}

