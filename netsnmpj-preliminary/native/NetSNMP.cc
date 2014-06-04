
/*

	       Copyright(c) 2003 by Andrew E. Page

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

#include <net-snmp/library/default_store.h>
#include <net-snmp/agent/ds_agent.h>

#ifndef _WIN32
#include <net-snmp/agent/net-snmp-agent-includes.h>
#endif

#include <net-snmp/agent/snmp_agent.h>
#include <net-snmp/agent/agent_handler.h>
#include <net-snmp/agent/instance.h>

/*
 * definitions here instead of from net-snmp logging include file
 * because of a conflict with va_args windows definitions
 */
extern "C" {
	void snmp_disable_stderrlog(void) ;
	void snmp_enable_stderrlog(void) ;
}

#include "org_netsnmp_NetSNMPSession.h"

extern int InterruptPipe[2] ;

extern jobject_static_field_proxy readLockField ;
extern jclass_proxy ioExceptionClass ;

int allow_severity, deny_severity ;

#if !defined(__GNUC__) || defined(_WIN32)
ProxyList JNIClassProxies ;
ProxyList JNIIDProxies ;
#endif

void init_jagentx(JNIEnv *env) ;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
  JNIEnv *env ;
  int err ;
  bool success ;

  try {
    err = jvm->GetEnv((void **)&env, JNI_VERSION_1_2) ;
    if( err != JNI_OK && env == NULL ) 
      return JNI_ERR ;
    success = InitJNIProxies(env) ;

    if( !success )
      return JNI_VERSION_1_2 ; // versions ok even if we're not
  
    JLocker lck(env) ;

    /* we should only need to do this once */

#ifdef WIN32
    winsock_startup() ;
#endif

    snmp_disable_stderrlog() ;
#ifndef WIN32
    err = pipe(InterruptPipe) ;
    if( err == -1 ) {
      ThrowIOException(env, errno, " setting up interrupt pipe") ;
      return JNI_VERSION_1_2 ;
    }
#else

    if( !Win32SocketPair(InterruptPipe) ) {
      ThrowIOException(env, WSAGetLastError(), " setting up interrupt pipe") ;
      return JNI_VERSION_1_2 ;
    }

#endif
    lck = readLockField ;

    // init_jagentx(env) ;

    init_snmp(0L) ;

    nativeThreadStart(env) ;

  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return JNI_VERSION_1_2 ;
  }
  catch( jthrowable je ) {
    return JNI_VERSION_1_2 ; // exception already being thrown
  }

  return JNI_VERSION_1_2 ;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved)
{
  JNIEnv *env ;
  int err ;

  err = jvm->GetEnv((void **)&env, JNI_VERSION_1_2) ;
  if( err != JNI_OK && env == NULL ) 
    return ;
  
  DeleteJNIProxies(env) ;
}


JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMP_enableStderrLogging
  (JNIEnv *env, jclass, jboolean flag)
{
  try {
    JLocker lck(env, readLockField) ;
    if( flag )
      snmp_enable_stderrlog() ;
    else
      snmp_disable_stderrlog() ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return  ;
  }
  catch( jthrowable je ) {
    return  ; // exception already being thrown
  }

}

JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMP_doDebugging
  (JNIEnv *env, jclass, jboolean flag)
{
  try {
    JLocker lck(env, readLockField) ;
    snmp_set_do_debugging(flag) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception already being thrown
  }

}

JNIEXPORT jstring JNICALL Java_org_netsnmp_NetSNMP_getNetSNMPVersion
  (JNIEnv *env, jclass)
{
  try {
    JLocker lck(env, readLockField) ;
    return env->NewStringUTF(netsnmp_get_version()) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return 0L ;
  }
  catch( jthrowable je ) {
    return 0L ; // exception already being thrown
  }

}



/*
 * $Log: NetSNMP.cc,v $
 * Revision 1.20  2003/06/01 16:03:49  aepage
 * merget from release-0.2.1
 *
 * Revision 1.19.2.1  2003/05/21 00:47:24  aepage
 * fixes to absorb MIB functions into the main library in order to
 * support RH7.3 and perhaps other flavors as well.
 *
 * Revision 1.19  2003/05/05 16:30:43  aepage
 * AgentX fix
 *
 * Revision 1.18  2003/05/04 21:37:28  aepage
 * fixes to catch blocks and methods to support subagent.
 *
 * Revision 1.17  2003/05/03 23:37:30  aepage
 * changes to support agentX subagents
 *
 * Revision 1.16  2003/04/25 23:05:24  aepage
 * fixes from win32 compilation
 *
 * Revision 1.15  2003/04/18 01:42:51  aepage
 * new exception handling
 *
 * Revision 1.14  2003/04/15 19:28:08  aepage
 * SNMPv3 Support
 *
 * Revision 1.13  2003/04/12 01:32:34  aepage
 * added try/catch block
 *
 * Revision 1.12  2003/03/31 17:43:58  aepage
 * fixes for shared lib issues
 *
 * Revision 1.11  2003/03/30 22:02:31  aepage
 * fixes for win32
 *
 * Revision 1.10  2003/03/29 23:19:42  aepage
 * continuation of fixes for static initializers and shared libraries.
 * Next stop win32.
 *
 * Revision 1.9  2003/03/29 18:12:12  aepage
 * Fix to the bedamned issue of static constructors between SUNpro C++
 * and gnu g++.  The trick(for SunPRO) seems to have the global list
 * instantiated in the main body of the code rather than the support
 * library.  It remains to be seen if this will hold up for g++, but at
 * least, the link order seems to remain the same.
 *
 * Revision 1.8  2003/03/29 00:13:38  aepage
 * new thread architecture
 *
 * Revision 1.7  2003/03/21 15:08:54  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.6  2003/03/17 16:11:12  aepage
 * added doDebugging method and now using jthrow faclilities.
 *
 */ 
