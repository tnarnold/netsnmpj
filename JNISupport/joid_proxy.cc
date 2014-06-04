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
#include <jni.h>
#include "jni_proxy.hh"
#include "joid_proxy.hh"

extern jclass_proxy DefaultOIDClass, nullPointerExceptionClass ;
extern jmethodID_proxy oidsMethod, nullPointerExceptionCtor ; // duplicate, but we may need that
extern jmethodID_proxy DefaultOIDCtor ;

joid_proxy::joid_proxy(JNIEnv *_env)
  : env(_env), joidsArray(env) 
{

}

joid_proxy::joid_proxy(JNIEnv *_env, jobject obj, jfieldID field)
  : isLocal(false), joidsArray(env)
{
  joid = env->GetObjectField(obj, field) ;
  if( !obj || !joid ) {
    jthrowable je = (jthrowable)env->NewObject(nullPointerExceptionClass, nullPointerExceptionCtor) ;
    env->Throw(je) ; // throws the java exception
    throw je ;
  }

  joidsArray = (jintArray)env->CallObjectMethod(joid, oidsMethod) ;
  if( env->ExceptionCheck() )
    return ;
 
}

joid_proxy::joid_proxy(JNIEnv *_env, jobject _joid)
  : isLocal(false), env(_env), joidsArray(env), joid(_joid)
{
  if( !_joid ) {
    jthrowable je = (jthrowable)env->NewObject(nullPointerExceptionClass, nullPointerExceptionCtor) ;
    env->Throw(je) ; // throws the java exception
    throw je ;
  }


  joidsArray = (jintArray)env->CallObjectMethod(_joid, oidsMethod) ;
  JEXCEPTION_CHECK(env) ;
}

joid_proxy::joid_proxy(JNIEnv *_env, int *oids, size_t len)
  : isLocal(true), env(_env), joidsArray(env, oids, len)
{
  joid = env->NewObject(DefaultOIDClass, DefaultOIDCtor, (jobject)joidsArray) ;
  JEXCEPTION_CHECK(env) ;
}

void joid_proxy::setOids(JNIEnv *env, int *oids, size_t len)
{

  joidsArray.setInts(env, oids, len) ;
  
  
  joid = env->NewObject(DefaultOIDClass, DefaultOIDCtor, (jobject)joidsArray) ;
}

int *joid_proxy::dup()
{
  if( (int *)joidsArray == 0L )
    return 0L ;

  return joidsArray.dup() ;
}

joid_proxy& joid_proxy::operator=(const jobject& _joid)
{
  if( _joid != 0L )
    joidsArray = (jintArray)env->CallObjectMethod(_joid, oidsMethod) ;
  else
    joidsArray = 0L ;
  joid = _joid ;
  return *this ;
}

joid_proxy::~joid_proxy()
{
}
