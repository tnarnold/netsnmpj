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

#include "lock.hh"
#include <stdio.h>
#include <errno.h>
/*
 * boy is this awful, currently we're not creating any threads with anything
 * but the default attributes, however, for the future, if not for esprit,
 * clean this up. 
 */
#ifndef WIN32
Lock::Lock(pthread_mutexattr_t *mtx_attr, pthread_condattr_t *cnd_attr) 
#else
Lock::Lock()
#endif
{
  int err = 0 ;
  _waiters = 0 ;
#ifndef WIN32
  err = pthread_mutex_init(&_lock, mtx_attr) ;
  if( err != 0 ) 
    throw Exception(err) ;

  err = pthread_cond_init(&_cond, cnd_attr) ;
#else
  _lock = CreateMutex(NULL, FALSE, NULL) ;
  if( lock == 0L ) err = -1 ;
#endif  

  if( err != 0 ) 
    throw Exception(err) ;  

}

void Lock::broadcast()
{
  int err ;
#ifndef WIN32
  if( _waiters == 0 )
    return ;

  err = pthread_cond_broadcast(&_cond) ;
  if( err )
    throw Exception(err) ;
#else
    throw Exception(9999) ;// not yet implemented
#endif
}



Lock::~Lock()
{
  int err ;

#ifndef WIN32
  err = pthread_mutex_destroy(&_lock) ;
  if( err != 0 )
    throw Exception(err) ;

  err = pthread_cond_destroy(&_cond) ;
  if( err != 0 )
    throw Exception(err) ;
#else
  CloseHandle(_lock) ;
#endif
}

void Lock::wait(const struct timespec *t)
{
  int err ;
#ifndef WIN32
  _waiters += 1 ;

  if( t )
    err = pthread_cond_timedwait(&_cond, &_lock, t) ;
  else
    err = pthread_cond_wait(&_cond, &_lock) ;

  _waiters -= 1 ;

  if( err ) {
    if( err == ETIMEDOUT ) throw TimeOutException() ;
    throw Exception(err) ;
  }
#else
  throw Exception(9999) ;// not yet implemented
#endif
}

void Lock::signal()
{
  int err ;
#ifndef WIN32
  if( _waiters == 0 )
    return ;
  err = pthread_cond_signal(&_cond) ;
  if( err )
    throw Exception(err) ;
#else
  throw Exception(9999) ;// not yet implemented
#endif
}

void Lock::lock()
{
  int err ;
  

#ifndef WIN32
  err = pthread_mutex_lock(&_lock) ;
#else
  err = WaitForSingleObject(_lock, INFINITE) ;
  if( err == WAIT_OBJECT_0 )
    return ;
  throw Exception(err) ;
#endif
  if( err ) throw Exception(err) ;
}

bool Lock::trylock()
{
  int err ;

#ifndef WIN32
  err = pthread_mutex_trylock(&_lock) ; 
  if( err == EBUSY ) return false ; // lock is held somewhere
  if( err ) throw Exception(err) ;
  return true ;
#else	      
  err = WaitForSingleObject(_lock, 0) ;
  if( err == WAIT_OBJECT_0 ) return true ;
  if( err == WAIT_TIMEOUT ) return false ; // no error, but lock is held somewhere
  throw Exception(err) ;
#endif
}

void Lock::unlock()
{
  int err ;
#ifndef WIN32
  err = pthread_mutex_unlock(&_lock) ;
  if( err ) throw Exception(err) ;
#else
  ReleaseMutex(_lock) ;
  // TBD check an error code for an exception
#endif
}


void Locker::lock() { if( !_isLocked ) { _lock_ref.lock() ; _isLocked = true ; } }
void Locker::unlock() { if( _isLocked ) { _lock_ref.unlock() ; _isLocked = false ; } }
Locker::~Locker() { unlock() ; }

Locker::Locker(Lock& mutex, bool isLocked) 
  : _lock_ref(mutex), _isLocked(false)
{
  if( isLocked )
    lock() ;
}

/*
 * $Log: lock.cc,v $
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
