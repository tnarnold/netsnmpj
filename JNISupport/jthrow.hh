#ifndef _H_JTHROW
#define _H_JTHROW
#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif

  void ThrowIOException(JNIEnv *env, int eErrno, const char *fmt, ...) ;

#ifdef __cplusplus
}
#endif


#endif
