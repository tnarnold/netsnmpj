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
#ifdef WIN32
#include <windows.h>
#endif

#include "jni_proxy.hh"
#include "JNISupport.hh"
#include <assert.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>


#if defined(__GNUC__) && !defined(_WIN32)
ProxyList JNIClassProxies ;
ProxyList JNIIDProxies ;
#else
extern ProxyList JNIClassProxies ;
extern ProxyList JNIIDProxies ;
#endif

jclass_proxy System("java/lang/System") ;
jclass_proxy ThrowableClass("java/lang/Throwable") ;


jmethodID_proxy PrintStackTrace_method(ThrowableClass, "printStackTrace", "()V") ;
jstaticmethodID_proxy SysGetProperty(System, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;") ;
jstaticmethodID_proxy SysGetPropertyDef(System, "getProperty", 
				  "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;") ;


jobject_static_field_proxy SysErr(System, "err", "Ljava/io/PrintStream;") ;

jclass_proxy PrintStream("java/io/PrintStream") ;
jmethodID_proxy println(PrintStream, "println", "(Ljava/lang/Object;)V") ;

jclass_proxy ObjectClass("java/lang/Object") ;
jmethodID_proxy NotifyAllMethod(ObjectClass, "notifyAll", "()V") ;
jmethodID_proxy NotifyMethod(ObjectClass, "notify", "()V") ;
jmethodID_proxy WaitMethod(ObjectClass, "wait", "()V") ;
jmethodID_proxy toString_method(ObjectClass, "toString", "()Ljava/lang/String;") ;

jclass_proxy ThreadClass("java/lang/Thread") ;
jstaticmethodID_proxy getCurrentThread(ThreadClass, "currentThread", "()Ljava/lang/Thread;") ;
jmethodID_proxy setThreadName(ThreadClass, "setName", "(Ljava/lang/String;)V") ;

jclass_proxy illegalStateExceptionClass("java/lang/IllegalStateException") ;
jmethodID_proxy illegalStateExceptionCtor(illegalStateExceptionClass, "<init>", "(Ljava/lang/String;)V") ;
jclass_proxy nullPointerExceptionClass("java/lang/NullPointerException") ;
jmethodID_proxy nullPointerExceptionCtor(nullPointerExceptionClass, "<init>", "()V") ;
jclass_proxy ioExceptionClass("java/io/IOException") ;
jclass_proxy classClass("java/lang/Class") ;

jmethodID_proxy isInstance(classClass, "isInstance", "(Ljava/lang/Object;)Z") ;

static JavaVM *jvm ;


jobject_static_field_proxy::jobject_static_field_proxy(const jclass_proxy& jc, const char *fName, const char *descriptor) 
  : clazz(jc), fieldName(fName), fieldSignature(descriptor)
{
  extern ProxyList JNIIDProxies ;
  JNIIDProxies.add(this) ;
}

jstaticmethodID_proxy::jstaticmethodID_proxy(const jclass_proxy& jc, const char *mname, const char *mSig)
  : clazz(jc), methodName(mname), methodSignature(mSig)
{
  extern ProxyList JNIIDProxies ;
  JNIIDProxies.add(this) ;
}

jmethodID_proxy::jmethodID_proxy(const jclass_proxy& jc, const char *mname, const char *mSig)
  : clazz(jc), methodName(mname), methodSignature(mSig)
{
  extern ProxyList JNIIDProxies ;
  JNIIDProxies.add(this) ;
}

jstaticFieldID_proxy::jstaticFieldID_proxy(const jclass_proxy& jc, const char *fName, const char *fieldSig)
  : clazz(jc), fieldName(fName), fieldSignature(fieldSig)
{
  extern ProxyList JNIIDProxies ;
  JNIIDProxies.add(this) ;
}

jstaticFieldID_proxy::jstaticFieldID_proxy(JNIEnv *_env, const jclass_proxy& jc, const char *fName, const char *fieldSig)
  : jni_proxy(_env), clazz(jc), fieldName(fName), fieldSignature(fieldSig)
{
  init(_env) ;
  JEXCEPTION_CHECK(_env) ;
}


jclass_proxy::jclass_proxy(const char *cname)
  : classname(cname)
{
  extern ProxyList JNIClassProxies ;
  JNIClassProxies.add(this) ;
}

jfieldID_proxy::jfieldID_proxy(const jclass_proxy& jc, const char *fName, const char *fieldSig)
  : clazz(jc), fieldName(fName), fieldSignature(fieldSig)
{
  extern ProxyList JNIIDProxies ;
  JNIIDProxies.add(this) ;
}

bool jclass_proxy::isa(JNIEnv *env, jobject obj) 
{
  return env->CallBooleanMethod((jobject)clazz, (jmethodID)isInstance, obj) ;
}

JNIEnv *GetJNIEnv()
{
  JNIEnv *env ;

  jvm->AttachCurrentThread((void **)&env, 0L) ;
  return env ;
}

/**
 * throw a null pointer exception if the object is null
 */
void ThrowIfNull(JNIEnv *env, jobject jobj) 
{
  if( jobj != 0L )
    return ;
  jthrowable je = (jthrowable)env->NewObject(nullPointerExceptionClass, nullPointerExceptionCtor) ;
  JEXCEPTION_CHECK(env) ; // some other exception
  throw Throwable(je) ;
}

void CheckException(JNIEnv *env)
{
  jthrowable excep = env->ExceptionOccurred() ;
  if( !excep )
    return ;
  env->ExceptionDescribe() ;
}

void notifyAll(JNIEnv *env, jobject obj) 
{
  env->CallVoidMethod(obj, NotifyAllMethod) ;
}

bool ProxyList::InitProxies(JNIEnv *env)
{
  jni_proxy *p ;
  int debug_flag = 0, i = 0 ;

  if( env->GetJavaVM(&jvm) != 0 )
    env->FatalError("GetJavaVM failed") ;

  if( getenv("JNIPROXY_DEBUG") && atoi(getenv("JNIPROXY_DEBUG")) )
    debug_flag = 1 ;

  for( p = head ; p ; p = p->next ) {
    if( debug_flag )
      fprintf(stderr, "%2d initializing proxy %s", i++, (const char *)*p) ;
    if( !p->init(env) ) {
      if( debug_flag )
	fprintf(stderr, " failed\n") ;
      return false ;
    }
    if( debug_flag )
      fprintf(stderr, " success\n") ;
  }
  return true ;
}

bool ProxyList::RemoveProxies(JNIEnv *env)
{
  jni_proxy *p ;

  for( p = head ; p ; p = p->next )
    if( !p->close(env) )
      return false ;
  return true ;
}

bool InitJNIProxies(JNIEnv *env)
{
  int debug_flag = 0 ;
  if( getenv("JNIPROXY_DEBUG") && atoi(getenv("JNIPROXY_DEBUG")) )
    debug_flag = 1 ;

  if( debug_flag ) {
    fprintf(stderr, "##\n## %d class proxies %d id proxies\n##\n",
	    JNIClassProxies.n, JNIIDProxies.n) ;
  }

  if( !JNIClassProxies.InitProxies(env) )
    return false ;
  if( !JNIIDProxies.InitProxies(env) )
    return false ;

  if( debug_flag ) {
    fprintf(stderr, "##\n## success\n##\n") ;
  }

  return true ;
}

bool DeleteJNIProxies(JNIEnv *env)
{
  if( !JNIClassProxies.RemoveProxies(env) )
    return false ;
  if( !JNIIDProxies.RemoveProxies(env) )
    return false ;

  return true ;
}

bool jni_proxy::close(JNIEnv *env)
{
  return true ; // successful
}


jni_proxy::jni_proxy(JNIEnv *env)
  : isLocal(true)
{
  
}

jni_proxy::jni_proxy()
  : isLocal(false)
{
  next = 0L ;
}

jni_proxy::~jni_proxy()
{
}

jclass_proxy::jclass_proxy(JNIEnv *env, const char *cname)
  : jni_proxy(env), classname(cname)
{
  init(env) ;
  JEXCEPTION_CHECK(env) ;
}

jclass_proxy::~jclass_proxy()
{
  JNIClassProxies.remove(this) ;
}

const char *jclass_proxy::toString() const
{
  return classname ;
}

jfieldID_proxy::~jfieldID_proxy()
{
  JNIIDProxies.remove(this) ;
}

const char *jfieldID_proxy::toString() const
{
  return fieldName ;
}

jstaticFieldID_proxy::~jstaticFieldID_proxy()
{
  JNIIDProxies.remove(this) ;
}

const char *jstaticFieldID_proxy::toString() const
{
  return fieldName ;
}

jmethodID_proxy::~jmethodID_proxy()
{
  JNIIDProxies.remove(this) ;
}

const char *jmethodID_proxy::toString() const 
{
  return methodName ;
}

/*
 * Static Method
 */


jstaticmethodID_proxy::~jstaticmethodID_proxy()
{
  JNIIDProxies.remove(this) ;
}

const char *jstaticmethodID_proxy::toString() const 
{
  return methodName ;
}


const char *jobject_static_field_proxy::toString() const 
{
  return fieldName ;
}

jobject_static_field_proxy::~jobject_static_field_proxy()
{
  JNIIDProxies.remove(this) ;
}


bool jclass_proxy::init(JNIEnv *env)
{
  clazz = env->FindClass(classname) ;
  if( !clazz )
    return false ;
  if( isLocal )
    return true ;

  /*
   * Exception is to be used persistantly
   */
  clazz = (jclass)env->NewGlobalRef((jobject)clazz) ;
  if( !clazz )
    return false ;
  return true ;
}

bool jclass_proxy::close(JNIEnv *env)
{
  if( !isLocal )
    env->DeleteGlobalRef((jobject)clazz) ;
  return true ; // success
}

bool jfieldID_proxy::init(JNIEnv *env)
{
  fid = env->GetFieldID(clazz, fieldName, fieldSignature) ;
  if( !fid ) 
    return false ;
  return true ;
}


bool jstaticFieldID_proxy::init(JNIEnv *env)
{
  fid = env->GetStaticFieldID(clazz, fieldName, fieldSignature) ;
  if( !fid ) 
    return false ;
  return true ;
}

jmethodID_proxy::jmethodID_proxy(JNIEnv *env, jclass_proxy& jc, const char *mname, const char *mSig)
  : jni_proxy(env), clazz(jc), methodName(mname), methodSignature(mSig)
{
  init(env) ;
  JEXCEPTION_CHECK(env) ;
}

bool jmethodID_proxy::init(JNIEnv *env)
{
  mid = env->GetMethodID(clazz, methodName, methodSignature) ;
  if( !mid ) 
    return false ;
  return true ;
}


bool jstaticmethodID_proxy::init(JNIEnv *env)
{
  mid = env->GetStaticMethodID(clazz, methodName, methodSignature) ;
  if( !mid ) 
    return false ;
  return true ;
}

bool jobject_static_field_proxy::init(JNIEnv *env)
{
  jfieldID fid ;
  jclass target_class = clazz ;

  if( target_class == NULL ) {
    return false ;
  }
  
  fid = env->GetStaticFieldID(target_class, fieldName, fieldSignature) ;
  if( !fid ) 
    return false ;
  
  obj = env->GetStaticObjectField(target_class, fid) ;
  if( !obj ) 
    return false ;

  obj = env->NewGlobalRef(obj) ;
  if( !obj ) 
    return false ;

  return true ;
}

void RemoveClassJNIProxy(jni_proxy *p)
{
  JNIClassProxies.remove(p) ;
}

void RemoveJNIProxy(jni_proxy *p)
{
  JNIIDProxies.remove(p) ;
}

jstring_proxy::jstring_proxy(JNIEnv *_env)
  : isLocal(true), env(_env), jstr(0L), str(0L) 
{

}

jstring_proxy::jstring_proxy(JNIEnv *_env, jstring _jstr)
  : isLocal(false), env(_env)
{
  jthrowable je = 0L ;
  jstr = 0L ;
  str = 0L ;

  if( _jstr == 0L ) {
    je = (jthrowable)env->NewObject(nullPointerExceptionClass, nullPointerExceptionCtor) ;
    throw Throwable(je) ;
  }

  jstr = _jstr ;
  str = env->GetStringUTFChars(jstr, NULL) ;
  JEXCEPTION_CHECK(env) ;
}

jstring_proxy::jstring_proxy(JNIEnv *_env, jobject obj, jfieldID fid)
  : isLocal(false), env(_env)
{
  jstr = (jstring)env->GetObjectField(obj, fid) ;
  str = env->GetStringUTFChars(jstr, NULL) ;
  JEXCEPTION_CHECK(env) ;
}

jstring_proxy::jstring_proxy(JNIEnv *_env, const char *s)
  : isLocal(true), env(_env)
{
  if( s == 0L ) {
    str = 0L ;
    jstr = 0L ;
    return ;
  }
  str = s ;
  jstr = env->NewStringUTF(s) ;
}

jstring_proxy::jstring_proxy(JNIEnv *_env, const char *s, size_t len)
  : isLocal(true), env(_env)
{
  if( s == 0L ) {
    str = 0L ;
    jstr = 0L ;
    return ;
  }
  str = s ;
  jstr = env->NewString((jchar *)s, len) ;
}

jsize jstring_proxy::length()
{
  if( !jstr )
    return 0 ;
  return env->GetStringLength(jstr) ;
}

jstring_proxy& jstring_proxy::operator=(const char *s)
{
  if( jstr && str && !isLocal )
    env->ReleaseStringUTFChars(jstr, str) ;

  isLocal = true ;
  str = s ;
  jstr = env->NewStringUTF(s) ;

  JEXCEPTION_CHECK(env) ;

  return *this ;
}

jstring_proxy& jstring_proxy::operator=(jstring js)
{


  if( jstr && str && !isLocal )
    env->ReleaseStringUTFChars(jstr, str) ;
  isLocal = false ;
  jstr = js ;
  if( jstr == 0L ) {
    str = 0L ;
    return *this ;
  }
  str = env->GetStringUTFChars(jstr, NULL) ;

  JEXCEPTION_CHECK(env) ;

  return *this ;
}

jstring_proxy::~jstring_proxy()
{

  if( jstr && str && !isLocal )
    env->ReleaseStringUTFChars(jstr, str) ;

  // Destructors probably should check for exceptions
  //  JEXCEPTION_CHECK(env) ;
}

jstring_global_proxy::jstring_global_proxy(JNIEnv *_env, jstring _jstr)
  : jstring_proxy(_env, _jstr)
{

  _jstr = (jstring)env->NewGlobalRef(_jstr) ;
  JEXCEPTION_CHECK(env) ;
}

jstring_global_proxy::~jstring_global_proxy()
{

}

byteArray_proxy::byteArray_proxy(JNIEnv *_env)
  : env(_env), jBytes(0L), bytes(0L)
{
}

byteArray_proxy::byteArray_proxy(JNIEnv *_env, jbyteArray _jBytes)
  : env(_env), jBytes(_jBytes), isLocal(false)
{
  if( jBytes == 0L ) {
    bytes = 0L ;
    len = 0 ;
    return ;
  }
  
  len = env->GetArrayLength(jBytes) ;
  bytes = env->GetByteArrayElements(jBytes, 0L) ;

  JEXCEPTION_CHECK(env) ;
}

byteArray_proxy::byteArray_proxy(JNIEnv *_env, jobject jobj, jfieldID fid)
  : env(_env)
{

  jBytes = (jbyteArray)env->GetObjectField(jobj, fid) ;
  JEXCEPTION_CHECK(env) ;

  len = env->GetArrayLength(jBytes) ;
  isLocal = false ;
  bytes = env->GetByteArrayElements(jBytes, 0L) ;
  JEXCEPTION_CHECK(env) ;
  
}

byteArray_proxy::byteArray_proxy(JNIEnv *_env, const char *_bytes, size_t len)
  : env(_env), isLocal(true)
{
  if( _bytes == 0L ) {
    bytes = 0L ;
    jBytes = 0L ;
    len = 0 ;
    return ;
  }
  jBytes = env->NewByteArray(len) ;
  bytes = (jbyte *)_bytes ;
  env->SetByteArrayRegion(jBytes, 0, len, bytes) ;
  JEXCEPTION_CHECK(env) ;
}

byteArray_proxy::byteArray_proxy(JNIEnv *_env, jobject jobj, jmethodID mid, ...)
  : env(_env)
{
  va_list ap ;
  va_start(ap, mid) ;

  jBytes = (jbyteArray)env->CallObjectMethodV(jobj, mid, ap) ;
  this->len = env->GetArrayLength(jBytes) ;
  isLocal = false ;
  va_end(ap) ;

  JEXCEPTION_CHECK(env) ;
  
}

byteArray_proxy& byteArray_proxy::operator=(jbyteArray _jBytes)
{
  releaseBytes() ;
  jBytes = _jBytes ;

  if( jBytes == 0L ) {
    bytes = 0L ;
    len = 0 ;
    return *this ;
  }

  len = env->GetArrayLength(jBytes) ;
  bytes = env->GetByteArrayElements(jBytes, 0L) ;
  isLocal = false ;
  
  JEXCEPTION_CHECK(env) ;

  return *this ;
}

char *byteArray_proxy::dup()
{
  if( bytes == 0L )
    return 0L ;
  char *val = (char *)malloc(getLen()) ;
  memcpy(val, bytes, getLen()) ;
  return val ;
}

void byteArray_proxy::releaseBytes()
{
  if( jBytes && bytes && !isLocal )
    env->ReleaseByteArrayElements(jBytes, bytes, JNI_ABORT) ;
}

size_t byteArray_proxy::getLen() const
{
  if( !jBytes )
    return 0 ;
  return len ;
}

byteArray_proxy::~byteArray_proxy()
{
  releaseBytes() ;
}


intArray_proxy::intArray_proxy(JNIEnv *_env)
  : env(_env), jints(0L), ints(0L), len(0)
{

}

intArray_proxy::intArray_proxy(JNIEnv *_env, jintArray _jints)
  : env(_env), jints(_jints), isLocal(false)
{
  ints = env->GetIntArrayElements(jints, 0L) ;
  
  JEXCEPTION_CHECK(env) ;

  len = env->GetArrayLength(jints) ;
  
}

void intArray_proxy::setInts(JNIEnv *_env, int *oids, size_t len)
{
  releaseInts() ;
  jints = _env->NewIntArray(len) ;

  JEXCEPTION_CHECK(_env) ;
  ints = (jint *)oids ;

  _env->SetIntArrayRegion(jints, 0, len, (jint *)oids) ;
  JEXCEPTION_CHECK(_env) ;
}

intArray_proxy::intArray_proxy(JNIEnv *_env, int *oids, size_t _len)
  : env(_env), isLocal(true), len(_len) 
{
  setInts(env, oids, len) ;

}

intArray_proxy::intArray_proxy(JNIEnv *_env, jobject jobj, jfieldID fid)
  : env(_env)
{
  jints = (jintArray)env->GetObjectField(jobj, fid) ;
  JEXCEPTION_CHECK(env) ;
  setInts() ;
}

intArray_proxy::intArray_proxy(JNIEnv *_env, jobject jobj, jmethodID mid, ...)
  : env(_env)
{
  va_list ap ;
  va_start(ap, mid) ;
  
  jints = (jintArray)env->CallObjectMethodV(jobj, mid, ap) ;

  va_end(ap) ;
  JEXCEPTION_CHECK(env) ;
  
  len = env->GetArrayLength(jints) ;
}

void intArray_proxy::releaseInts()
{
  if( jints && ints && !isLocal )
    env->ReleaseIntArrayElements(jints, ints, JNI_ABORT) ;
}

void intArray_proxy::setInts()
{

  if( jints == 0L ) {
    len = 0 ;
    ints = 0L ;
    return ;
  }
  len = env->GetArrayLength(jints) ;
  ints = env->GetIntArrayElements(jints, 0L) ;
  JEXCEPTION_CHECK(env) ;
}


int *intArray_proxy::dup()
{
  int *rVal ;
  if( jints == 0L )
    return 0L ;

  rVal = (int *)malloc(sizeof(int) * getLen()) ;
  memcpy(rVal, ints, getLen() * sizeof(int)) ;
  return rVal ;
}

size_t intArray_proxy::getLen() const 
{
  if( !jints )
    return 0 ;
  return len ;
}

intArray_proxy& intArray_proxy::operator=(jintArray _jints)
{
  releaseInts() ;
  jints = _jints ;
  setInts() ;
  return *this ;
}

intArray_proxy::~intArray_proxy()
{
  releaseInts() ;
}


jproperty_proxy::jproperty_proxy(JNIEnv *env, const char *prop, const char *defaultProp)
{
  jstring jstr, jprop, jdefaultProp ;
  str_proxy = 0L ;

  jprop = env->NewStringUTF(prop) ;
  JEXCEPTION_CHECK(env) ;
  if( !jprop )
    return ;

  jdefaultProp = env->NewStringUTF(defaultProp) ;
  JEXCEPTION_CHECK(env) ;
  if( !jdefaultProp )
    return ;

  jstr = (jstring)env->CallStaticObjectMethod(System, SysGetPropertyDef, jprop, jdefaultProp) ;
  JEXCEPTION_CHECK(env) ;
  if( !jstr )
    return ;

  str_proxy = new jstring_proxy(env, jstr) ;
}

jproperty_proxy::jproperty_proxy(JNIEnv *env, const char *prop)
{
  jstring jstr, jprop ;
  str_proxy = 0L ;

  jprop = env->NewStringUTF(prop) ;
  JEXCEPTION_CHECK(env) ;
  if( !jprop )
    return ;

  jstr = (jstring)env->CallStaticObjectMethod(System, SysGetProperty, jprop) ;
  JEXCEPTION_CHECK(env) ;
  if( !jstr )
    return ;

  str_proxy = new jstring_proxy(env, jstr) ;
}

jproperty_proxy::operator int() const 
{
  if( !str_proxy )
    return 0 ;
  
  return atoi(*str_proxy) ;
}

jproperty_proxy::operator bool() const 
{ 
  return str_proxy != 0L && (const char *)*str_proxy != 0L ; 
}


jproperty_proxy::~jproperty_proxy()
{
  if( str_proxy )
    delete str_proxy ;
}

ProxyList::ProxyList()
{
  head = 0L ;
}

void ProxyList::add(jni_proxy *new_proxy)
{
  jni_proxy *p ;

  if( head == 0L ) {
    head = new_proxy ;
    n = 1 ;
    return ;
  }
  
  p = head ;
  while( p->next != 0L ) 
    p = p->next ;

  p->next = new_proxy ;
  n += 1 ;
}

void ProxyList::remove(jni_proxy *target)
{
  jni_proxy *lastP, *p ;

  if( target == head ) {
    head = head->next ;
    return ;
  }

  lastP = head ;
  p = head ;
  while( p ) {
    if( p == target ) {
      lastP->next = p->next ;
      return ;
    }

    lastP = p ;
    p = p->next ;
  }
}

JLocker::JLocker(JNIEnv *theEnv)
{
  env = theEnv ;
  obj = 0L ;
  isHeld = false ;
}

JLocker::JLocker(JNIEnv *theEnv, jobject theObj, bool hold)
{
  env = theEnv ;
  obj = theObj ;
  isHeld = false ;
  if( !hold && theObj )
    return ;
  this->lock() ;
}

JLocker& JLocker::operator=(jobject theObj)
{
  this->unlock() ;
  obj = theObj ;
  this->lock() ;
  return *this ;
}

void JLocker::lock()
{
  jint err ;

  if( isHeld || !obj )
    return ;

  err = env->MonitorEnter(obj) ;
  JEXCEPTION_CHECK(env) ;
  if( err != 0 ) 
    throw JNIProxyException() ;

  isHeld = true ;
}

void JLocker::unlock()
{
  jint err ;

  if( !isHeld || !obj )
    return ;

  err = env->MonitorExit(obj) ;
  JEXCEPTION_CHECK(env) ;

  if( err != 0 ) 
    throw JNIProxyException() ;
  
  isHeld = false ;
}

void JLocker::wait()
{
  assert(isHeld) ;
  isHeld = false ;
  env->CallVoidMethod(obj, WaitMethod) ;
  JEXCEPTION_CHECK(env) ;

  isHeld = true ;
}

void JLocker::notify()
{
  assert(isHeld) ;
  env->CallVoidMethod(obj, NotifyMethod) ;
  JEXCEPTION_CHECK(env) ;
}

JLocker::~JLocker()
{
  this->unlock() ;
}

/*
$Log: jni_proxy.cc,v $
Revision 1.18  2003/05/04 22:25:19  aepage
fix for exception handling

Revision 1.17  2003/05/04 21:32:46  aepage
fixes and extension of use of the JEXCEPTION_CHECK macro

Revision 1.16  2003/05/03 23:37:31  aepage
changes to support agentX subagents

Revision 1.15  2003/04/27 11:49:07  aepage
added jstaticFieldID_proxy for proxying static fields within classes.

Revision 1.14  2003/04/25 22:55:11  aepage
fixes from win32 compilation

Revision 1.13  2003/04/23 17:28:39  aepage
Fix to 'close' operation that tightens up the locking operations of
the nativeThread.  This corrects some 'bad file' errors on select for
Solaris that were not occurring under Linux

Revision 1.12  2003/04/18 01:25:36  aepage
support for natively generated/allocated objects.  New assignment
operators.

Revision 1.11  2003/04/15 19:37:16  aepage
better exception support and support for localized jclass and jmethodID proxies

Revision 1.10  2003/04/12 01:31:31  aepage
added various thread methods

Revision 1.9  2003/03/31 17:43:58  aepage
fixes for shared lib issues

Revision 1.8  2003/03/30 21:58:17  aepage
fixes for win32

Revision 1.7  2003/03/29 23:19:43  aepage
continuation of fixes for static initializers and shared libraries.
Next stop win32.

Revision 1.6  2003/03/29 18:12:14  aepage
Fix to the bedamned issue of static constructors between SUNpro C++
and gnu g++.  The trick(for SunPRO) seems to have the global list
instantiated in the main body of the code rather than the support
library.  It remains to be seen if this will hold up for g++, but at
least, the link order seems to remain the same.

Revision 1.5  2003/03/28 23:43:59  aepage
Added GetEnv function for attaching to native threads.

Revision 1.4  2003/03/28 13:40:25  aepage
fix for ASNObject conversion

Revision 1.3  2003/03/24 13:33:09  aepage
JLocker object fixes

Revision 1.2  2003/03/21 21:40:53  aepage
JLocker class that allows for 'scoped' locks that will automatically
free.

Revision 1.1  2003/03/21 14:42:55  aepage
Initial checkins

Revision 1.9  2003/03/20 21:01:57  aepage
additionl debugging

Revision 1.8  2003/03/20 00:06:16  aepage
Removal of JNI_PROXY_HIPERF instances

Revision 1.7  2003/03/18 20:22:24  aepage
modifications to proxy classes in an attempt to overcome the vagaries
of static constructor initializations in various compilers.

Revision 1.6  2003/03/14 15:59:26  aepage
Addtional support functions to print to java streams.  Test for
success of finding a class, methodID, fieldID etc and a cleaner
failure path.  Removal of troublesome try blocks that have problems
with g++ that are now irrellevant with other fixes.  Addition of array
proxy classes that take advantage of C++ stack frame cleanups.

Revision 1.5  2003/03/05 15:35:14  aepage
Crude first pass at removing the STL list dependency

Revision 1.4  2003/03/02 16:52:03  aepage
pre release checkin

Revision 1.3  2003/02/27 17:50:00  aepage
jstring_proxy class

Revision 1.2  2003/02/22 23:27:17  aepage
Win32 Port fixes

Revision 1.1.1.1  2003/02/07 23:56:53  aepage
Migration Import

Revision 1.1  2003/02/04 22:05:02  aepage
Initial Checkin

*/
