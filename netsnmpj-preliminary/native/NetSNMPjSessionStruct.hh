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

#ifndef _H_SESSIONSTRUCT
#define _H_SESSIONSTRUCT

#include <jni.h>
#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>
#include <net-snmp/mib_api.h>
#include <net-snmp/library/snmp_client.h>



/**
 * Exception class used to end a read
 */
class SNMPEndReading {
public:
  SNMPEndReading() {}
} ;

class NullNewObject {
public:
  NullNewObject() {}
} ;


/*
 * Private information for each session
 */
struct NetSNMPjSessionStruct {
  netsnmp_session sessData, *sessp ;
  bool isOpen ;
} ;


struct callback_struct {
  jobject jsession ;
  jobject obj ;
} ;


/*
 * Prototypes
 */
void setSessionFields(JNIEnv *env, netsnmp_session *sessp, jobject jsession) ;
void getSessionFields(JNIEnv *env, netsnmp_session *sessp, jobject jsession)  ;

void internalRead(JNIEnv *env, jclass, jdouble timeout_secs, jint nReads) ;
struct NetSNMPjSessionStruct *GetSessionStruct(struct JNIEnv_ *env, class _jobject *obj) ;

void init_session_threads(void) ;

bool SetObjectIDs(struct JNIEnv_ *env) ;

void throwNotFound(struct JNIEnv_ *env, jstring msg) ;
jthrowable ThrowableError(struct JNIEnv_ *env, jclass cls, int s_snmp_errno, int s_errno) ;
bool SendInterrupt(struct JNIEnv_ *env) ;
bool ClearInterrupt(struct JNIEnv_ *env) ;
void nativeThreadStart(struct JNIEnv_ *env) ;

int session_callback(int op, netsnmp_session *sess, int reqid,
		     netsnmp_pdu *pdu, void *structPtr) ;


#ifdef WIN32
extern "C" {
  char *winsock_startup(void) ; /* net-snmp library call */
  int Win32SocketPair(int fds[2]) ;
}

#endif


#endif

/*
 * $Log: NetSNMPjSessionStruct.hh,v $
 * Revision 1.13  2003/06/08 00:57:59  aepage
 * agentX fixes and a new finalizer scheme for sessions
 *
 * Revision 1.12  2003/05/06 12:35:40  aepage
 * change Win32SocketPair to C binding for possible use in net-snmp native lib
 *
 * Revision 1.11  2003/04/30 00:48:03  aepage
 * attempt to threadify access to snmp_api error strings.
 *
 * Revision 1.10  2003/04/18 01:43:46  aepage
 * migration of fields to java properties and SNMPv3 support
 *
 * Revision 1.9  2003/03/31 17:43:58  aepage
 * fixes for shared lib issues
 *
 * Revision 1.8  2003/03/29 00:13:38  aepage
 * new thread architecture
 *
 * Revision 1.7  2003/03/21 15:24:26  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.6  2003/03/17 16:14:07  aepage
 * changes to JNIEnv_ forward declartion and increased comments for some
 * core functions.  Also added new java to java and native to java object
 * supporting functions.
 *
 * Revision 1.5  2003/02/27 17:45:34  aepage
 * removed free field.  A different structure will be used for agent
 * sessions.
 *
 * Revision 1.4  2003/02/27 17:38:21  aepage
 * Moved callback struct into NetSNMPjSessionStruct.hh
 *
 * Revision 1.3  2003/02/25 14:46:40  aepage
 * Added win32 specific prototypes
 *
 * Revision 1.2  2003/02/24 00:05:20  aepage
 * New interrupt scheme that should work for both Win32 and Unix/Linux.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/06 18:10:01  aepage
 * namespace definiation macros
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
