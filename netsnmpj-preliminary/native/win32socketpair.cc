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
 * win32socketpair.cc    
 *
 *   The current interrupt facility in netsnmpj works by establishing
 * a 'pipe'.  The read end of this pipe is added to the fdset that the
 * NetSNMPSession.read method uses to monitor the open snmp sessions.
 * an 'interrupt' to the read method can be delivered by other threads
 * by writing a byte into write end of the pipe(preferably with the
 * SendInterrupt subroutine supplied in the NetSNMPjSession_read.cc
 * file).
 *
 *   Unfortunately, Win32 pipes created with _pipe cannot be added to the 
 *  fdset.  Winsock select returns an error of WSAENOTSOCK(10038).  
 *
 *   For the time being we are forced to resort to this workaround
 * that creates a listennging socket, then connects socket to this and
 * spawns a socket with 'accept', then closes the listening socket and
 * returns the two connected sockets that can act effectively as a
 * pipe.
 *
 *
 * This is currenly implemented for the AF_INET family, but may be adapted
 * to others.  
 *
 *  This code is to be used for Windows versions of netsnmpj, and should not
 * be included in the builds of unix versions.  
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
#include <winsock2.h>
#include <stdio.h>
#include "NetSNMPjSessionStruct.hh"
/*
 * Start looking for a free port here
 */
#define PORTSTART 1234

/*
 * stop here
 */
#define PORTLIMIT 8000

/**
 * [out]  a pair of connected sockets
 * @return true if successful 
 */
int Win32SocketPair(int fds[2])
{
  SOCKET listeningSock, s, s2 ;
  sockaddr_in sAddr, sAddr2 ;
  int err, len ;
  u_long argp ;
  u_short portlimit = PORTSTART ;
  struct hostent *ent ;
  int reuseAddr = true ;

  /* create listening socket */

  listeningSock = socket(AF_INET, SOCK_STREAM, 0) ;
  if( listeningSock == INVALID_SOCKET )
    return false ;

  err = setsockopt(listeningSock, SOL_SOCKET, SO_REUSEADDR, (const char *)&reuseAddr, sizeof(reuseAddr)) ;
  if( err == SOCKET_ERROR )
    return false ;

  ent = gethostbyname("127.0.0.1") ;
  if( !ent )
    return false ;

  /* loop on ports till  bind  succeeds */

  sAddr.sin_family = AF_INET ;
  sAddr.sin_addr.S_un.S_un_b.s_b1 = ent->h_addr_list[0][0] ;
  sAddr.sin_addr.S_un.S_un_b.s_b2 = ent->h_addr_list[0][1] ;
  sAddr.sin_addr.S_un.S_un_b.s_b3 = ent->h_addr_list[0][2] ;
  sAddr.sin_addr.S_un.S_un_b.s_b4 = ent->h_addr_list[0][3] ;

  do {
    sAddr.sin_port = htons(portlimit) ;
    
    err = bind(listeningSock, (struct sockaddr *)&sAddr, sizeof(sAddr)) ;
  } while( err != 0 && portlimit++ < PORTLIMIT) ;

  if( err != 0 )
    return false ;

  err = listen(listeningSock, 1) ;
  if( err != 0 )
    return false ;

  /* non blocking 'connect' */

  s = socket(AF_INET, SOCK_STREAM, 0) ;
  if( s == INVALID_SOCKET )
    return false ;

  argp = 1 ;
  err = ioctlsocket(s, FIONBIO, &argp) ;
  if( err == SOCKET_ERROR )
    return false ;

  
  err = connect(s, (struct sockaddr *)&sAddr, sizeof(sAddr)) ;
  if( err != 0 && WSAGetLastError() != WSAEWOULDBLOCK )
    return false ;

  /* accept */

  s2 = accept(listeningSock, 0L, 0) ;
  if( s2 == INVALID_SOCKET )
    return false ;

  closesocket(listeningSock) ;

  fds[1] = s ;
  fds[0] = s2 ;

  return true ;
}


/*
 * $Log: win32socketpair.cc,v $
 * Revision 1.4  2003/05/06 12:35:40  aepage
 * change Win32SocketPair to C binding for possible use in net-snmp native lib
 *
 * Revision 1.3  2003/02/27 02:36:19  aepage
 * added necessary htons call.
 *
 * Revision 1.2  2003/02/25 15:28:08  aepage
 * fix for error caught by compiler.
 *
 * Revision 1.1  2003/02/25 15:23:17  aepage
 * Initial checkin.
 *
 */
