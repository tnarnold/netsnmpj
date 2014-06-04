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

/**
 * @author Andrew E. Page<aepage@users.sourceforge.net>
 */

#ifdef _WIN32
#include <windows.h>
#endif

#include <jni_proxy.hh>
#include <JNISupport.hh>
#include <joid_proxy.hh>
#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>
#include <net-snmp/mib_api.h>
#include <net-snmp/library/snmp_client.h>
#include <net-snmp/library/keytools.h>

#ifdef WIN32
#include <native.h>
#else
#include <strings.h>
#endif

extern jclass_proxy nullPointerExceptionClass ;

extern jobject_static_field_proxy noAuth ;
extern jobject_static_field_proxy authNoPriv ;
extern jobject_static_field_proxy authPriv ;

extern jclass_proxy DefaultOIDClass ;
extern jmethodID_proxy DefaultOIDCtor ;

extern jclass_proxy NetSNMPException, illegalStateExceptionClass ;
extern jfieldID_proxy SecurityLevelID ;

extern jfieldID_proxy NSS_version ;
extern jfieldID_proxy NSS_retries ;
extern jfieldID_proxy NSS_timeout ;
extern jfieldID_proxy NSS_peerName ;
extern jfieldID_proxy NSS_community ;
extern jfieldID_proxy NSS_rcvMsgMaxSize ;
extern jfieldID_proxy NSS_sndMsgMaxSize ;
extern jfieldID_proxy NSS_contextEngineID ;
extern jfieldID_proxy NSS_contextName ;
extern jfieldID_proxy NSS_securityEngineID ;
extern jfieldID_proxy NSS_securityName ;
extern jfieldID_proxy NSS_securityAuthOID ;
extern jfieldID_proxy NSS_authPassword ;
extern jfieldID_proxy NSS_securityAuthKey ;
extern jfieldID_proxy NSS_privPassword ;
extern jfieldID_proxy NSS_securityPrivKey ;
extern jfieldID_proxy NSS_securityModel ;
extern jfieldID_proxy NSS_securityLevel ;
extern jfieldID_proxy NSS_isOpen ;
extern jfieldID_proxy NSS_securityPrivOID ;
extern jobject_static_field_proxy SNMPv1Enum, SNMPv2cEnum, SNMPv3Enum ;

/**
 * Take fields from the native session structure and put them into 
 * the java object
 */
void setSessionFields(JNIEnv *env, netsnmp_session *sessp, jobject jsession)
{
  jobject obj ;
  jstring_proxy strProxy(env) ;
  joid_proxy joidProxy(env) ;

  /*
   * Version Field
   */
  switch( sessp->version ) {
  case SNMP_VERSION_1:
    obj = SNMPv1Enum ;
    break ;
  case -1: // default
  case SNMP_VERSION_2c:
    obj = SNMPv2cEnum ;
    break ;
  case SNMP_VERSION_3:
    obj = SNMPv3Enum ;
    break ;
  default:
    throw Throwable(newThrowable(env, NetSNMPException, "version %d not supported", sessp->version)) ;
    break ;
  }

  env->SetObjectField(jsession, NSS_version, obj) ;

  /*
   * retries
   */

  env->SetIntField(jsession, NSS_retries, sessp->retries) ;

  /*
   * timeout
   */

  env->SetLongField(jsession, NSS_timeout, sessp->timeout) ;

  /*
   * peername
   */
  strProxy = sessp->peername ;

  env->SetObjectField(jsession, NSS_peerName, strProxy) ;

  /*
   * NSS_community
   */

  env->SetObjectField(jsession, 
		      NSS_community, 
		      jstring_proxy(env, (char *)sessp->community, sessp->community_len)) ;

  /*
   * rcvMsgMaxSize
   */

  env->SetLongField(jsession, NSS_rcvMsgMaxSize, sessp->rcvMsgMaxSize) ;

  /*
   * sndMsgMaxSize
   */
  env->SetLongField(jsession, NSS_sndMsgMaxSize, sessp->sndMsgMaxSize) ;

  /*
   * contextEngineID
   */
  env->SetObjectField(jsession, 
		      NSS_contextEngineID, 
		      byteArray_proxy(env, (char *)sessp->contextEngineID, sessp->contextEngineIDLen)) ;

  /*
   * contextName
   */
  env->SetObjectField(jsession,
		      NSS_contextName,
		      jstring_proxy(env, (char *)sessp->contextName, sessp->contextNameLen)) ;

  /*
   * securityName
   */
  
  env->SetObjectField(jsession, 
		      NSS_community, 
		      jstring_proxy(env, sessp->securityName, sessp->securityNameLen)) ;

  /*
   * securityEngineID 
   */
  env->SetObjectField(jsession,
		      NSS_securityEngineID,
		      byteArray_proxy(env, (char *)sessp->securityEngineID, sessp->securityEngineIDLen)) ;

  /*
   * authProto oid
   */
  obj = env->NewObject(DefaultOIDClass, 
		       DefaultOIDCtor, 
		       (jobject)intArray_proxy(env, (int *)sessp->securityAuthProto, sessp->securityAuthProtoLen)) ;

  env->SetObjectField(jsession,
		      NSS_securityAuthOID,
		      obj) ;

  /*
   * securityAuthKey
   */

  env->SetObjectField(jsession,
		      NSS_securityAuthKey,
		      byteArray_proxy(env, (char *)sessp->securityAuthKey, sessp->securityAuthKeyLen)) ;

  /*
   * securityPrivOID
   */
  obj = env->NewObject(DefaultOIDClass, 
		       DefaultOIDCtor, 
		       (jobject)intArray_proxy(env, (int *)sessp->securityPrivProto, sessp->securityPrivProtoLen)) ;

  env->SetObjectField(jsession,
		      NSS_securityPrivOID,
		      obj) ;

  /*
   * securityPrivKey
   */
  env->SetObjectField(jsession,
		      NSS_securityPrivKey,
		      byteArray_proxy(env, (char *)sessp->securityPrivKey, sessp->securityPrivKeyLen)) ;

  /*
   * securityModel
   */
  env->SetIntField(jsession,
		   NSS_securityModel,
		   sessp->securityModel) ;

  /*
   * securityLevel
   */
  switch( sessp->securityLevel ) {
  case 0: // default
  case SNMP_SEC_LEVEL_NOAUTH:
    obj = noAuth ;
    break ;
  case SNMP_SEC_LEVEL_AUTHNOPRIV:
    obj = authNoPriv ;
    break ;
  case SNMP_SEC_LEVEL_AUTHPRIV:
    obj = authPriv ;
    break ;
  default:
    throw Throwable(newThrowable(env, illegalStateExceptionClass, "unknown authorization level %d", sessp->securityLevel)) ;
    break ;
  }
  env->SetObjectField(jsession, NSS_securityLevel, obj) ;
}


extern jfieldID_proxy SNMPVersionID ;
/**
 * Take the fields from the java object and put them into the session structure
 */
void getSessionFields(JNIEnv *env, netsnmp_session *sessp, jobject jsession)
{
  jobject obj ;
  jstring_proxy jstrProxy(env) ;
  byteArray_proxy byteProxy(env) ;
  joid_proxy joidProxy(env) ;
  const char *str ;
  int err ;
  // int x = 1 ;
  /*
   * Version Field
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  obj = env->GetObjectField(jsession, NSS_version) ;
  sessp->version = env->GetIntField(obj, SNMPVersionID) ;

  /*
   * retries
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  sessp->retries = env->GetIntField(jsession, NSS_retries) ;

  /*
   * timeout
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  sessp->timeout = (long)env->GetLongField(jsession, NSS_timeout) ;

  /*
   * peerName
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->peername )
    free(sessp->peername) ;
  obj = env->GetObjectField(jsession, NSS_peerName) ;
  if( obj == 0L )
    throw Throwable(newThrowable(env, nullPointerExceptionClass, "null peername")) ;

  jstrProxy = (jstring)obj ;
  if( jstrProxy.length() > 0 )
    sessp->peername = strdup(jstrProxy) ;
  else
    sessp->peername = 0L ;

  /*
   * community
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->community )
    free(sessp->community) ;
  obj = env->GetObjectField(jsession, NSS_community) ;

  jstrProxy = (jstring)obj ;

  str = jstrProxy ;
  if( str ) {
    sessp->community = (u_char *)strdup(str) ;
    sessp->community_len = jstrProxy.length() ;
  }
  else {
    sessp->community = 0L ;
    sessp->community_len = 0 ;
  }

  /*
   * rcvMsgMaxSize
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  sessp->rcvMsgMaxSize = (long)env->GetLongField(jsession, NSS_rcvMsgMaxSize) ;
  
  /*
   * sndMsgMaxSize
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  sessp->sndMsgMaxSize = (long)env->GetLongField(jsession, NSS_sndMsgMaxSize) ;

  /*
   * contextName
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->contextName )
    free( sessp->contextName ) ;

  jstrProxy = (jstring)env->GetObjectField(jsession, NSS_contextName) ;
  if( jstrProxy.length() > 0 ) {
    sessp->contextName = strdup(jstrProxy) ;
    sessp->contextNameLen = jstrProxy.length() ;
  }
  else {
    sessp->contextName = 0L ;
    sessp->contextNameLen = 0 ;
  }

  /*
   * contextEngineID
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->contextEngineID )
    free( sessp->contextEngineID ) ;

  byteProxy = (jbyteArray)env->GetObjectField(jsession, NSS_contextEngineID) ;
  str = byteProxy ;
  sessp->contextEngineID = (u_char *)str ;
  sessp->contextEngineIDLen = byteProxy.getLen() ;

  /*
   * securityName
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->securityName )
    free(sessp->securityName) ;

  obj = env->GetObjectField(jsession, NSS_securityName) ;
  if( obj != 0L ) {
    jstrProxy = (jstring)obj ;
    str = jstrProxy ;
    sessp->securityName = (char *)strdup(str) ;
    sessp->securityNameLen = jstrProxy.length() ;
  }
  else {
    sessp->securityName = 0L ;
    sessp->securityNameLen = 0 ;
  }

  /*
   * securityAuthProto
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->securityAuthProto )
    free(sessp->securityAuthProto) ;

  joidProxy = env->GetObjectField(jsession, NSS_securityAuthOID) ;
  sessp->securityAuthProto = (oid *)joidProxy.dup() ;
  sessp->securityAuthProtoLen = joidProxy.getLen() ;

  /*
   * securityPrivProto
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  if( sessp->securityPrivProto )
    free(sessp->securityPrivProto) ;

  joidProxy = env->GetObjectField(jsession, NSS_securityPrivOID) ;
  sessp->securityPrivProto = (oid *)joidProxy.dup() ;
  sessp->securityPrivProtoLen = joidProxy.getLen() ;
  
  /*
   * security model
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  sessp->securityModel = env->GetIntField(jsession, NSS_securityModel) ;
  
  /*
   * security level
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  obj = env->GetObjectField(jsession, NSS_securityLevel) ;
  sessp->securityLevel = env->GetIntField(obj, SecurityLevelID) ;


  /*
   * securityAuthKey
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  jstrProxy = (jstring)env->GetObjectField(jsession, NSS_authPassword) ;
  str = jstrProxy ;

  if( str != 0L && jstrProxy.length() > 0 ) {
    sessp->securityAuthKeyLen = USM_AUTH_KU_LEN ;
    err = generate_Ku(sessp->securityAuthProto,
		      sessp->securityAuthProtoLen,
		      (u_char *)str, jstrProxy.length(),
		      sessp->securityAuthKey,
		      &sessp->securityAuthKeyLen) ;
    if( err != 0 ) 
      throw Throwable(newThrowable(env, NetSNMPException, "auth generate_Ku err = %d", err)) ;
  }

  

  /*
   * securityPrivKey
   */
  // fprintf(stderr, "pt = %d\n", x++) ;
  jstrProxy = (jstring)env->GetObjectField(jsession, NSS_authPassword) ;
  str = jstrProxy ;
  if( str != 0L && jstrProxy.length() > 0 ) {
    sessp->securityPrivKeyLen = USM_AUTH_KU_LEN ;
    err = generate_Ku(sessp->securityAuthProto,
		      sessp->securityAuthProtoLen,
		      (u_char *)str, jstrProxy.length(),
		      sessp->securityPrivKey,
		      &sessp->securityPrivKeyLen) ;
    if( err != 0 ) 
      throw Throwable(newThrowable(env, NetSNMPException, "priv generate_Ku err = %d", err)) ;
  }

}
