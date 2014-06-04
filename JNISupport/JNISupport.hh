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
#ifndef _H_JNISUPPORT
#define _H_JNISUPPORT

#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>
#include <net-snmp/mib_api.h>
#include <net-snmp/library/snmp_client.h>

/*
 * macros for determining class
 */
#define PACKAGE "org/netsnmp"
#define CLASS(s) PACKAGE"/"s


#ifdef __cplusplus
extern "C" {
#endif

  class _jobject *pdu_to_jobject(struct JNIEnv_ *env, netsnmp_pdu *pdu) ;

  netsnmp_pdu *jobject_to_pdu(struct JNIEnv_ *env, class _jobject *, int reqID) ;

  /**
   * Converts a java ASNValue object to a variable list entry or a pdu entry
   * @return true if value was successfully converted.  False otherwise
   */
  bool setNativeValue(struct JNIEnv_ *env, jobject jasnValue, jobject joid, netsnmp_pdu *pdu, netsnmp_variable_list *var) ;

  jobject ClassifyPDUCommand(struct JNIEnv_ *env, int command) ;
  jobject ClassifyASNType(JNIEnv *env, int asn_type) ;

  /**
   * Construct a new OID object from a native oid array
   */
  jobject newOID(struct JNIEnv_ *env, oid *oidPtr, size_t len) ;

/**
 * Creates an ASNValue object from a native variable
 *
 * @return an ASNValue object
 */
jobject nativeValueToASNValue(struct JNIEnv_ *env, netsnmp_variable_list *var) ;JNIEnv *GetJNIEnv() ;

JNIEnv *GetJNIEnv() ;

  jthrowable newThrowable(struct JNIEnv_ *env, jclass clazz, const char *fmt, ...) ;

#ifdef __cplusplus
}
#endif

#endif

/*
 * $Log: JNISupport.hh,v $
 * Revision 1.4  2003/05/04 21:32:46  aepage
 * fixes and extension of use of the JEXCEPTION_CHECK macro
 *
 * Revision 1.3  2003/04/18 01:22:31  aepage
 * functionality for better exception handling.
 *
 * Revision 1.2  2003/03/28 23:43:59  aepage
 * Added GetEnv function for attaching to native threads.
 *
 */
