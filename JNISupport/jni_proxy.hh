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

#ifndef _H_JNI_PROXY
#define _H_JNI_PROXY
#include <jni.h>

#ifdef WIN32
#define snprintf _snprintf
#endif


/**
 * Convenience class to contain a list of proxies. 
 *  NOTE:  This is a step down from using a std::list, however,
 * in attempting to use sourceforge's compile farm to do a solaris
 * build with the sunpro compiler suite, its installed STL proved to
 * be such a nightmare, that it has reached a point of diminishing returns
 * to keep using std::list for just this simple instance.
 *
 *   I would like to note that the GNU STL implementation and Microsoft's
 * from Visual Studio 6.0 worked very smoothly.  
 *
 *   This could be just a result of an old STL/RougeWave installation
 * being present on the sourceforge compile farm. 
 *
 *
 *   NOTE:   the current implementation of this crude list will not preserve
 * the entry order when a remove operation has been performed
 */

#define DEFAULT_PROXY_LIST_INCREMENT 256

class Throwable {
public:
  jthrowable je ;
  Throwable(jthrowable _je) ;
} ;

class JNIProxyException {
public:
} ;

class ProxyList {
public:
  int n ;
  ProxyList() ;
  void add(class jni_proxy *) ;
  void remove(class jni_proxy *) ;
  bool InitProxies(JNIEnv *env) ;
  bool RemoveProxies(JNIEnv *env) ;

private:
  class jni_proxy *head ;

} ;

class jni_proxy {
public:
  jni_proxy() ;
  jni_proxy(JNIEnv *env) ;
  operator const char *() const { return toString() ; }
  virtual const char *toString() const = 0 ;

  /**
   * The init method is to be called once at OnLoad.
   * This will load and globalize(NewGlobalRef) object
   * types such as jclass so that it will not be necessary
   * to reload them for each use of the library.  
   *
   * @return true if successful, false otherwise
   * 
   */
  virtual bool init(JNIEnv *env) = 0 ;
  virtual bool close(JNIEnv *env) ;


  virtual ~jni_proxy() ;
  
protected:
  const bool isLocal ;
private:
  friend class ProxyList ;

  class jni_proxy *next ;

} ;

class jclass_proxy : public jni_proxy {
public:
  jclass_proxy(JNIEnv *env, const char *name) ; // for use in local frames
  jclass_proxy(const char *cname) ;
  operator jclass () const ;
  virtual ~jclass_proxy() ;
  virtual const char *toString() const ;


  virtual bool init(JNIEnv *env) ;
  virtual bool close(JNIEnv *env) ;
  bool isa(JNIEnv *env, jobject obj) ;

private:
  const char * const classname ;
  bool isLocal ;
  jclass clazz ;
} ;

inline jclass_proxy::operator jclass () const { return clazz ; }


class jfieldID_proxy : public jni_proxy {
public:
  jfieldID_proxy(const jclass_proxy& jc, const char *fName, const char *fieldSig) ;
  operator jfieldID () const ;
  virtual ~jfieldID_proxy() ;
  virtual const char *toString() const ;

public:
  virtual bool init(JNIEnv *env) ;
private:
  jfieldID fid ;

private:
  const jclass_proxy& clazz ;
  const char * const fieldName ;
  const char * const fieldSignature ;
} ;

inline jfieldID_proxy::operator jfieldID () const { return fid ; }


class jstaticFieldID_proxy : public jni_proxy {
public:
  jstaticFieldID_proxy(const jclass_proxy& jc, const char *fName, const char *fieldSig) ;
  jstaticFieldID_proxy(JNIEnv *env, const jclass_proxy& jc, const char *fName, const char *fieldSig) ;
  operator jfieldID () const ;
  virtual ~jstaticFieldID_proxy() ;
  virtual const char *toString() const ;

public:
  virtual bool init(JNIEnv *env) ;
private:
  jfieldID fid ;

private:
  const jclass_proxy& clazz ;
  const char * const fieldName ;
  const char * const fieldSignature ;
} ;

inline jstaticFieldID_proxy::operator jfieldID () const { return fid ; }


class jmethodID_proxy : public jni_proxy {
public:
  operator jmethodID () const ;
  jmethodID_proxy(const jclass_proxy& jc, const char *mname, const char *mSig) ;
  jmethodID_proxy(JNIEnv *env, jclass_proxy& jc, const char *mname, const char *mSig) ;
  virtual ~jmethodID_proxy() ;
  virtual const char *toString() const;

public:
  virtual bool init(JNIEnv *env) ;
private:
  jmethodID mid ;

private:
  const jclass_proxy& clazz ;
  const char * const methodName ;
  const char * const methodSignature ;
} ;

inline jmethodID_proxy::operator jmethodID () const { return mid ; }

class jstaticmethodID_proxy : public jni_proxy {
public:
  operator jmethodID () const ;
  jstaticmethodID_proxy(const jclass_proxy& jc, const char *mname, const char *mSig) ;
  virtual ~jstaticmethodID_proxy() ;
  virtual const char *toString() const;

public:
  virtual bool init(JNIEnv *env) ;
private:
  jmethodID mid ;

private:
  const jclass_proxy& clazz ;
  const char * const methodName ;
  const char * const methodSignature ;
} ;

inline jstaticmethodID_proxy::operator jmethodID () const { return mid ; }

class jobject_static_field_proxy : public jni_proxy {
public:
  jobject_static_field_proxy(const jclass_proxy& jc, const char *fName, const char *descriptor) ;
  operator jobject () const ;
  virtual ~jobject_static_field_proxy() ;
  virtual const char *toString() const;

public:
  virtual bool init(JNIEnv *env) ;
private:
  jobject obj ;

private:
  const jclass_proxy& clazz ;
  const char * const fieldName ;
  const char * const fieldSignature ;

} ;

inline jobject_static_field_proxy:: operator jobject () const { return obj ; }


#ifdef __cplusplus
extern "C" {
#endif

  /**
   * subroutine that allows for printing on the
   * System.out stream
   */
  int sysout_printf(JNIEnv *env, const char *fmt, ...) ;
  /**
   * subroutine that allows for printing on the
   * System.out stream
   */
  int syserr_printf(JNIEnv *env, const char *fmt, ...) ;
  
  int stream_printf(JNIEnv *env, jobject theStream, const char *fmt, ...) ;

  bool InitJNIProxies(JNIEnv *env) ;
  void RemoveClassJNIProxy(class jni_proxy *) ;
  void RemoveJNIProxy(class jni_proxy *) ;
  bool DeleteJNIProxies(JNIEnv *env) ;

#ifdef __cplusplus
}
#endif

/**
 * jstring_proxy
 *
 *   A proxy class that converts a jstring to a const char *
 * when the scope that contains the proxy exits, the string
 * is automatically released.
 */

class jstring_proxy {
public:
  jstring_proxy(JNIEnv *env) ;
  jstring_proxy(JNIEnv *env, jstring _jstr) ; // convert jstring to native string
  jstring_proxy(JNIEnv *env, jobject obj, jfieldID fid) ;
  jstring_proxy(JNIEnv *env, const char *s) ; // convert native string to jstring
  jstring_proxy(JNIEnv *env, const char *s, size_t len) ; // convert native string to jstring
  virtual ~jstring_proxy() ;
  operator const char *() const ;
  operator jstring() const ;
  operator jobject() const ;
  jstring_proxy& operator=(const char *s) ;
  jstring_proxy& operator=(jstring _jstr) ;
  

  jsize length() ;
protected:
  bool isLocal ;
  JNIEnv *env ;
  jstring jstr ;
  const char *str ;
} ;

inline jstring_proxy::operator const char *() const { return str ; }
inline jstring_proxy::operator jstring() const { return jstr ; }
inline jstring_proxy::operator jobject() const { return (jobject)jstr ; }
/**
 * jstring_global_proxy
 *
 *  globalizes the string, so that the string will not be reclaimed
 * between calls into the native library.  Should be used for string
 * storage within native data pointers.
 */
class jstring_global_proxy : public jstring_proxy {
public:
  jstring_global_proxy(JNIEnv *env, jstring _jstr) ;
  virtual ~jstring_global_proxy() ;
  operator const char *() const ;
} ;

inline jstring_global_proxy::operator const char *() const { return str ; }



class jproperty_proxy  {
public:
  jproperty_proxy(JNIEnv *env, const char *prop, const char *defaultProp) ;
  jproperty_proxy(JNIEnv *env, const char *prop) ;
  virtual ~jproperty_proxy() ;
  operator const char *() const ;
  operator bool() const ;
  operator int() const ;
private:
  jstring_proxy *str_proxy ;
} ;

inline jproperty_proxy::operator const char *() const { return (const char *)*str_proxy ; }

/**
 * Convenience class that will track the allocation/conversion arrays from
 * their java analogs.  This class is pretty much 'in flux' and should be used
 * with some caution.
 *
 *  TBD:  add a proper copy constructor and perhaps extend to a ref count
 * paradigm.  
 */
class byteArray_proxy {
public: 
  byteArray_proxy(JNIEnv *env) ;
  byteArray_proxy(JNIEnv *env, const char *, size_t len) ;
  byteArray_proxy(JNIEnv *env, jbyteArray _jByteArray) ;
  byteArray_proxy(JNIEnv *env, jobject jobj, jfieldID fid) ;
  byteArray_proxy(JNIEnv *env, jobject jobj, jmethodID mid, ...) ;
  operator const char *() const ;
  // operator unsigned char *() const ;
  operator jbyteArray() const ;
  operator jobject() const ;
  byteArray_proxy& operator=(jbyteArray _jBytes) ;
  
  size_t getLen() const ;
  /**
   * duplicate with a malloc'd piece of memory
   */
  char *dup() ; 

  virtual ~byteArray_proxy() ;
private:
  void releaseBytes() ;

  JNIEnv *env ;
  jbyteArray jBytes ;
  jbyte *bytes ;
  bool isLocal ;
  size_t len ;
} ;

inline byteArray_proxy::operator const char *() const { return (const char *)bytes ; }
// inline byteArray_proxy::operator unsigned char *() const { return (unsigned char *)bytes ; }
inline byteArray_proxy::operator jbyteArray() const { return jBytes ; }
inline byteArray_proxy::operator jobject() const { return (jobject)jBytes ; }

/**
 * Convenience class that will track the allocation/conversion arrays from
 * their java analogs.  This class is pretty much 'in flux' and should be used
 * with some caution.
 *
 *  TBD:  add a proper copy constructor and perhaps extend to a ref count
 * paradigm.  
 */
class intArray_proxy {
public:
  intArray_proxy(JNIEnv *env) ;
  intArray_proxy(JNIEnv *env, jintArray _jints) ;
  intArray_proxy(JNIEnv *env, jobject jobj, jfieldID fid) ;
  intArray_proxy(JNIEnv *env, jobject jobj, jmethodID mid, ...) ;
  intArray_proxy(JNIEnv *env, int *oids, size_t len) ;

  intArray_proxy& operator=(jintArray _jInts) ;
  operator const int *() const ;
  operator int *() const ;
  operator unsigned long *() const ;
  operator jobject() const ;
  size_t getLen() const ;
  virtual ~intArray_proxy() ;
  int *dup() ;
  void setInts(JNIEnv *env, int *oids, size_t len) ;
private:
  void releaseInts() ;
  void setInts() ;
  JNIEnv *env ;
  jintArray jints ;
  jint *ints ;
  bool isLocal ;
  size_t len ;
} ;

inline intArray_proxy::operator const int *() const { return (const int *)ints ; }
inline intArray_proxy::operator int *() const { return (int *)ints ; }
inline intArray_proxy::operator unsigned long *() const { return (unsigned long *)ints ; }
inline intArray_proxy::operator jobject() const { return (jobject)jints ; }

/**
 * A class that will get the java 'lock' on a given object
 * and release it if the frame exits
 */
class JLocker {
public:
  JLocker(JNIEnv *env) ;
  JLocker(JNIEnv *env, jobject obj, bool hold = true) ;
  JLocker& operator=(jobject obj) ;
  ~JLocker() ;
  void lock() ;
  void unlock() ;
  void wait() ;
  void notify() ;
private:
  bool isHeld ;
  jobject obj ;
  JNIEnv *env ;
  
} ;

/*
 * Other Prototypes
 */
void CheckException(JNIEnv *env) ;
void notifyAll(JNIEnv *env, jobject obj) ;
/**
 * throw a null pointer exception if the object is null
 */
void ThrowIfNull(JNIEnv *env, jobject jobj) ;

/**
 * Check for an exception and throw it if one has occurred 
 */
#define JEXCEPTION_CHECK(env) { jthrowable __je ; if( (__je = env->ExceptionOccurred()) != 0L ) { env->ExceptionClear() ; throw Throwable(__je) ; } }


#endif // _H_JNI_PROXY
/*
 * $Log: jni_proxy.hh,v $
 * Revision 1.10  2003/05/04 21:32:46  aepage
 * fixes and extension of use of the JEXCEPTION_CHECK macro
 *
 * Revision 1.9  2003/05/03 23:37:31  aepage
 * changes to support agentX subagents
 *
 * Revision 1.8  2003/04/27 11:49:07  aepage
 * added jstaticFieldID_proxy for proxying static fields within classes.
 *
 * Revision 1.7  2003/04/23 17:28:39  aepage
 * Fix to 'close' operation that tightens up the locking operations of
 * the nativeThread.  This corrects some 'bad file' errors on select for
 * Solaris that were not occurring under Linux
 *
 * Revision 1.6  2003/04/18 01:25:36  aepage
 * support for natively generated/allocated objects.  New assignment
 * operators.
 *
 * Revision 1.5  2003/04/15 19:37:15  aepage
 * better exception support and support for localized jclass and jmethodID proxies
 *
 * Revision 1.4  2003/04/12 01:31:50  aepage
 * added wait method
 *
 * Revision 1.3  2003/03/24 13:33:08  aepage
 * JLocker object fixes
 *
 * Revision 1.2  2003/03/21 21:40:53  aepage
 * JLocker class that allows for 'scoped' locks that will automatically
 * free.
 *
 * Revision 1.1  2003/03/21 14:42:55  aepage
 * Initial checkins
 *
 * Revision 1.8  2003/03/20 00:06:15  aepage
 * Removal of JNI_PROXY_HIPERF instances
 *
 * Revision 1.7  2003/03/18 20:22:25  aepage
 * modifications to proxy classes in an attempt to overcome the vagaries
 * of static constructor initializations in various compilers.
 *
 * Revision 1.6  2003/03/17 16:16:48  aepage
 * adjustments for types.
 *
 * Revision 1.5  2003/03/14 15:59:26  aepage
 * Addtional support functions to print to java streams.  Test for
 * success of finding a class, methodID, fieldID etc and a cleaner
 * failure path.  Removal of troublesome try blocks that have problems
 * with g++ that are now irrellevant with other fixes.  Addition of array
 * proxy classes that take advantage of C++ stack frame cleanups.
 *
 * Revision 1.4  2003/03/05 15:35:14  aepage
 * Crude first pass at removing the STL list dependency
 *
 * Revision 1.3  2003/02/27 17:49:59  aepage
 * jstring_proxy class
 *
 * Revision 1.2  2003/02/22 23:29:47  aepage
 * Win32 Port fixes
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
