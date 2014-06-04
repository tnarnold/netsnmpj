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
#include <stdarg.h>
#include <stdlib.h>
#include <stdio.h>
#include "jni_proxy.hh"
#if !defined(_WIN32) && !defined(DARWIN)
#include <alloca.h>
#else
#ifdef _WIN32
#include <malloc.h>
#define vsnprintf _vsnprintf
#endif
#endif

extern jclass_proxy System ;

extern jclass_proxy PrintStream ;
jmethodID_proxy printMethod(PrintStream, "print", "(Ljava/lang/String;)V") ;

jobject_static_field_proxy SystemErr(System, "err", "Ljava/io/PrintStream;") ;
jobject_static_field_proxy SystemOut(System, "out", "Ljava/io/PrintStream;") ;


#define BUFFER_SIZE 256
#define MAX_BUFFER_SIZE (1024*1024)

/**
 * @returns -1 if buffersize was insuffcient -2 if an exception was thrown
 */
static int core_stream_vsprintf(JNIEnv *env, int bufferSize, jobject theStream, const char *fmt, va_list ap)
{
  char *buffer = (char *)alloca(bufferSize) ;
  int len ;
  jstring jstr ;

  len = vsnprintf(buffer, bufferSize, fmt, ap) ;
  if( len == bufferSize )
    return -1 ;
 
  jstr = env->NewStringUTF(buffer) ;

  env->CallVoidMethod(theStream, printMethod, jstr) ;
  if( env->ExceptionCheck() )
    return -2 ;

  return len ;
}

static int stream_vsprintf(JNIEnv *env, jobject theStream, const char *fmt, va_list ap)
{
  int sz, len ;

  for( sz = BUFFER_SIZE ; sz <= MAX_BUFFER_SIZE ; sz *= 4 ) {

    len = core_stream_vsprintf(env, sz, theStream, fmt, ap) ;
    if( len == -2 )
      return len ;

    if( len >= 0 )
      return len ;
  }
  return -1 ;
}

int stream_printf(JNIEnv *env, jobject theStream, const char *fmt, ...)
{
  va_list ap ;
  int len ;

  va_start(ap, fmt) ;

  len = stream_vsprintf(env, theStream, fmt, ap) ;

  va_end(ap) ;
  
  return len ;
}

int sysout_printf(JNIEnv *env, const char *fmt, ...)
{
  va_list ap ;
  int len ;

  va_start(ap, fmt) ;

  len = stream_vsprintf(env, SystemOut, fmt, ap) ;

  va_end(ap) ;
  
  return len ;
}

int syserr_printf(JNIEnv *env, const char *fmt, ...)
{
  va_list ap ;
  int len ;

  va_start(ap, fmt) ;

  len = stream_vsprintf(env, SystemErr, fmt, ap) ;

  va_end(ap) ;
  
  return len ;
}

