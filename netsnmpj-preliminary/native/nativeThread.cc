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

#define strcasecmp _stricmp
#endif



#include "org_netsnmp_NetSNMPSession.h"
#include "NetSNMPjSessionStruct.hh"
#include <jni_proxy.hh>
#include <JNISupport.hh>
#include "jthrow.hh"

#include <assert.h>
#include <string.h>
#include <time.h>
#include <signal.h>
#include <stdarg.h>
#include <limits.h>


#include <stdlib.h>
#ifndef _WIN32
#include <pthread.h>
#else
#endif

struct nativeThreadData {
  JavaVM *jvm ;
#ifndef _WIN32
  pthread_t thr ;
#else
  HANDLE thr ;
#endif
  bool attachDaemon ; // jvms < 1.4.1 under linux can't Attach as a daemon for jvm

} ;

extern jmethodID_proxy NotifyMethod ;
extern jobject_static_field_proxy readLockField ;

extern jclass_proxy ThreadClass ;
extern jstaticmethodID_proxy getCurrentThread ;
extern jmethodID_proxy setThreadName ;
extern jmethodID_proxy PrintStackTrace_method ;
extern jmethodID_proxy println, toString_method ;
extern jobject_static_field_proxy SysErr ;
extern "C" {
#ifndef _WIN32
  void *netsnmpj_nativeThread(void *arg) ;
#else
  DWORD WINAPI netsnmpj_nativeThread(LPVOID *arg) ;
#endif
}

#ifndef _WIN32
void *netsnmpj_nativeThread(void *arg)
#else
DWORD WINAPI netsnmpj_nativeThread(LPVOID arg) 
#endif
{
  struct nativeThreadData *ntData = (struct nativeThreadData*)arg ;
  JNIEnv *env ;
  int result ;
  JavaVM *jvm = ntData->jvm ;
  jstring threadName ;
  jobject currentThread ;

  /*
   * note we use stderr and exit here since the AttachCurrentThread seems
   * to have failed, thus the env variable may be unreliable.  
   */

  if( ntData->attachDaemon )
    result = jvm->AttachCurrentThreadAsDaemon((void **)&env, 0L) ;
  else 
    result = jvm->AttachCurrentThread((void **)&env, 0L) ;

  if( result != 0 ) {
    fprintf(stderr, "AttachCurrentThreadAsDaemon failed: could not start reading thread") ;
    exit(2) ;
    return 0L ;
  }

  currentThread = env->CallStaticObjectMethod(ThreadClass, getCurrentThread) ;
  if( currentThread == 0L || env->ExceptionCheck() ) {
    env->ExceptionDescribe() ;
    env->FatalError("currentThread failed: could not start reading thread") ;
    return 0L ;
  }

  threadName = env->NewStringUTF("NetSNMP") ;
  if( threadName == 0L ) {
    env->ExceptionDescribe() ;
    env->FatalError("creating thread name failed: could not start reading thread") ;
    return 0L ;
  }

  env->CallVoidMethod(currentThread, setThreadName, threadName) ;
  if( env->ExceptionCheck() ) {
    env->ExceptionDescribe() ;
    env->FatalError("setting thread name failed: could not start reading thread") ;
    return 0L ;
  }
  
  // release the memory allocated for the thread name
  // perhaps over-the-top since these instructions may well exceed the length
  // of the name, but it behooves us to be in the habit of being tidy
  env->DeleteLocalRef(threadName) ;
  if( env->ExceptionCheck() ) {
    env->ExceptionDescribe() ;
    env->FatalError("releasing thread name failed: could not start reading thread") ;
    return 0L ;
  }
  

  free(ntData) ;

  env->MonitorEnter(readLockField) ;
  if( env->ExceptionCheck() ) {
    env->ExceptionDescribe() ;
    env->FatalError("MonitorEnter failed: could not start reading thread") ;
    return 0L ;
  }

  /*
   * notify that we've started
   */
  env->CallVoidMethod(readLockField, NotifyMethod) ;
  if( env->ExceptionCheck() ) {
    env->ExceptionDescribe() ;
    env->FatalError("notify failed:  Could not start reading thread") ;
    return 0L ;
  }

  /*
   * release the lock
   */
  env->MonitorExit(readLockField) ;
  if( env->ExceptionCheck() ) {
    env->ExceptionDescribe() ;
    env->FatalError("MontiorExit failed:  Could not start reading thread") ;
  }

  while( 1 ) { 
    if( env->ExceptionCheck() )
      env->ExceptionDescribe() ;
   
    try {
      internalRead(env, 0L, 1e9, 1024*1024) ;
    }
    catch( Throwable e ) {
      env->Throw(e.je) ; // caught and checked below
    }
    catch( jthrowable e ) {
      // we largely expect this and take care of it below
#ifdef WIN32
//      env->CallVoidMethod(e, PrintStackTrace_method) ;
#endif
    }
    if( env->ExceptionCheck() ) {
      env->ExceptionDescribe() ; // clears exception
    }
    
  }

  jvm->DetachCurrentThread() ;

  return 0L ;
}

void nativeThreadStart(JNIEnv *env)
{
  struct nativeThreadData *ntData = (nativeThreadData *)calloc(sizeof(nativeThreadData), 1) ;
  int err ;
  JavaVM *jvm ;
  bool isLinux ;

  err = env->GetJavaVM(&jvm) ;
  if( err != 0 ) 
    env->FatalError("could not get vm") ;

  jproperty_proxy vmVersion(env, "java.vm.version") ;
  jproperty_proxy osName(env, "os.name") ;

  if( strcasecmp(osName, "linux") == 0 )
    isLinux = true ;
  else
    isLinux = false ;

  if( strcmp(vmVersion, "1.4") > 0 )
    ntData->attachDaemon = true ;
  else
    ntData->attachDaemon = false ;

  assert(jvm) ;
  ntData->jvm = jvm ;
#ifndef _WIN32 /* UNIX */
  err = pthread_create(&ntData->thr, 0L, netsnmpj_nativeThread, ntData) ;
  if( err != 0 ) 
    env->FatalError("Could not start reading thread") ;
  return ;
#else
  ntData->thr = CreateThread(0L, 0, netsnmpj_nativeThread, ntData, 0, 0L) ;
  if( ntData->thr  == NULL )
    env->FatalError("Could not start windows reading thread") ;
  return ;
#endif
}
