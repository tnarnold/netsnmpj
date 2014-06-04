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
#endif

#include <jni_proxy.hh>
#include "NetSNMPjSessionStruct.hh"
#include <JNISupport.hh>

jclass_proxy SNMPVersion(CLASS("SNMPVersion")) ;
jclass_proxy NetSNMP(CLASS("NetSNMP")) ;
jclass_proxy NetSNMPSession(CLASS("NetSNMPSession")) ;
jstaticmethodID_proxy NSS_finalizeNatives(NetSNMPSession, "finalizeNatives", "()V") ;

jclass_proxy SNMPSendError(CLASS("NetSNMPSendError")) ;
jmethodID_proxy SNMPSendError_ctor(SNMPSendError, "<init>", "(IILjava/lang/String;)V") ;

jclass_proxy SNMPErrTooBig(CLASS("NetSNMPErrTooBig")) ;
jmethodID_proxy SNMPErrTooBig_ctor(SNMPErrTooBig, "<init>", "()V") ;



jclass_proxy readInterruptClass(CLASS("NetSNMPSession$readInterrupt")) ;


jclass_proxy SNMPEventListenerClass("org/netsnmp/NetSNMPAction") ;

jfieldID_proxy sessionHandleFieldID(NetSNMPSession, "sessionHandle", "J"), GetListenersFieldID(NetSNMPSession, "listeners", "[Lorg/netsnmp/NetSNMPAction;") ;
jfieldID_proxy sessionPeerField(NetSNMPSession, "peerName", "Ljava/lang/String;") ;
jfieldID_proxy sessionCommunityField(NetSNMPSession, "community",  "Ljava/lang/String;") ;

jmethodID_proxy ListenerActionMethod(SNMPEventListenerClass, "actionPerformed", "(ILorg/netsnmp/NetSNMPSession;Lorg/netsnmp/PDU;Ljava/lang/Object;)Z") ;


/*
 * Apparently for some reason that we do not understand yet, a static field
 * under certain conditions needs to be loaded from the class that it is declared
 * in.  These fields also exist in the NetSNMP class as copies of the reference that
 * is created in the SNMPVersion class.  However, when loading the library from
 * some classes, the lookup of the fields in those locations fails.  
 */
jobject_static_field_proxy SNMPv3Enum(SNMPVersion, "v3", "Lorg/netsnmp/SNMPVersion;") ;
jobject_static_field_proxy SNMPv2cEnum(SNMPVersion, "v2c", "Lorg/netsnmp/SNMPVersion;") ;
jobject_static_field_proxy SNMPv1Enum(SNMPVersion, "v1", "Lorg/netsnmp/SNMPVersion;") ;
jfieldID_proxy SNMPVersionID(SNMPVersion, "version", "I") ;

jclass_proxy SecurityLevel("org/netsnmp/SecurityLevel") ;
jfieldID_proxy SecurityLevelID(SecurityLevel, "level", "I") ;
jobject_static_field_proxy noAuth(SecurityLevel, "noAuth", "Lorg/netsnmp/SecurityLevel;") ;
jobject_static_field_proxy authNoPriv(SecurityLevel, "authNoPriv", "Lorg/netsnmp/SecurityLevel;") ;
jobject_static_field_proxy authPriv(SecurityLevel, "authPriv", "Lorg/netsnmp/SecurityLevel;") ;

jfieldID_proxy authProtocolFieldID(NetSNMPSession, "securityAuthOID", "Lorg/netsnmp/OID;") ;
jfieldID_proxy privProtocolFieldID(NetSNMPSession, "securityPrivOID", "Lorg/netsnmp/OID;") ;

jobject_static_field_proxy readLockField(NetSNMPSession, "readLock", "Ljava/lang/Object;") ;
jobject_static_field_proxy intAckField(NetSNMPSession, "interruptAcknowledge", "Ljava/lang/Object;") ;


/*
 * Fields for the netsnmp session
 */

// NSS => NetSNMPSession 

jfieldID_proxy NSS_version(NetSNMPSession, "version", "Lorg/netsnmp/SNMPVersion;") ;
jfieldID_proxy NSS_retries(NetSNMPSession, "retries", "I") ;
jfieldID_proxy NSS_timeout(NetSNMPSession, "timeout", "J") ;
jfieldID_proxy NSS_peerName(NetSNMPSession, "peerName", "Ljava/lang/String;") ;
jfieldID_proxy NSS_community(NetSNMPSession, "community", "Ljava/lang/String;") ;
jfieldID_proxy NSS_rcvMsgMaxSize(NetSNMPSession, "rcvMsgMaxSize", "J") ;
jfieldID_proxy NSS_sndMsgMaxSize(NetSNMPSession, "sndMsgMaxSize", "J") ;
jfieldID_proxy NSS_contextName(NetSNMPSession, "contextName", "Ljava/lang/String;") ;
jfieldID_proxy NSS_contextEngineID(NetSNMPSession, "contextEngineID", "[B") ;
jfieldID_proxy NSS_securityEngineID(NetSNMPSession, "securityEngineID", "[B") ;
jfieldID_proxy NSS_securityName(NetSNMPSession, "securityName", "Ljava/lang/String;") ;
jfieldID_proxy NSS_securityAuthOID(NetSNMPSession, "securityAuthOID", "Lorg/netsnmp/OID;") ;

jfieldID_proxy NSS_authPassword(NetSNMPSession, "authPassword", "Ljava/lang/String;") ;
jfieldID_proxy NSS_privPassword(NetSNMPSession, "privPassword", "Ljava/lang/String;") ;
jfieldID_proxy NSS_securityAuthKey(NetSNMPSession, "securityAuthKey", "[B") ;
jfieldID_proxy NSS_securityPrivKey(NetSNMPSession, "securityPrivKey", "[B") ;
jfieldID_proxy NSS_securityModel(NetSNMPSession, "securityModel", "I") ;
jfieldID_proxy NSS_securityLevel(NetSNMPSession, "securityLevel", "Lorg/netsnmp/SecurityLevel;") ;
jfieldID_proxy NSS_isOpen(NetSNMPSession, "isOpen", "Z") ;
jfieldID_proxy NSS_securityPrivOID(NetSNMPSession, "securityPrivOID", "Lorg/netsnmp/OID;") ;


/*
 * Classes for agentx support
 */
jclass_proxy SET_ACTION("org/netsnmp/agentx/AgentX$SET_ACTION") ;
jclass_proxy SET_COMMIT("org/netsnmp/agentx/AgentX$SET_COMMIT") ;
jclass_proxy SET_FREE("org/netsnmp/agentx/AgentX$SET_FREE") ;
jclass_proxy SET_RESERVE1("org/netsnmp/agentx/AgentX$SET_RESERVE1") ;
jclass_proxy SET_RESERVE2("org/netsnmp/agentx/AgentX$SET_RESERVE2") ;
jclass_proxy SET_UNDO("org/netsnmp/agentx/AgentX$SET_UNDO") ;
jclass_proxy GET("org/netsnmp/agentx/AgentX$GET") ;
jclass_proxy GETNEXT("org/netsnmp/agentx/AgentX$GETNEXT") ;
jclass_proxy GETBULK("org/netsnmp/agentx/AgentX$GETBULK") ;

jmethodID_proxy SET_ACTION_method(SET_ACTION, "SET_ACTION", "(Lorg/netsnmp/OID;Lorg/netsnmp/ASN/ASNValue;)Z") ;
jmethodID_proxy SET_COMMIT_method(SET_COMMIT, "SET_COMMIT", "(Lorg/netsnmp/OID;)Z") ;
jmethodID_proxy SET_FREE_method(SET_FREE, "SET_FREE", "(Lorg/netsnmp/OID;)Z") ;
jmethodID_proxy SET_RESERVE1_method(SET_RESERVE1, "SET_RESERVE1", "(Lorg/netsnmp/OID;Lorg/netsnmp/ASN_TYPE;)Z") ;
jmethodID_proxy SET_RESERVE2_method(SET_RESERVE2, "SET_RESERVE2", "(Lorg/netsnmp/OID;)Z") ;
jmethodID_proxy SET_UNDO_method(SET_UNDO, "SET_UNDO", "(Lorg/netsnmp/OID;)Z") ;
jmethodID_proxy GET_method(GET, "GET", "(Lorg/netsnmp/OID;)Lorg/netsnmp/ASN/ASNValue;") ;
jmethodID_proxy GETNEXT_method(GETNEXT, "GETNEXT", "(Lorg/netsnmp/OID;)Lorg/netsnmp/ASN/ASNValue;") ;
jmethodID_proxy GETBULK_method(GETBULK, "GETBULK", "(Lorg/netsnmp/OID;)Lorg/netsnmp/ASN/ASNValue;") ;


/*
 * $Log: NetSNMPjClasses.cc,v $
 * Revision 1.20  2003/06/08 00:57:59  aepage
 * agentX fixes and a new finalizer scheme for sessions
 *
 * Revision 1.19  2003/06/07 15:57:22  aepage
 * AgentX fixes
 *
 * Revision 1.18  2003/05/03 23:37:30  aepage
 * changes to support agentX subagents
 *
 * Revision 1.17  2003/04/27 11:43:27  aepage
 * Added isClosing static field for synchronized closing of sessions.
 *
 * Revision 1.16  2003/04/25 23:05:17  aepage
 * fixes from win32 compilation
 *
 * Revision 1.15  2003/04/23 17:28:35  aepage
 * Fix to 'close' operation that tightens up the locking operations of
 * the nativeThread.  This corrects some 'bad file' errors on select for
 * Solaris that were not occurring under Linux
 *
 * Revision 1.14  2003/04/18 01:44:28  aepage
 * fields to support SNMPv3
 *
 * Revision 1.13  2003/04/15 19:29:15  aepage
 * migration of some basic classes to JNISupport and SNMPv3 Support
 *
 * Revision 1.12  2003/04/11 19:24:54  aepage
 * additional fields
 *
 * Revision 1.11  2003/03/30 22:02:30  aepage
 * fixes for win32
 *
 * Revision 1.10  2003/03/29 00:13:38  aepage
 * new thread architecture
 *
 * Revision 1.9  2003/03/24 13:28:15  aepage
 * added java.lang.Object
 *
 * Revision 1.8  2003/03/21 15:19:42  aepage
 * Migration issues for  JNISupport Module
 *
 * Revision 1.7  2003/03/17 16:11:46  aepage
 * added ioException class
 *
 * Revision 1.6  2003/02/27 17:25:19  aepage
 * Additional PDU Command classes and fields in prepartion for
 * Agent sessions.
 *
 * Revision 1.5  2003/02/24 00:05:20  aepage
 * New interrupt scheme that should work for both Win32 and Unix/Linux.
 *
 * Revision 1.4  2003/02/19 19:18:45  aepage
 * added test for GETBULK.  Added class proxy for NetSNMPSession
 * to ensure that the class is loaded.
 *
 * Revision 1.3  2003/02/15 18:01:29  aepage
 * support for use of a typesafe enum to specify the version of snmp to
 * use.
 *
 * Revision 1.2  2003/02/14 13:57:45  aepage
 * Reordering of the loading order of some of the classes.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.5  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
