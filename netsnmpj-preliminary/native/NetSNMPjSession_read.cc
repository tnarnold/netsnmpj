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
#include <sys/timeb.h>
#endif

#include "org_netsnmp_NetSNMPSession.h"
#include "NetSNMPjSessionStruct.hh"
#include <jni_proxy.hh>
#include "jthrow.hh"

#include <assert.h>
#include <string.h>
#include <time.h>
#include <signal.h>
#include <stdarg.h>
#include <limits.h>
#include <stdlib.h>
#include <errno.h>

#ifndef _WIN32

#include <netdb.h>
#include <unistd.h>
#include <pthread.h>

#define SELECT_FAILURE -1
#define READ_WOULD_BLOCK  EWOULDBLOCK

#else
#include <io.h>

#define ssize_t int
#define read _read

#define READ_WOULD_BLOCK  WSAEWOULDBLOCK

#define SELECT_FAILURE SOCKET_ERROR
#endif /* WIN32 */

extern jclass_proxy ioExceptionClass ;
extern jclass_proxy readInterruptClass ;
extern jclass_proxy NetSNMPSession ;

extern jobject_static_field_proxy readLockField ;
extern jobject_static_field_proxy intAckField ;
extern jstaticmethodID_proxy NSS_finalizeNatives ;

extern int ParkNativeRead ;

  /*
   * NOTE:   030223
   * pipes used to alert the NetSNMPSession.read method
   * that some other operation is pending.  This methodology
   * is going to replace the 'signal' interface, since
   *  a)  it uses a native faclity(sockets) that is integral
   * to the operation of the read method.
   *  b)  It should prove to be more easily interoperable
   * between Win32 and Unix/Linux
   */

int InterruptPipe[2] ;


/** 
 *
 *  Interrupts read operations occurring on NetSNMPSession.read()
 * @returns TRUE if successful 
 * @returns FALSE othereise (java ioexception is thrown)
 */
bool SendInterrupt(JNIEnv *env)
{
  char c[1] ;
  ssize_t n ;

#ifndef WIN32
  n = write(InterruptPipe[1], c, sizeof(c)) ;
#else
  n = send(InterruptPipe[1], c, sizeof(c), 0) ; // TBD see if this works for unix
#endif
  if( n < (ssize_t)sizeof(c) ) {
    ThrowIOException(env, errno, " SendInterrupt") ;
    return false ;
  }

  return true ;
}

/**
 * Clears the interrupt sent by SendInterrupt
 */
bool ClearInterrupt(JNIEnv *env)
{
  char c ;
  ssize_t n ;

#ifndef WIN32
  n = read(InterruptPipe[0], &c, sizeof(c)) ;
#else
  n = recv(InterruptPipe[0], &c, sizeof(c), 0) ;
#endif
  if( n <= -1 ) {
    ThrowIOException(env, errno, "ClearInterupt") ;
    return false ;
  }
  return true ;
}

/*
 * Class:     org_netsnmp_NetSNMPSession
 * Method:    interrupt
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_netsnmp_NetSNMPSession_interrupt(JNIEnv *env, jclass)
{
  SendInterrupt(env) ; // exception thrown or not we can not bother check the return
}


#ifndef NDEBUG
int runawayCount = 0 ;
#endif

/*
 * Class:     org_netsnmp_NetSNMPSession
 * Method:    read
 * Signature: ()V
 */
void internalRead(JNIEnv *env, jclass, jdouble timeout_secs, jint nReads)
{
  JLocker rLock(env, readLockField, false) ;
  JLocker intAck(env, intAckField, false) ;
  struct timeval timeout, *timeoutPtr = &timeout ;
#ifndef WIN32
  struct timeval currentTime, completedTime ;
#else
  struct timeb currentTime, completedTime ;
#endif
  int i, cnt, nFds, block = 1 ;
  fd_set fdset, checkingSet ;

  if( timeout_secs == -1.0 ) {
    timeoutPtr = 0L ; 
    block = 1 ;
  }

  while( nReads-- ) {
    rLock.lock() ;
#ifndef WIN32
    gettimeofday(&currentTime, 0L) ;
    timeout.tv_sec = (long)timeout_secs ;
    timeout.tv_usec = (long)((timeout_secs - timeout.tv_sec) * 1E6) ;
#else
    ftime(&currentTime) ;
#endif
    FD_ZERO(&fdset) ;
    nFds = 0 ;


    /*
     * Finalize any closed sessions as necessary
     */
    env->CallStaticVoidMethod(NetSNMPSession, NSS_finalizeNatives) ;


    /*
     * add the interrupt pipe to the fdset so that we can be 
     * interrupted by another thread
     */

    FD_SET(InterruptPipe[0], &fdset) ;

    /*
     * construct the fdset for the open snmp sessions
     */

    cnt = snmp_select_info(&nFds, &fdset, &timeout, &block) ;
    if( (InterruptPipe[0]+1) > nFds )
      nFds = InterruptPipe[0]+1 ; // in case our pipe is outside the fdset
   
    /*
     * some flavors of select will give a EINVAL error
     * for timeout values that are less than zero or pegged
     * to 1 hour.  
     */
    if( block || timeoutPtr->tv_sec < 0 || timeoutPtr->tv_sec >= 3600 )
      timeoutPtr->tv_sec = 3600 ; /* an hour should be sufficient */
    if( block || timeoutPtr->tv_usec < 0  )
      timeoutPtr->tv_usec = 0 ;

     
#ifndef NDEBUG
    if( timeoutPtr->tv_sec <= 0 && timeoutPtr->tv_usec < 1000 )
      runawayCount+=1 ;
    else
      runawayCount = 0 ;

    if( runawayCount > 1000 ) {
      ThrowIOException(env, -1, "runaway select") ;
      runawayCount = 0 ;
      return ;
    }
#endif // NDEBUG
    
    //fprintf(stderr, "timeout = %.6f  cnt = %d/%d timeout.secs = %d timeout.usecs = %d\n", 
    // timeout_secs, cnt, nFds, timeoutPtr->tv_sec, timeoutPtr->tv_usec) ; 
 
    rLock.unlock() ;

    /*
     * Note not all implementations treat timeout the same
     * way... it is being assumed that the value is not valid
     * for anything after select has completed.  
     * Ref linux man(2) select
     */

    cnt = select(nFds, &fdset, 0L, 0L,  timeoutPtr) ;
    if( cnt == SELECT_FAILURE ) {
      ThrowIOException(env, errno, "selecting tv_sec=%ld tv_usec=%ld nFds=%d", 
		       timeoutPtr->tv_sec, 
		       timeoutPtr->tv_usec, 
		       nFds) ;
      return ;
    }

    /**
     * If another thread has tried to interrupt us we'll detect it in
     * this fd, and respond accordingly.
     */
    if( FD_ISSET(InterruptPipe[0], &fdset) ) {
      if( !ClearInterrupt(env) )  // false if failure
	return ; // exception thrown
      // fprintf(stderr, "interrupt received IsClosing = %d\n", ParkNativeRead) ;
      intAck.lock() ;
	
      while( ParkNativeRead > 0 ) {
	intAck.notify() ; // notify interupt for close received
	intAck.wait() ;   // wait for acknowledgement of close
	ParkNativeRead -= 1 ;
      }

      intAck.unlock() ;
      continue ;
    }

    rLock.lock() ;

    if( cnt != 0 ) {
      FD_ZERO(&checkingSet) ;
      for( i = 0 ; i < nFds ; i++ ) {
	if( !FD_ISSET(i, &fdset) )
	  continue ;
	FD_SET(i, &checkingSet) ;
	rLock.unlock() ;
	snmp_read(&checkingSet) ;
	rLock.lock() ;
	FD_CLR(i, &checkingSet) ;
	if( env->ExceptionCheck() )
	  return ; // exception was thrown by the callback
      }
    }
    else 
      snmp_timeout() ;

#ifndef WIN32
    gettimeofday(&completedTime, 0L) ;
    timeout_secs -= ((double)completedTime.tv_sec + ((double)completedTime.tv_usec/1e6)) -
      ((double)currentTime.tv_sec + ((double)currentTime.tv_usec/1e6)) ;
#else
    ftime(&completedTime) ;
    timeout_secs -= ((double)completedTime.time + ((double)completedTime.millitm/1e3)) -
      ((double)currentTime.time + ((double)currentTime.millitm/1e3)) ;
#endif

    rLock.unlock() ;
  } // while

}

/*
 * NetSNMPjSession_read.cc,v
 * Revision 1.18  2003/04/27 11:55:46  aepage
 * more careful synchronizing of close operations.
 *
 * Revision 1.17  2003/04/25 22:59:42  aepage
 * Fix for the 'runaway select', and also a fix for a thread race
 * condition that was not making itself apparent for Linux when sessions were being closed.
 *
 * Revision 1.16  2003/04/23 17:28:35  aepage
 * Fix to 'close' operation that tightens up the locking operations of
 * the nativeThread.  This corrects some 'bad file' errors on select for
 * Solaris that were not occurring under Linux
 *
 * Revision 1.15  2003/04/18 01:35:37  aepage
 * deprecation and hiding of visiable internal read method.
 *
 * Revision 1.14  2003/04/15 19:26:20  aepage
 * eliminated jvm global
 *
 * Revision 1.13  2003/03/30 22:02:28  aepage
 * fixes for win32
 *
 * Revision 1.12  2003/03/29 00:13:38  aepage
 * new thread architecture
 *
 * Revision 1.11  2003/03/21 15:22:22  aepage
 * Increased multi-threading support.  SNMP fds are also now examined
 * indvidually to better allow us to catch exceptions that are thrown by
 * the registered handlers.
 *
 * Revision 1.10  2003/03/20 00:05:34  aepage
 * Removal of deprecated SetProxyEnv macro
 *
 * Revision 1.9  2003/03/18 01:16:16  aepage
 * read fix for solaris
 *
 * Revision 1.8  2003/03/17 19:26:39  aepage
 * removal of some try block.  C++ exceptions are under review
 * for various compiler/platoform differences.
 *
 * Revision 1.7  2003/03/17 16:15:49  aepage
 * migration of some functions, sanity check on return value from
 * snmp_select_info to accomodate linux version of select that does not
 * allow for a timeout field less than zero.
 *
 * Revision 1.6  2003/02/26 00:15:19  aepage
 * Micrsoft can't select on its pipe, and unix can't recv or send on its
 * pipes.
 *
 * Revision 1.5  2003/02/25 23:51:01  aepage
 * tune up of how the fdset is constructed
 *
 * Revision 1.4  2003/02/25 15:41:03  aepage
 * Many fixes to take advantage of new interrupt scheme.  Win32 additions
 * and fixes.  Cleanup of unused variables.
 *
 * Revision 1.3  2003/02/24 00:05:20  aepage
 * New interrupt scheme that should work for both Win32 and Unix/Linux.
 *
 * Revision 1.2  2003/02/08 22:14:11  aepage
 * solaris fixes
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
