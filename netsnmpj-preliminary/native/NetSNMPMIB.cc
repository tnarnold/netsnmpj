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

#include "org_netsnmp_MIB.h"
#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>
#include <net-snmp/mib_api.h>
#include <net-snmp/library/snmp_client.h>
#include <net-snmp/library/mib.h>
#include <net-snmp/library/default_store.h>
#include <assert.h>
#include <jni_proxy.hh>
#include <joid_proxy.hh>
#include <JNISupport.hh>
#include "NetSNMPjSessionStruct.hh"


extern "C" {
	void snmp_disable_stderrlog() ;
}

#include <JNISupport.hh>

#if !defined(__GNUC__) || defined(_WIN32)
extern ProxyList JNIClassProxies ;
extern ProxyList JNIIDProxies ;
#endif


extern jclass_proxy OIDClass, DefaultOIDClass ;
extern jmethodID_proxy oidsMethod ;
jmethodID_proxy oidToStringMethod(OIDClass, "toString", "()Ljava/lang/String;") ;
extern jmethodID_proxy DefaultOIDCtor ;

jclass_proxy leaf_class("org/netsnmp/MIB$leaf") ;
jmethodID_proxy leaf_ctor(leaf_class, "<init>", "(ILjava/lang/String;Z)V") ;
jclass_proxy arrays_class("java/util/Arrays") ;
jstaticmethodID_proxy sort_method(arrays_class, "sort", "([Ljava/lang/Object;)V") ;

jobject new_leaf(JNIEnv *env, int id, const char *label, bool hasChildren)
{
  jstring jstr = env->NewStringUTF(label) ;
  return env->NewObject(leaf_class, leaf_ctor, id, jstr, hasChildren) ;
}

jobjectArray new_peer_leaves(JNIEnv *env, struct tree *theTree)
{
  int n, i ;
  struct tree *t ;
  jobject leaf ;
  jobjectArray leaves ;

  /* count */
  for( n = 0, t = theTree ; t ; t = t->next_peer )
    n++ ;

  leaves = env->NewObjectArray(n, leaf_class, NULL) ;
  if( leaves == NULL )
    return NULL ; // exception's on the way
  for( i = 0, t = theTree ; i < n ; i++, t = t->next_peer ) {

    leaf = new_leaf(env, t->subid, t->label, t->child_list != 0L) ;
    if( leaf == NULL )
      return NULL ;

    env->SetObjectArrayElement(leaves, i, leaf) ;
  }
  
  env->CallStaticVoidMethod(arrays_class, sort_method, leaves) ;

  return leaves ;
  
}

/*
 * Class:     org_netsnmp_MIB
 * Method:    treeHead
 * Signature: ()Lsnmp/MIB$leaf;
 */
JNIEXPORT jobjectArray JNICALL Java_org_netsnmp_MIB_treeHead
  (JNIEnv *env, jclass clazz)
{
  return new_peer_leaves(env, get_tree_head()) ;
}


/*
 * Class:     org_netsnmp_MIB
 * Method:    findLeaves
 * Signature: ([I)[Lsnmp/MIB$leaf;
 */
JNIEXPORT jobjectArray JNICALL Java_org_netsnmp_MIB_findLeaves
  (JNIEnv *env, jclass, jobject oid_j)
{
  int n_oids, i ;
  oid id ;
  jint *oidPtr ;
  jintArray joidsArray ;
  struct tree *t, *head = get_tree_head()  ;
  jstring jstr ;

  joidsArray = (jintArray)env->CallObjectMethod(oid_j, oidsMethod) ;
  if( joidsArray == NULL || env->ExceptionCheck() )
      return 0L ;
 
  n_oids = env->GetArrayLength(joidsArray) ;

  oidPtr = env->GetIntArrayElements(joidsArray, NULL) ;

  for( i = 0 ; i < n_oids && head ; i++ ) {
    id = oidPtr[i] ;
   
    for( t = head, head = 0L ; t ; t = t->next_peer ) {
      if( t->subid == id ) {
	head = t->child_list ;
	break ;
      }
    } // for
    

  } // for

  /*
   * release the oid ptr
   */
  env->ReleaseIntArrayElements(joidsArray, oidPtr, 0) ;

  if( head == 0L ) {
    jstr = (jstring)env->CallObjectMethod(oid_j, oidToStringMethod) ;
    if( jstr == 0L || env->ExceptionCheck() )
      return 0L ;
    throwNotFound(env, jstr) ;
    return 0L ;
  }
  return new_peer_leaves(env, head) ;
}


void throwNotFound(JNIEnv *env, jstring descr)
{
  jclass exceptionClass ;
  jmethodID mid ;
  jthrowable excp ;
  jint err ;

  exceptionClass = env->FindClass("org/netsnmp/MIBItemNotFound") ;
  if( exceptionClass == NULL  ) 
    env->FatalError("could not find class for MIBItemNotFound") ;

  mid = env->GetMethodID(exceptionClass, "<init>", "(Ljava/lang/String;)V") ;
  if( mid == 0 ) 
    env->FatalError("could not find constructor for MIBItemNotFound") ;
  excp = (jthrowable)env->NewObject(exceptionClass, mid, descr) ;

  err = env->Throw(excp) ;
  if( err != 0 ) 
    env->FatalError("could not throw MIBItemNotFound") ;

}

void throwFileNotFound(JNIEnv *env, const char *fName)
{
  jclass exceptionClass ;

  exceptionClass = env->FindClass("java/io/FileNotFoundException") ;
  if( exceptionClass == NULL  ) 
    env->FatalError("could not find class for FileNotFoundException") ;

  env->ThrowNew(exceptionClass, fName) ;


}

JNIEXPORT jobject JNICALL Java_org_netsnmp_MIB_readObjID
  (JNIEnv *env, jclass, jstring sysDescrJStr)
{
  int err ;
  size_t len ;
  oid oid_data[512] ; // should be sufficient for any reasonable application
  const char *sysDescrStr = env->GetStringUTFChars(sysDescrJStr, 0L) ;
  jintArray oidArray ;

  len = sizeof(oid_data)/sizeof(oid) ;
  err = read_objid(sysDescrStr, oid_data, &len) ;
  if( err == 0 ) {
    /*
     * lookup of the descriptor failed
     */
    throwNotFound(env, sysDescrJStr) ;
    return NULL ;
  }

  /*
   * Lookup succeeded
   */
  oidArray = env->NewIntArray(len) ;
  if( oidArray == 0L )
    env->FatalError("could not allocation intarray for oid") ;

  env->SetIntArrayRegion(oidArray, 0, (int)len, (jint *)oid_data) ;

  env->ReleaseStringUTFChars(sysDescrJStr, sysDescrStr) ; /* free up the string */

  return env->NewObject(DefaultOIDClass, DefaultOIDCtor, oidArray) ;
}

JNIEXPORT jint JNICALL Java_org_netsnmp_MIB_addMIBDir
  (JNIEnv *env, jclass, jstring jdirName)
{
  const char *dirStr ;
  int result ;

  dirStr = env->GetStringUTFChars(jdirName, 0L) ;
  if( dirStr == 0L )
    return -1 ; // exception is being thrown

  result = add_mibdir(dirStr) ;

  env->ReleaseStringUTFChars(jdirName, dirStr) ;

  return result ;
}

JNIEXPORT void JNICALL Java_org_netsnmp_MIB_readModule
  (JNIEnv *env, jclass, jstring jModuleName)
{
  const char *moduleStr ;
  struct tree *t ;

  moduleStr = env->GetStringUTFChars(jModuleName, 0L) ;
  if( moduleStr == 0L )
    return ;

  t = read_module(moduleStr) ;
 
  if( t == NULL ) 
    throwFileNotFound(env, moduleStr) ;

  env->ReleaseStringUTFChars(jModuleName, moduleStr) ;
 
}

JNIEXPORT void JNICALL Java_org_netsnmp_MIB_readMIB
  (JNIEnv *env, jclass, jstring jfileName)
{
  const char *fileName ;
  struct tree *t ;
  int oldWarningLevel ;

  fileName = env->GetStringUTFChars(jfileName, 0L) ;

  oldWarningLevel =  netsnmp_ds_get_int(NETSNMP_DS_LIBRARY_ID, 
		       NETSNMP_DS_LIB_MIB_WARNINGS);

  netsnmp_ds_set_int(NETSNMP_DS_LIBRARY_ID, 
		     NETSNMP_DS_LIB_MIB_WARNINGS, 0);
  t = read_mib(fileName) ;
  netsnmp_ds_set_int(NETSNMP_DS_LIBRARY_ID, 
		     NETSNMP_DS_LIB_MIB_WARNINGS, oldWarningLevel);


  if( t == NULL ) 
    throwFileNotFound(env, fileName) ;

  env->ReleaseStringUTFChars(jfileName, fileName) ;  
}



/*
 * $Log: NetSNMPMIB.cc,v $
 * Revision 1.14  2003/06/01 16:03:49  aepage
 * merget from release-0.2.1
 *
 * Revision 1.12.2.2  2003/05/21 01:12:45  aepage
 * fix for solaris compiler
 *
 * Revision 1.12.2.1  2003/05/21 00:47:24  aepage
 * fixes to absorb MIB functions into the main library in order to
 * support RH7.3 and perhaps other flavors as well.
 *
 * Revision 1.12  2003/05/03 23:37:30  aepage
 * changes to support agentX subagents
 *
 * Revision 1.11  2003/04/29 12:21:17  aepage
 * changing of addMibDir to addMIBDir for consistency.
 *
 * Revision 1.10  2003/04/27 11:41:31  aepage
 * extern definition needed for other platforms as well
 *
 * Revision 1.9  2003/04/25 23:05:21  aepage
 * fixes from win32 compilation
 *
 * Revision 1.8  2003/04/12 00:42:45  aepage
 * fixes from -Wall from g++
 *
 * Revision 1.7  2003/03/31 17:43:58  aepage
 * fixes for shared lib issues
 *
 * Revision 1.6  2003/03/30 22:02:30  aepage
 * fixes for win32
 *
 * Revision 1.5  2003/03/29 23:19:42  aepage
 * continuation of fixes for static initializers and shared libraries.
 * Next stop win32.
 *
 * Revision 1.4  2003/03/29 18:12:12  aepage
 * Fix to the bedamned issue of static constructors between SUNpro C++
 * and gnu g++.  The trick(for SunPRO) seems to have the global list
 * instantiated in the main body of the code rather than the support
 * library.  It remains to be seen if this will hold up for g++, but at
 * least, the link order seems to remain the same.
 *
 * Revision 1.3  2003/03/21 15:08:54  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.2  2003/03/17 19:24:40  aepage
 * added check for successful init of proxies
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
