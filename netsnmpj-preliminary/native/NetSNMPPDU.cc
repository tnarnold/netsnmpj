
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
#include "JNISupport.hh"
#include <joid_proxy.hh>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#ifndef _WIN32
#include <strings.h>
#endif
#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>

#include "org_netsnmp_PDU.h"

#include <JNISupport.hh>

extern jobject_static_field_proxy readLockField ;

const char *new_line = "\n" ; // TDB take this from System.getProperty("line.separator")
int new_line_len = strlen(new_line) ;

extern jclass_proxy illegalStateExceptionClass ;
extern jfieldID_proxy PDUErrorStatus, PDUErrorIndex ;

/*
 * Class:     snmp_PDU
 * Method:    size
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_snmp_PDU_size
  (JNIEnv *env, jobject jpdu)
{
  netsnmp_pdu *pdu ;
  u_char buffer[1500] ; // avg ethernet pdu
  size_t len = sizeof(buffer) ;
  
  pdu = jobject_to_pdu(env, jpdu, 0) ;

  snmp_pdu_build(pdu, buffer, &len) ;

  snmp_free_pdu(pdu) ;

  return sizeof(buffer) - len ;
}

#define MAX_VARS 128

JNIEXPORT jstring JNICALL Java_org_netsnmp_PDU_toString(JNIEnv *env, jobject jpdu)
{

  try {
    JLocker lck(env, readLockField) ;
    netsnmp_pdu *pdu ;
    int i, n, len, offset ;
    netsnmp_variable_list *var ;
    u_char *oid_buffers[MAX_VARS], *var_buffers[MAX_VARS] ;
    size_t buflen, oid_outlens[MAX_VARS], var_outlens[MAX_VARS] ;
    char *final_buffer ;
    jstring jStr ;

    pdu = jobject_to_pdu(env, jpdu, 0) ;
    if( pdu == 0L ) {
      if( env->ExceptionCheck() )
	return 0L ;
      env->ThrowNew(illegalStateExceptionClass, "pdu conversion failed") ;
      return 0L ;
    }
    if( env->ExceptionCheck() )
	return 0L ;

    len = 0 ;
    n = 0 ;
    for( var = pdu->variables ; var != 0L && n < MAX_VARS ; var = var->next_variable, n++ ) {
      oid_buffers[n] = (u_char *)malloc(1) ;
      buflen = 1 ;
      oid_outlens[n] = 0 ;
      sprint_realloc_objid(&oid_buffers[n], &buflen, &oid_outlens[n], TRUE, var->name, var->name_length) ;
      len += oid_outlens[n] ;

      var_buffers[n] = (u_char *)malloc(1) ;
      buflen = 1 ;
      var_outlens[n] = 0 ;
      sprint_realloc_value(&var_buffers[n], &buflen, &var_outlens[n], TRUE, var->name, var->name_length, var) ;
      len += var_outlens[n] ;

      len += new_line_len + 1 ;
    } 

    final_buffer = (char *)malloc(len+1) ;
    offset = 0 ;
    for( i = 0 ; i < n ; i++ ) {
      memcpy(final_buffer+offset, oid_buffers[i], oid_outlens[i]) ;
      offset += oid_outlens[i] ;

      memcpy(final_buffer+offset," ", 1) ;
      offset += new_line_len ;

      memcpy(final_buffer+offset, var_buffers[i], var_outlens[i]) ;
      offset += var_outlens[i] ;

      if( i != (n-1) ) {
	memcpy(final_buffer+offset, new_line, new_line_len) ;
	offset += new_line_len ;
      }

      free(oid_buffers[i]) ;
      free(var_buffers[i]) ;
    }
    final_buffer[offset] = 0 ; // null terminate
  
    jStr = env->NewStringUTF(final_buffer) ;
    free(final_buffer) ;

    snmp_free_pdu(pdu);

    return jStr ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return 0L ;
  }
  catch( jthrowable je) {
    return 0L ;
  }
  catch( JNIProxyException e ) {
    fprintf(stderr, "caught JNIProxyException during string conversion\n") ;
    return 0L;
  }
  catch( ... ) {
    fprintf(stderr, "caught unknown c++ exception during send\n") ;
    return 0L ;
  }

  return 0L ;
  
}

JNIEXPORT jstring JNICALL Java_org_netsnmp_PDU_errString(JNIEnv *env, jobject jpdu)
{
  char buffer[128] ;
  try {
    int errStatus = env->GetIntField(jpdu, PDUErrorStatus) ;
    int errIndex = env->GetIntField(jpdu, PDUErrorIndex) ;

    snprintf(buffer, sizeof(buffer), "%s idx=%d", snmp_errstring(errStatus), errIndex) ;
    return env->NewStringUTF(buffer) ;
  }
  catch( Throwable& JE ) {
    env->Throw(JE.je) ;
    return 0L ;
  }
  catch( jthrowable je) {
    return 0L ;
  }
  return 0L ;
}
