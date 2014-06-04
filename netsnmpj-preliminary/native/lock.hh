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

#ifndef _H_LOCK
#define _H_LOCK
#include "exception.hh"
#include <errno.h>
#ifndef WIN32
#include <pthread.h>
#else
#include <windows.h>
#endif


class TimeOutException : public Exception {
public:
  TimeOutException() ;
} ;

inline TimeOutException::TimeOutException() : Exception(-1) {}


class Lock {
public:
#ifndef WIN32
  Lock(pthread_mutexattr_t *mtx_attr = (pthread_mutexattr_t *)0L,
       pthread_condattr_t *cnd_attr = (pthread_condattr_t *)0L) ;
#else
  Lock() ;
#endif
  ~Lock() ;
  void lock(), unlock(), wait(const struct timespec *t = 0L) ; // see nanosleep for details on timespec
  void signal() ;
  void broadcast() ;
  bool trylock() ;
private:
#ifndef WIN32
  pthread_cond_t _cond ;
  pthread_mutex_t _lock ;
#else
  HANDLE _lock ;
  
#endif
  int _waiters ;

} ;


/*
 * Convenience class that will automatically unlock
 * a mutex destruction from a return from subroutine, or
 * a thrown exception
 */
class Locker {
public:
  Locker(Lock& lck, bool isLocked = true) ;
  ~Locker() ;
  void lock(), unlock() ;
private:
  Lock& _lock_ref ;
  bool _isLocked ;
} ;

#endif // _H_LOCK
/*
 * $Log: lock.hh,v $
 * Revision 1.4  2003/04/12 00:44:08  aepage
 * fixes from -Wall from g++
 *
 * Revision 1.3  2003/02/24 00:05:20  aepage
 * New interrupt scheme that should work for both Win32 and Unix/Linux.
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
