#ifndef _H_JOIDPROXY
#define _H_JOIDPROXY
#include <jni.h>
#include "jni_proxy.hh"

/**
 * Class to be instantiated in a local frame that will
 * extract the native integers making up an OID and then
 * free them accordingly when the object goes 'out of scope'.
 *
 *   NOTE:   This object is not to be used with the new operator.
 * use of this will be prevented in the future by the throwing
 * of an exception
 */



class joid_proxy {
public:
  joid_proxy(JNIEnv *env, jobject obj, jfieldID field) ;
  joid_proxy(JNIEnv *env, jfieldID field) ; // will extract the fieldid from assigned objects
  joid_proxy(JNIEnv *env, jobject joid) ;
  joid_proxy(JNIEnv *env, int *oids, size_t len) ;
  joid_proxy(JNIEnv *env) ;
  virtual ~joid_proxy() ;
  joid_proxy& operator=(const jobject&) ;
  operator int *() const ;
  operator jobject() const ;
  operator unsigned long *() const ;
  bool good() ; // false if an allocation failed and java exception thrown
  int getLen() const ;
  void setOids(JNIEnv *env, int *oids, size_t len) ;
  int *dup() ; // duplicate with a malloc'd piece of memory
private:
  bool isLocal ;
  JNIEnv *env ;
  intArray_proxy joidsArray ;
  jobject joid ;
} ;

inline joid_proxy::operator jobject() const { return joid ; }
inline joid_proxy::operator int *() const { return joidsArray ; }
inline joid_proxy::operator unsigned long *() const { return (unsigned long *)joidsArray ; }
inline int joid_proxy::getLen() const { return joidsArray.getLen() ; }

#endif // _H_JOIDPROXY
