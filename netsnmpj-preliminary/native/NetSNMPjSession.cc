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
#include <joid_proxy.hh>
#include <JNISupport.hh>
#include "org_netsnmp_NetSNMPSession.h"
#include "NetSNMPjSessionStruct.hh"
#include "lock.hh"
#include <assert.h>
#include <stdlib.h>

#ifndef _WIN32
#include <unistd.h>
#include <strings.h>
#else
#endif

#include <net-snmp/library/snmp_api.h>
#include <net-snmp/library/keytools.h>
#include <net-snmp/library/snmp_secmod.h>
#include <net-snmp/library/snmpusm.h>

/*
 * Initialize and check the ids
 * we've see where they can 'change'
 * we'll track here to see if the situation
 * is in fact the case, or someother problem
 */

#define JABORT(tst, s) if((tst)) throw (const char *)s ;

int ParkNativeRead = 0 ;

extern jobject_static_field_proxy readLockField ;
extern jobject_static_field_proxy intAckField ;

extern jclass_proxy NetSNMPSession, SNMPEventListenerClass ;

extern jfieldID_proxy sessionHandleFieldID, GetListenersFieldID ;

extern jmethodID_proxy ListenerActionMethod ;

extern jclass_proxy PDUCommandClass ;

extern jclass_proxy illegalStateExceptionClass ;
extern jclass_proxy nullPointerExceptionClass ;

extern jobject_static_field_proxy SNMPv1Enum, SNMPv2cEnum, SNMPv3Enum ;
extern jclass_proxy NetSNMPException ;
extern jfieldID_proxy SNMPVersionID, SecurityLevelID, sessionPeerField, sessionCommunityField ;
extern jfieldID_proxy authProtocolFieldID, privProtocolFieldID ;
extern jfieldID_proxy NSS_isOpen ;
/**
 * Extracts the C++ NetSNMPjSessionStruct pointer from the object
 * that a method is being invoked on.
 *
 */
struct NetSNMPjSessionStruct *GetSessionStruct(JNIEnv *env, jobject obj)
{
  jlong theField ;
#ifdef PTR64
  unsigned long long theFieldL ;
#else
  unsigned long theFieldL ;
#endif

  assert(env) ;
  assert(obj) ;
  theField = env->GetLongField(obj, sessionHandleFieldID) ;
  theFieldL = (long)theField ;
  
  return (NetSNMPjSessionStruct *)theFieldL ;
}


/*
 * Class:     org_netsnmp_NetSNMPSession
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMPSession_nativeInit
(JNIEnv *env, jobject jsession)
{
  try {
    struct NetSNMPjSessionStruct *sessStruct = (NetSNMPjSessionStruct *)calloc(1, sizeof(NetSNMPjSessionStruct)) ;
    snmp_sess_init(&sessStruct->sessData) ;

    env->SetLongField(jsession, sessionHandleFieldID, (jlong)(unsigned long)sessStruct) ;

    setSessionFields(env, &sessStruct->sessData, jsession) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception being thrown 
  }

}

/**
 *  Called from nativeThread to close handles before another select call is made
 * We do not use the lock at this point, since this is, currently, only called from the 
 * native thread where we expect to hold the lock anyway.  
 */
JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMPSession_nativeFinalize
  (JNIEnv *env, jclass, jlong nativeHandle)
{
  try {
    /*
     * jlongs are 64-bits so we get a warning about the cast from some compilers
     */
    struct NetSNMPjSessionStruct *sessStruct = (NetSNMPjSessionStruct *)nativeHandle ; 

    if( sessStruct->sessp && sessStruct->isOpen )
      snmp_close(sessStruct->sessp) ;

    if( sessStruct->sessData.peername != 0L )
      free(sessStruct->sessData.peername) ;

    if( sessStruct->sessData.community != 0L )
      free(sessStruct->sessData.community) ;

    if( sessStruct->sessData.contextEngineID )
      free(sessStruct->sessData.contextEngineID) ;

    if( sessStruct->sessData.contextName )
       free(sessStruct->sessData.contextName) ;

    if( sessStruct->sessData.securityName )
      free(sessStruct->sessData.securityName) ;

    if( sessStruct->sessData.securityPrivProto != 0L ) 
      free(sessStruct->sessData.securityPrivProto) ;
    if( sessStruct->sessData.securityAuthProto != 0L ) 
      free(sessStruct->sessData.securityAuthProto) ;

    free(sessStruct) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception already being thrown
  }
}

JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMPSession_updateSession
  (JNIEnv *env, jobject jsession)
{
  try {
    struct NetSNMPjSessionStruct *sessStruct = GetSessionStruct(env, jsession) ;
    if( !env->GetBooleanField(jsession, NSS_isOpen) )
      return ;
    getSessionFields(env, &sessStruct->sessData, jsession) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception being thrown 
  }

} // end of Java_org_netsnmp_NetSNMPSession_updateSession

/*
 * Class:     org_netsnmp_NetSNMPSession
 * Method:    open
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMPSession_open(JNIEnv *env, jobject jsession)
{
  try {
    JLocker lck(env, readLockField) ;
    netsnmp_session *sessp ;
    struct NetSNMPjSessionStruct *sessStruct = GetSessionStruct(env, jsession) ;
    getSessionFields(env, &sessStruct->sessData, jsession) ;

    if( !sessStruct->sessData.peername ) {
      lck.unlock() ;
      env->ThrowNew(illegalStateExceptionClass, "peername not set") ;
      return ;
    }

    sessp = snmp_open(&sessStruct->sessData) ;
    sessStruct->sessp = sessp ;

    if( !sessp ) {
      lck.unlock() ;
      ThrowableError(env, 0L, sessStruct->sessData.s_snmp_errno, -1) ;
      return ;
    }
    sessStruct->isOpen = true ;
    env->SetBooleanField(jsession, NSS_isOpen, (jboolean)true) ;

  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception being thrown
  }
  catch( JNIProxyException e ) {
    return ;
  }

  SendInterrupt(env) ; // wake up running session to update interal libs

} // end of Java_org_netsnmp_NetSNMPSession_nativeInit

/*
 * $Log: NetSNMPjSession.cc,v $
 * Revision 1.31  2003/06/08 00:57:59  aepage
 * agentX fixes and a new finalizer scheme for sessions
 *
 * Revision 1.30  2003/06/07 20:15:22  aepage
 * agentx fixes
 *
 * Revision 1.29  2003/06/07 15:57:22  aepage
 * AgentX fixes
 *
 * Revision 1.28  2003/06/01 16:03:49  aepage
 * merget from release-0.2.1
 *
 * Revision 1.27.2.1  2003/05/22 08:56:13  aepage
 * modifications to support RH 7.3 RH 8.0 cross compatiblity.
 *
 * Revision 1.27  2003/05/04 21:34:51  aepage
 * fix of try/catch block
 *
 * Revision 1.26  2003/05/02 13:19:26  aepage
 * correction to field that get's updated.
 *
 * Revision 1.25  2003/04/30 00:48:03  aepage
 * attempt to threadify access to snmp_api error strings.
 *
 * Revision 1.24  2003/04/27 16:31:42  aepage
 * exception support improvements
 *
 * Revision 1.23  2003/04/27 11:53:08  aepage
 * More careful synchronization of the close operation, and freed an
 * allocated field from the native structure.
 *
 * Revision 1.22  2003/04/25 23:00:27  aepage
 * Fix for a thread race condition that was not making itself apparent
 * for Linux when sessions were being closed.
 *
 * Revision 1.21  2003/04/23 17:28:35  aepage
 * Fix to 'close' operation that tightens up the locking operations of
 * the nativeThread.  This corrects some 'bad file' errors on select for
 * Solaris that were not occurring under Linux
 *
 * Revision 1.20  2003/04/18 01:43:46  aepage
 * migration of fields to java properties and SNMPv3 support
 *
 * Revision 1.19  2003/04/15 19:25:44  aepage
 * Better exception handling and SNMPv3 Support
 *
 * Revision 1.18  2003/04/11 19:25:42  aepage
 * field accesses
 *
 * Revision 1.17  2003/03/30 22:02:29  aepage
 * fixes for win32
 *
 * Revision 1.16  2003/03/29 17:24:12  aepage
 * correction of a programming error
 *
 * Revision 1.15  2003/03/29 00:13:38  aepage
 * new thread architecture
 *
 * Revision 1.14  2003/03/22 00:22:32  aepage
 * lock strategy updates
 *
 * Revision 1.13  2003/03/21 15:24:26  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.12  2003/03/20 00:05:34  aepage
 * Removal of deprecated SetProxyEnv macro
 *
 * Revision 1.11  2003/03/17 19:25:32  aepage
 * include fix
 *
 * Revision 1.10  2003/03/17 16:12:36  aepage
 * 64 bit system tenuous support
 *
 * Revision 1.9  2003/02/27 17:29:39  aepage
 * Movement of Static initializer to NetSNMPjClasses.cc.  changed
 * sprintfs to snprintfs to thwart possible attackers.
 *
 * Revision 1.8  2003/02/24 00:05:20  aepage
 * New interrupt scheme that should work for both Win32 and Unix/Linux.
 *
 * Revision 1.7  2003/02/23 21:30:54  aepage
 * assertion fix
 *
 * Revision 1.6  2003/02/23 17:21:30  aepage
 * removal of fprintf that should not have been checked in
 *
 * Revision 1.5  2003/02/22 23:34:24  aepage
 * Win32 Port fixes
 *
 * Revision 1.4  2003/02/17 18:18:23  aepage
 * fixed signature issue for setRetries.  int => jint
 *
 * Revision 1.3  2003/02/15 18:01:29  aepage
 * support for use of a typesafe enum to specify the version of snmp to
 * use.
 *
 * Revision 1.2  2003/02/08 22:14:11  aepage
 * solaris fixes
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 * Revision 1.3  2003/02/07 14:13:23  aepage
 * Refactored NetSNMPPDU to PDU
 *
 * Revision 1.2  2003/02/06 23:19:17  aepage
 * disabled stderr logging in both libraries.  There is support in
 * the NetSNMP object for enabling it if necessary.
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
