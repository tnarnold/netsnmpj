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

#include "org_netsnmp_NativeOID.h"
#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>
#include <net-snmp/mib_api.h>
#include <net-snmp/library/snmp_client.h>
#include <assert.h>
#include <jni_proxy.hh>
#include <joid_proxy.hh>
#include "NetSNMPjSessionStruct.hh"
#include "JNISupport.hh"


extern jmethodID_proxy oidsMethod ;

JNIEXPORT jstring JNICALL Java_org_netsnmp_NativeOID_toText
  (JNIEnv *env, jobject joid)
{
  char buffer[1024] ;
  jintArray joidsArray ;
  jsize n_oids ;
  int *oidPtr ;


  // oids field within oid field

  joidsArray = (jintArray)env->CallObjectMethod(joid, oidsMethod) ;
  if( joidsArray == 0L || env->ExceptionCheck() )
    return 0L ;
  
  // convert oids to a native int array
  
  n_oids = env->GetArrayLength(joidsArray) ;
  oidPtr = (int *)env->GetIntArrayElements(joidsArray, NULL) ;
  if( !oidPtr )
    return 0L ;
  
  // convert oids to a native int array

  n_oids = env->GetArrayLength(joidsArray) ;
  oidPtr = (int *)env->GetIntArrayElements(joidsArray, NULL) ;
  assert(oidPtr) ;

  snprint_objid(buffer, sizeof(buffer), (const oid *)oidPtr, n_oids) ;
 
  /*
   * release the oid ptr
   */
  env->ReleaseIntArrayElements(joidsArray, (jint *)oidPtr, 0) ;

  return env->NewStringUTF(buffer) ;

}


/*
 * Class:     org_netsnmp_NativeOID
 * Method:    getASNType
 * Signature: ()Lorg/netsnmp/ASN_TYPE;
 */
JNIEXPORT jobject JNICALL Java_org_netsnmp_NativeOID_getASNType
  (JNIEnv *env, jobject joid)
{
  try {
    int asnType ;
    joid_proxy oidProxy(env, joid) ;
    struct tree *t = get_tree(oidProxy, oidProxy.getLen(), get_tree_head()) ;
    if( !t )
      return 0L ;

    asnType = mib_to_asn_type(t->type) ;
    
    return ClassifyASNType(env, asnType) ;
  }
  catch( jthrowable je ) {
    return 0L ;
  }
  catch( Throwable JE ) {
    env->Throw(JE.je) ;
    return 0L ;
  }
}


/*
 * $Log: NetSNMPOID.cc,v $
 * Revision 1.5  2003/06/01 15:25:20  aepage
 * addtional functionality to determine type
 *
 * Revision 1.4  2003/04/25 23:05:20  aepage
 * fixes from win32 compilation
 *
 * Revision 1.3  2003/04/12 00:43:05  aepage
 * fixes from -Wall from g++
 *
 * Revision 1.2  2003/03/21 15:08:54  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
