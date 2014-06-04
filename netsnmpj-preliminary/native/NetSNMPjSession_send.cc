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
#include "org_netsnmp_NetSNMPSession.h"
#include "NetSNMPjSessionStruct.hh"
#include "lock.hh"
#include <string.h>
#include <assert.h>

#include <JNISupport.hh>

extern jclass_proxy ioExceptionClass, NetSNMPException, illegalStateExceptionClass ;
extern jclass_proxy nullPointerExceptionClass ;
extern jfieldID_proxy NSS_isOpen ;

extern jobject_static_field_proxy readLockField ;
/*
 * should return 1 if the request has filled, 0 if the 
 * request should be kept 'pending'.  (i.e. kept on being sent)
 */
int session_callback(int op, netsnmp_session *sess, int reqid,
		     netsnmp_pdu *pdu, void *structPtr)
{
  struct callback_struct *cb_struct = (struct callback_struct *)structPtr ;
  jobject jsession = cb_struct->jsession ;
  JNIEnv *env = GetJNIEnv() ;
  JLocker lck(env) ;
  jobjectArray listenersArray ;
  jobject listener ;
  jobject jpdu, o = cb_struct->obj ;
  extern jfieldID_proxy GetListenersFieldID ;
  extern jmethodID_proxy ListenerActionMethod ;
  jfieldID listenersField = GetListenersFieldID ;
  bool t ;
  int i, n ;
  //fprintf(stderr, "reqid = %d callback op = %d pdu = %p structPtr = %p\n", reqid, op, pdu, structPtr) ;
  
  delete cb_struct ;

  jpdu = pdu_to_jobject(env, pdu) ;
  if( jpdu == 0L ) {
    return 1 ;
  }
  
  if( pdu->reqid ) {
    
    /*
     * Free v1 or v2 TRAP PDU iff no error  
     */
    fprintf(stderr, "freeing\n") ;
    snmp_free_pdu(pdu);
  }

  if( op == NETSNMP_CALLBACK_OP_TIMED_OUT ) {
    /*
     * At the present time we're not treating a timeout
     * operation any differently at this level
     */
    //printf("timed out\n") ;
  }

  listenersArray = (jobjectArray)env->GetObjectField(jsession, listenersField) ;

  n = env->GetArrayLength(listenersArray) ;

  // call listeners

  for( i = 0, t = true ; (i < n) && t; i++ ) {
    listener = env->GetObjectArrayElement(listenersArray, i) ;

    t = env->CallBooleanMethod(listener, ListenerActionMethod, op, jsession, jpdu, o) ;
    JEXCEPTION_CHECK(env) ;
  }
  lck.unlock() ;

  /*
   * request sucessfully processed
   */

  /*
   * mark the reference to our session contained in this
   * callback as clear for garbage collection.  NOTE:
   * this does not mean that the session object iself will
   * collected, just this reference that's being used.
   */
  env->DeleteGlobalRef(jsession) ;

  /*
   * mark the reference to the object as being available 
   * for garbage collection
   */
  env->DeleteGlobalRef(o) ;
  return 1 ;
}

JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMPSession_send(JNIEnv *env, jobject jsession, jobject jpdu, jobject o)
{
  extern jclass_proxy SNMPSendError ;
  if( jpdu == 0L ) {
    env->ThrowNew(nullPointerExceptionClass, "null pdu argument") ;
    return ;
  }

  try {
    JLocker lck(env, readLockField) ;
    struct NetSNMPjSessionStruct *sessStruct = GetSessionStruct(env, jsession) ;
    int err ;

    if( !env->GetBooleanField(jsession, NSS_isOpen) ) {
      lck.unlock() ;
      env->ThrowNew(illegalStateExceptionClass, "Session is not Open") ;
      return ;
    }

    struct callback_struct *cb_struct = new struct callback_struct ;

    netsnmp_pdu *pdu = jobject_to_pdu(env, jpdu, 0) ;
    if( pdu == 0L )
      return ;

    cb_struct->jsession = env->NewGlobalRef(jsession) ;

    assert(cb_struct->jsession && jsession) ;

    cb_struct->obj = env->NewGlobalRef(o) ;
    
    //fprintf(stderr, "sending structPtr = %p\n", cb_struct) ;
    err = snmp_async_send(sessStruct->sessp, pdu, session_callback, cb_struct) ;
    if( sessStruct->sessp->s_snmp_errno != 0 || sessStruct->sessp->s_errno != 0 ) {
      lck.unlock() ; // has to be unlocked before we throw
      jthrowable je = ThrowableError(env, SNMPSendError, sessStruct->sessp->s_snmp_errno, sessStruct->sessp->s_errno) ;
      throw je ;
      return ;
    }
  }
  catch( JNIProxyException e ) {
    // fprintf(stderr, "caught JNIProxyException during send\n") ;
    return ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return ;
  }
  catch( jthrowable je ) {
    return ; // exception being thrown 
  }
  catch( ... ) {
    fprintf(stderr, "caught unknown c++ exception during send\n") ;
    return ;
  }
   SendInterrupt(env) ; // wake up running session to update interal libs
}

jthrowable ThrowableError(JNIEnv *env, jclass clazz, int s_snmp_errno, int s_errno)
{
  extern jclass_proxy SNMPSendError, SNMPErrTooBig, illegalStateExceptionClass ;
  extern jmethodID_proxy SNMPSendError_ctor, SNMPErrTooBig_ctor, illegalStateExceptionCtor ;
  jthrowable jexception ;
  jmethodID mid = 0 ;
  jstring jstr ;
  char errBuffer[256] ;
  errBuffer[0] = 0 ; // default
  bool javaException = false ;

  if( clazz == 0L ) {
    clazz = SNMPSendError ;
    mid = SNMPSendError_ctor ;
  }

  switch( s_snmp_errno ) {
  case SNMPERR_BAD_ADDRESS:
    snprintf(errBuffer, sizeof(errBuffer), "host not found") ;
	clazz = illegalStateExceptionClass ;
	mid = illegalStateExceptionCtor ;
	javaException = true ;
    break ;

  case SNMPERR_TOO_LONG:
    clazz = SNMPErrTooBig ;
    mid = SNMPErrTooBig_ctor ;
    jexception = (jthrowable)env->NewObject(SNMPErrTooBig, SNMPErrTooBig_ctor) ;
	strcpy(errBuffer, "PDU too large") ;
    break ;
  case SNMPERR_USM_AUTHENTICATIONFAILURE:
  case SNMPERR_USM_ENCRYPTIONERROR:
    { // braces ensure we don't intialize unnecessarily
    jclass_proxy authExcp(env, "org/netsnmp/NetSNMPAuthenticationException") ;
    jmethodID_proxy authExcpCtor(env, authExcp, "<init>", "(IILjava/lang/String;)V") ;
    clazz = authExcp ;
    mid = authExcpCtor ;
    }
    break ;
  default:
    clazz = SNMPSendError ;
    mid = SNMPSendError_ctor ;
    
    errBuffer[sizeof(errBuffer)-1] = 0 ;
    strncpy(errBuffer, snmp_api_errstring(s_snmp_errno), sizeof(errBuffer)) ;
    
    break ;
  }

  assert(mid) ;
  assert(clazz) ;

  jstr = env->NewStringUTF(errBuffer) ;
  if( javaException )
	jexception = (jthrowable)env->NewObject(clazz, mid, jstr) ;
  else
	jexception = (jthrowable)env->NewObject(clazz, mid, s_snmp_errno, s_errno, jstr) ;
  assert(jexception) ;
  throw Throwable(jexception) ;
}

/*
 * $Log: NetSNMPjSession_send.cc,v $
 * Revision 1.15  2003/06/01 16:03:49  aepage
 * merget from release-0.2.1
 *
 * Revision 1.14.2.1  2003/05/22 08:56:13  aepage
 * modifications to support RH 7.3 RH 8.0 cross compatiblity.
 *
 * Revision 1.14  2003/05/04 21:34:03  aepage
 * JEXCEPTION_CHECK macro replacing exception check.
 *
 * Revision 1.13  2003/04/30 00:48:03  aepage
 * attempt to threadify access to snmp_api error strings.
 *
 * Revision 1.12  2003/04/29 12:20:15  aepage
 * correction for finding correct error string.
 *
 * Revision 1.11  2003/04/27 16:31:42  aepage
 * exception support improvements
 *
 * Revision 1.10  2003/04/25 23:05:14  aepage
 * fixes from win32 compilation
 *
 * Revision 1.9  2003/04/18 01:34:20  aepage
 * new field ID's and error checking support
 *
 * Revision 1.8  2003/04/15 19:27:19  aepage
 * new exception for SNMPv3 Support
 *
 * Revision 1.7  2003/03/29 00:13:38  aepage
 * new thread architecture
 *
 * Revision 1.6  2003/03/22 00:22:31  aepage
 * lock strategy updates
 *
 * Revision 1.5  2003/03/21 15:24:25  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.4  2003/03/20 00:05:33  aepage
 * Removal of deprecated SetProxyEnv macro
 *
 * Revision 1.3  2003/02/27 17:47:18  aepage
 * moved cb_struct into NetSNMPSessionStruct.hh.  Dead code removed.
 * Minor aesthetic fixes.
 *
 * Revision 1.2  2003/02/15 18:01:27  aepage
 * support for use of a typesafe enum to specify the version of snmp to
 * use.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
