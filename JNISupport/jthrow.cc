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

#include "jni_proxy.hh"
#include "jthrow.hh"
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <assert.h>
#include <stdio.h>
#include "JNISupport.hh"

#ifdef _WIN32
#define vsnprintf _vsnprintf
#endif

extern jclass_proxy ioExceptionClass ;

void ThrowIOException(JNIEnv *env, int eErrno, const char *fmt, ...)
{
  char buffer[128] ;
  char suffix_buffer[128] ;
  int len ;
  va_list ap ;
  const char *errMsg ;
#ifndef WIN32
  errMsg = (const char *)strerror(eErrno) ;
#else
  void *lpMsgBuf ;
    FormatMessage( 
    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
    FORMAT_MESSAGE_FROM_SYSTEM | 
    FORMAT_MESSAGE_IGNORE_INSERTS,
    NULL,
    eErrno == 0 ? GetLastError() : eErrno,
    MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
    (LPTSTR) &lpMsgBuf,
    0,
    NULL); 
    errMsg = (const char *)lpMsgBuf ;
#endif

  
  if( errMsg == 0L )
    errMsg = "null" ;

  va_start(ap, fmt) ;
  
  vsnprintf(suffix_buffer, sizeof(suffix_buffer), fmt, ap) ;

  va_end(ap) ;

  len = snprintf(buffer, sizeof(buffer), "%s(%d) %s", errMsg, eErrno, suffix_buffer) ;
  env->ThrowNew(ioExceptionClass, buffer) ;

#ifdef WIN32
  LocalFree(lpMsgBuf) ;
#endif

}

jthrowable newThrowable(JNIEnv *env, jclass clazz, const char *fmt, ...)
{
  char buffer[256] ;
  va_list ap ;
  jthrowable je ;
  jmethodID mid = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;)V") ;

  va_start(ap, fmt) ;
  
  vsnprintf(buffer, sizeof(buffer), fmt, ap) ;

  va_end(ap) ;

  je = (jthrowable)env->NewObject(clazz, mid,
				  env->NewStringUTF(buffer)) ;

  JEXCEPTION_CHECK(env) ; // in case the constructor chokes
  assert(je) ;
  return je ;
}

Throwable::Throwable(jthrowable _je)
  : je(_je)
{
  
}
