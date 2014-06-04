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
 * @author Andrew E. Page<aepage@users.sourceforge.net>
 */
#ifdef _WIN32
#include <windows.h>
#endif

#include "jni_proxy.hh"
#include "JNISupport.hh"
#include "joid_proxy.hh"
#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include <net-snmp/net-snmp-config.h>
#include <net-snmp/session_api.h>
#include <net-snmp/mib_api.h>
#include <net-snmp/library/snmp_client.h>
#include <net-snmp/library/asn1.h>

#ifdef WIN32
#include <native.h>
typedef unsigned int mode_t ;
#else
#include <strings.h>
#endif

#include <net-snmp/library/system.h> // for get_uptime()

/**
 * Routines to convert from a netsnmp object to a Java object
 * and vice versa
 *
 */

/*
extern jmethodID_proxy ASN_Value_type_method ;
extern jmethodID_proxy ASN_VALUE_toInt, ASN_VALUE_toInt64 ;
extern jmethodID_proxy ASN_VALUE_toOBJECTID, ASN_VALUE_toOctetStr ;
extern jclass_proxy PDUClass, OIDClass, DefaultOIDClass, ASN_Value_class, ASN_OCTET_STR_class ;
extern jmethodID_proxy PDUCtor, OIDCtor, ASN_OCTET_STR_ctor, PDUSetEntries ;

extern jclass_proxy ASN_OCTET_STR_class, ASN_INTEGER_class, ASN_GAUGE_class, ASN_TIMETICKS_class, ASN_COUNTER_class, ASN_OBJECTID_class, ASN_IPADDRESS_class ;
extern jmethodID_proxy ASN_OCTET_STR_ctor, ASN_INTEGER_ctor, ASN_GAUGE_ctor, ASN_TIMETICKS_ctor, ASN_COUNTER_ctor, ASN_OBJECTID_ctor, ASN_IPADDRESS_ctor ;
extern jfieldID_proxy PDUReqID, PDUErrorStatus, PDUErrorIndex ;

extern jobject_static_field_proxy NULL_VALUE_object, NO_SUCH_OBJECT_object, NO_SUCH_INSTANCE_object ;
*/

extern jclass_proxy ioExceptionClass ; // decleared in jthrow

jclass_proxy ASN_Value_class(CLASS("ASN/ASNValue")) ;
jmethodID_proxy ASN_Value_type_method(ASN_Value_class, "asn_type", "()I") ;
jmethodID_proxy ASN_VALUE_toInt(ASN_Value_class, "toInt", "()I") ;
jmethodID_proxy ASN_VALUE_toInt64(ASN_Value_class, "toInt64", "()J") ;
jmethodID_proxy ASN_VALUE_toOBJECTID(ASN_Value_class, "toOBJECTID", "()[I") ;
jmethodID_proxy ASN_VALUE_toOctetStr(ASN_Value_class, "toOctetString", "()[B") ;

jclass_proxy PDUClass(CLASS("PDU")) ;
jclass_proxy PDUCommand(CLASS("PDU$PDU_COMMAND")) ;
jmethodID_proxy PDUCtor(PDUClass, "<init>", "(Lorg/netsnmp/PDU$PDU_COMMAND;)V") ;
jmethodID_proxy PDUDefaultCtor(PDUClass, "<init>", "()V") ;
jfieldID_proxy PDUReqID(PDUClass, "requestID", "I") ;
jfieldID_proxy PDUCommandField(PDUClass, "type", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jfieldID_proxy PDUErrorStatus(PDUClass, "errStatus", "I") ;
jfieldID_proxy PDUErrorIndex(PDUClass, "errIndex", "I") ;
jfieldID_proxy PDUMaxRepetitions(PDUClass, "maxRepetitions", "I") ;
jfieldID_proxy PDUNonRepeaters(PDUClass, "nonRepeaters", "I") ;

jfieldID_proxy PDUTrapType(PDUClass, "trap_type", "I") ;
jfieldID_proxy PDUSpecificType(PDUClass, "specific_type", "I") ;
jfieldID_proxy PDUEnterprise(PDUClass, "enterprise", "Lorg/netsnmp/OID;") ;
jfieldID_proxy PDUTime(PDUClass, "time", "I") ;
jfieldID_proxy PDUAgentAddr(PDUClass, "agent_addr", "[B") ; // 4 byte array

jclass_proxy PDUEntryClass(CLASS("PDU$entry")) ;
jfieldID_proxy PDUEntryOIDFieldID(PDUEntryClass, "oid", "Lorg/netsnmp/OID;") ;
jfieldID_proxy PDUEntryValFieldID(PDUEntryClass, "value", "Lorg/netsnmp/ASN/ASNValue;") ;
jfieldID_proxy PDUEntriesFieldID(PDUClass, "entries", "[Lorg/netsnmp/PDU$entry;") ;

jfieldID_proxy PDUCommandID(PDUCommand, "id", "I") ;

jmethodID_proxy PDUSetEntries(PDUClass, "setEntries", "([Lorg/netsnmp/OID;[Lorg/netsnmp/ASN/ASNValue;)V") ;
jclass_proxy PDUCommandClass("org/netsnmp/PDU$PDU_COMMAND") ;


jclass_proxy ASN_OCTET_STR_class(CLASS("ASN/OCTET_STR")) ;
jmethodID_proxy ASN_OCTET_STR_ctor(ASN_OCTET_STR_class, "<init>", "([B)V") ;

jclass_proxy ASN_INTEGER_class(CLASS("ASN/INTEGER")) ;
jmethodID_proxy ASN_INTEGER_ctor(ASN_INTEGER_class, "<init>", "(I)V") ;

jclass_proxy ASN_COUNTER_class(CLASS("ASN/COUNTER")) ;
jmethodID_proxy ASN_COUNTER_ctor(ASN_COUNTER_class, "<init>", "(J)V") ;

jclass_proxy ASN_COUNTER64_class(CLASS("ASN/COUNTER64")) ;
jmethodID_proxy ASN_COUNTER64_ctor(ASN_COUNTER64_class, "<init>", "(J)V") ;

jclass_proxy ASN_TIMETICKS_class(CLASS("ASN/TIMETICKS")) ;
jmethodID_proxy ASN_TIMETICKS_ctor(ASN_TIMETICKS_class, "<init>", "(J)V") ;

jclass_proxy ASN_GAUGE_class(CLASS("ASN/GAUGE")) ;
jmethodID_proxy ASN_GAUGE_ctor(ASN_GAUGE_class, "<init>", "(I)V") ;

jclass_proxy ASN_UNSIGNED_class(CLASS("ASN/UNSIGNED")) ;
jmethodID_proxy ASN_UNSIGNED_ctor(ASN_UNSIGNED_class, "<init>", "(J)V") ;

jclass_proxy ASN_OBJECTID_class(CLASS("ASN/OBJECTID")) ;
jmethodID_proxy ASN_OBJECTID_ctor(ASN_OBJECTID_class, "<init>", "([I)V") ;

jclass_proxy ASN_IPADDRESS_class(CLASS("ASN/IPADDRESS")) ;
jmethodID_proxy ASN_IPADDRESS_ctor(ASN_IPADDRESS_class, "<init>", "([B)V") ;

jclass_proxy ASN_NULL_class(CLASS("ASN/NULL")) ;
jmethodID_proxy ASN_NULL_ctor(ASN_NULL_class, "<init>", "()V") ;
jobject_static_field_proxy NULL_VALUE_object(ASN_NULL_class, "NULL_VALUE", "Lorg/netsnmp/ASN/NULL;") ;

jclass_proxy NO_SUCH_OBJECT_class(CLASS("ASN/NO_SUCH_OBJECT")) ;
jobject_static_field_proxy NO_SUCH_OBJECT_object(NO_SUCH_OBJECT_class, "NO_SUCH_OBJECT", "Lorg/netsnmp/ASN/NO_SUCH_OBJECT;") ;

jclass_proxy NO_SUCH_INSTANCE_class(CLASS("ASN/NO_SUCH_INSTANCE")) ;
jobject_static_field_proxy NO_SUCH_INSTANCE_object(NO_SUCH_INSTANCE_class, "NO_SUCH_INSTANCE", "Lorg/netsnmp/ASN/NO_SUCH_INSTANCE;") ;


jclass_proxy END_OF_MIBVIEW_class(CLASS("ASN/END_OF_MIBVIEW")) ;
jobject_static_field_proxy END_OF_MIBVIEW_object(END_OF_MIBVIEW_class, "END_OF_MIBVIEW", "Lorg/netsnmp/ASN/END_OF_MIBVIEW;") ;

jclass_proxy OIDClass(CLASS("OID")) ;
jclass_proxy DefaultOIDClass(CLASS("DefaultOID")) ;

jmethodID_proxy DefaultOIDCtor(DefaultOIDClass, "<init>", "([I)V") ;
jmethodID_proxy oidsMethod(OIDClass, "oids", "()[I") ;

/**
 *  Convert a java object PDU into a netsnmp_pdu
 */
netsnmp_pdu *jobject_to_pdu(struct JNIEnv_ *env, class _jobject *jpdu, int requestID)
{
  netsnmp_pdu *pdu ;
  jobjectArray entries ;
  joid_proxy joids(env) ;
  jobject entry, oid_j, value_j, pdu_type, obj ;
  jsize i, n ;
  joid_proxy jobject_id(env) ;
  joid_proxy enterprise(env) ;
  byteArray_proxy valueData(env) ;
  
  int cmd_type ;

  pdu_type = env->GetObjectField(jpdu, PDUCommandField) ;

  cmd_type = env->GetIntField(pdu_type, PDUCommandID) ;
  pdu = snmp_pdu_create(cmd_type) ;
  assert(pdu) ;

  pdu->reqid = env->GetIntField(jpdu, PDUReqID) ;

  pdu->max_repetitions = env->GetIntField(jpdu, PDUMaxRepetitions) ;
  pdu->non_repeaters = env->GetIntField(jpdu, PDUNonRepeaters) ;

  /*
   * Trap Info
   */
  enterprise = env->GetObjectField(jpdu, PDUEnterprise) ;
  pdu->trap_type = env->GetIntField(jpdu, PDUTrapType) ;
  pdu->specific_type = env->GetIntField(jpdu, PDUSpecificType) ;
  pdu->enterprise = (oid *)enterprise.dup() ;
  pdu->enterprise_length = enterprise.getLen() ;
  pdu->time = env->GetIntField(jpdu, PDUTime) ;
  if( pdu->time == 0 )
    pdu->time = get_uptime() ;
  
  obj = env->GetObjectField(jpdu, PDUAgentAddr) ;
  if( obj == 0L ) {
    in_addr_t a  = get_myaddr() ;
    memcpy(pdu->agent_addr, &a, sizeof(pdu->agent_addr)) ;
  }
  else {
    byteArray_proxy agent_addr(env, (jbyteArray)obj) ;
    memcpy(pdu->agent_addr,
	   (const char *)agent_addr,
	   sizeof(pdu->agent_addr) < agent_addr.getLen() ? sizeof(pdu->agent_addr) : agent_addr.getLen()) ;
  }
  

  /*
   * The entries in the PDU
   */

  entries = (jobjectArray)env->GetObjectField(jpdu, PDUEntriesFieldID) ;

  n = env->GetArrayLength(entries) ;

  for( i = 0 ; i < n ; i++ ) {
    entry = env->GetObjectArrayElement(entries, i) ;
    if( !entry )
      return 0L ;

    // oid field

    oid_j = env->GetObjectField(entry, PDUEntryOIDFieldID) ;
    if( !oid_j )
      return 0L ;

    // oids field within oid field

    joids = oid_j ;
    if( env->ExceptionCheck() )
      return 0L ;

    // SNMPValue type
    value_j = env->GetObjectField(entry, PDUEntryValFieldID) ;

    if( !setNativeValue(env, value_j, oid_j, pdu, 0L) )
      return 0L ;
  }

  return pdu ;
}


bool setNativeValue(struct JNIEnv_ *env, jobject jasnValue, jobject joid, netsnmp_pdu *pdu, netsnmp_variable_list *var)
{
  int asn_type ;
  u_char *byte_value, single_byte[1] ;
  int byte_len ;
  jlong longValue ;
  jint intValue ;
  oid *object_id_ptr ;
  int object_id_len ;
  joid_proxy jobject_id(env) ;
  intArray_proxy jints(env) ;
  const char *str ;
  joid_proxy oid_proxy(env) ;
  struct counter64 cnt64 ;

  assert(joid || var) ;
  if( joid )
    oid_proxy = joid ;
  else 
    oid_proxy.setOids(env, (int *)var->name, var->name_length) ;

  if( env->ExceptionCheck() )
    return false ;

  byteArray_proxy valueData(env) ;
  if( env->ExceptionCheck() )
    return false ;

  asn_type = env->CallIntMethod(jasnValue, ASN_Value_type_method) ;
  if( env->ExceptionCheck() )
    return false ;

  switch( asn_type ) {
  case ASN_COUNTER64:
    longValue = env->CallLongMethod(jasnValue, ASN_VALUE_toInt64) ;
    if( env->ExceptionCheck() ) 
      return 0L ; // an exception has been thrown
    
    cnt64.low = (u_long)(longValue & 0x0FFFFFFFF) ;
    cnt64.high  = (u_long)((unsigned long long)longValue >> 32) ;
    
    byte_value = (u_char *)&cnt64 ;
    byte_len = sizeof(cnt64) ;
    break ;

  case ASN_TIMETICKS:
  case ASN_COUNTER:
    longValue = env->CallLongMethod(jasnValue, ASN_VALUE_toInt64) ;
    if( env->ExceptionCheck() ) 
      return 0L ; // an exception has been thrown
    intValue = (long)longValue ; // kick it down (no choice for now)
    byte_value = (u_char *)&intValue ;
    byte_len = sizeof(intValue) ;
    break ;

  case ASN_GAUGE:
  case ASN_INTEGER:
    intValue = env->CallIntMethod(jasnValue, ASN_VALUE_toInt) ;
    if( env->ExceptionCheck() ) 
      return 0L ; // an exception has been thrown

    byte_value = (u_char *)&intValue ;
    byte_len = sizeof(intValue) ;
    break ;

  case ASN_IPADDRESS:
  case ASN_OCTET_STR:
    valueData = (jbyteArray)env->CallObjectMethod(jasnValue, ASN_VALUE_toOctetStr) ;
    if( env->ExceptionCheck() ) 
      return 0L ; // an exception has been thrown
    str = valueData ;
    byte_value = (u_char *)str ;
    byte_len = valueData.getLen() ;
    break ;

  case ASN_NULL:
    byte_value = single_byte ; // not going to be encoded, but is at least valid
    byte_len = 0 ;
    break ;

  case ASN_OBJECT_ID:
    jints = (jintArray)env->CallObjectMethod(jasnValue, ASN_VALUE_toOBJECTID) ;
    if( env->ExceptionCheck() ) 
      return 0L; // an exception has been thrown

    object_id_ptr = (oid *)(int *)jints ;
    object_id_len = jints.getLen() ;

    byte_value = (u_char *)object_id_ptr ;
    byte_len = object_id_len * sizeof(oid) ;
    
    break ;

  default:
    env->ThrowNew(ioExceptionClass, "unknown field type") ;
    return 0L ;
  }

  if( pdu && joid )
    if( snmp_pdu_add_variable(pdu, oid_proxy, oid_proxy.getLen(), asn_type, byte_value, byte_len) == 0L )
      return false ;

  if( var ) {
    snmp_set_var_typed_value(var, (u_char)asn_type, byte_value, byte_len) ;
  }

  return true ;
}


jobject pdu_to_jobject(struct JNIEnv_ *env, netsnmp_pdu *pdu)
{
  jobject jpdu, valueObj, oidObj ;
  netsnmp_variable_list *var ;
  jobject param ;
  jobjectArray oidArray ;
  jobjectArray valueArray ;
  int n_variables ;
  jsize idx ;

  param = ClassifyPDUCommand(env, pdu->command) ;
  if( param == 0L)
    return 0L ; // exception is being thrown

  /*
   * Create the PDU object
   */
  jpdu = env->NewObject(PDUClass, PDUCtor, param) ;
  if( jpdu == 0L ) {
    return 0L ;
  }

  /*
   * Set basic fields
   */
  env->SetIntField(jpdu, PDUReqID, pdu->reqid) ;
  env->SetIntField(jpdu, PDUErrorStatus, pdu->errstat) ;
  env->SetIntField(jpdu, PDUErrorIndex, pdu->errindex) ;


  // count the variables ;
  n_variables = 0 ;
  for( var = pdu->variables ; var != 0L ; var = var->next_variable ) 
    n_variables++ ;

  oidArray = env->NewObjectArray(n_variables, OIDClass, NULL) ;
  if( !oidArray )
    return 0L ;

  valueArray = env->NewObjectArray(n_variables, ASN_Value_class, NULL) ;
  if( !valueArray )
    return 0L ;

  for( idx = 0, var = pdu->variables ; var != 0L ; var = var->next_variable, idx++ ) {
    oidObj = newOID(env, var->name, var->name_length) ;

    valueObj = nativeValueToASNValue(env, var) ;
    if( !valueObj )
      return 0L ;
    
    env->SetObjectArrayElement(oidArray, idx, oidObj) ;
    env->SetObjectArrayElement(valueArray, idx, valueObj) ;

  } // for

  /*
   * Set the entries
   */
  env->CallVoidMethod(jpdu, PDUSetEntries, oidArray, valueArray) ;
  if( env->ExceptionCheck() )
    return 0L ;

  return jpdu ;

} // pdu_to_jobject


jobject newOID(struct JNIEnv_ *env, oid *oidPtr, size_t len)
{
  jintArray joidDescr ;
  jobject joid ;

  joidDescr = env->NewIntArray(len) ;
  if( !joidDescr )
    return 0L ;

  env->SetIntArrayRegion(joidDescr, 0, len, (jint *)oidPtr) ;
  
  joid = env->NewObject(DefaultOIDClass, DefaultOIDCtor, joidDescr) ;

  return joid ;
}

jobject nativeValueToASNValue(struct JNIEnv_ *env, netsnmp_variable_list *var)
{
  jobject jasnValue ;
  jbyteArray jbytes ;
  jint intVal ;
  unsigned long counterVal ;
  jlong counter64 ;
  jintArray jobject_id ;

  switch( var->type ) {
  case ASN_COUNTER64:

    counter64 = (jlong)var->val.counter64->high << 32 ;
    counter64 |= var->val.counter64->low ;
    
    jasnValue = env->NewObject(ASN_COUNTER64_class, ASN_COUNTER64_ctor, counter64) ;
    if( !jasnValue )
      return 0L ;
    break ;

  case ASN_OCTET_STR:
    jbytes = env->NewByteArray(var->val_len) ;
    env->SetByteArrayRegion(jbytes, 0, var->val_len, (jbyte *)var->val.string) ;
    /*
     * Construct an OCTET_STR type
     */
    jasnValue = env->NewObject(ASN_OCTET_STR_class, ASN_OCTET_STR_ctor, jbytes) ;
    if( !jasnValue )
      return 0L ;
    break ;

  case ASN_IPADDRESS:
    jbytes = env->NewByteArray(var->val_len) ;
    if( !jbytes )
      return 0L ;
    env->SetByteArrayRegion(jbytes, 0, var->val_len, (jbyte *)var->val.string) ;
    /*
     * Construct an ASN_IPADDRESS type
     */
    jasnValue = env->NewObject(ASN_IPADDRESS_class, ASN_IPADDRESS_ctor, jbytes) ;
    break ;

  case ASN_INTEGER:
    intVal = *var->val.integer ;
    jasnValue = env->NewObject(ASN_INTEGER_class, ASN_INTEGER_ctor, intVal) ;
    break ;

  case ASN_COUNTER:
    counterVal = (unsigned long)*var->val.integer ;
    jasnValue = env->NewObject(ASN_COUNTER_class, ASN_COUNTER_ctor, (int64_t)counterVal) ;
    break ;

  case ASN_TIMETICKS:
    counterVal = (unsigned long)*var->val.integer ;
    jasnValue = env->NewObject(ASN_TIMETICKS_class, ASN_TIMETICKS_ctor, (int64_t)counterVal) ;
    break ;

    // case ASN_UNSIGNED:
  case ASN_GAUGE:
    intVal = *var->val.integer ;
    jasnValue = env->NewObject(ASN_GAUGE_class, ASN_GAUGE_ctor, intVal) ;
    break ;

  case ASN_OBJECT_ID:
    jobject_id = env->NewIntArray(var->val_len/sizeof(oid)) ;

    env->SetIntArrayRegion(jobject_id, 0, var->val_len/sizeof(oid), (jint *)var->val.objid) ;

    jasnValue = env->NewObject(ASN_OBJECTID_class, ASN_OBJECTID_ctor, jobject_id) ;
    break ;

  case SNMP_NOSUCHOBJECT:
    jasnValue = NO_SUCH_OBJECT_object ;
    break ;

  case SNMP_NOSUCHINSTANCE:
    jasnValue = NO_SUCH_INSTANCE_object ;
    break ;

  case SNMP_ENDOFMIBVIEW:
    jasnValue = END_OF_MIBVIEW_object ;
    break ;

  default:
    jasnValue = NULL_VALUE_object ;
    break ;
  }


  
  return jasnValue ;
}

jobject_static_field_proxy SNMP_MSG_GET_param(PDUClass, "SNMP_MSG_GET", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_SET_param(PDUClass, "SNMP_MSG_SET", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_GETBULK_param(PDUClass, "SNMP_MSG_GETBULK", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_GETNEXT_param(PDUClass, "SNMP_MSG_GETNEXT", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;

jobject_static_field_proxy SNMP_MSG_RESPONSE_param(PDUClass, "SNMP_MSG_RESPONSE", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_TRAP_param(PDUClass, "SNMP_MSG_TRAP", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_INFORM_param(PDUClass, "SNMP_MSG_INFORM", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_TRAP2_param(PDUClass, "SNMP_MSG_TRAP2", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;
jobject_static_field_proxy SNMP_MSG_REPORT_param(PDUClass, "SNMP_MSG_REPORT", "Lorg/netsnmp/PDU$PDU_COMMAND;") ;

jclass_proxy NetSNMPException(CLASS("NetSNMPException")) ;

jobject ClassifyPDUCommand(JNIEnv *env, int command)
{
  char buffer[64] ;

  switch( command ) {
  case SNMP_MSG_GET:
    return SNMP_MSG_GET_param ;
  case SNMP_MSG_GETNEXT:
    return SNMP_MSG_GETNEXT_param ;
  case SNMP_MSG_RESPONSE:
    return SNMP_MSG_RESPONSE_param ;
  case SNMP_MSG_SET:
    return SNMP_MSG_SET_param ;
  case SNMP_MSG_TRAP:
    return SNMP_MSG_TRAP_param ;
  case SNMP_MSG_GETBULK:
    return SNMP_MSG_GETBULK_param ;
  case SNMP_MSG_INFORM:
    return SNMP_MSG_INFORM_param ;
  case SNMP_MSG_TRAP2:
    return SNMP_MSG_TRAP2_param ;
  case SNMP_MSG_REPORT:
    return SNMP_MSG_REPORT_param ;
  }

  snprintf(buffer, sizeof(buffer), "unknown command type received(%d)", command) ;
  env->ThrowNew(NetSNMPException, buffer) ;
  return 0L ;
}

jclass_proxy ASN_TYPE_Class("org/netsnmp/ASN_TYPE") ;
jobject_static_field_proxy ASN_BOOLEAN_field(ASN_TYPE_Class, "ASN_BOOLEAN", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_INTEGER_field(ASN_TYPE_Class, "ASN_INTEGER", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_BIT_STR_field(ASN_TYPE_Class, "ASN_BIT_STR", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_OCTET_STR_field(ASN_TYPE_Class, "ASN_OCTET_STR", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_NULL_field(ASN_TYPE_Class, "ASN_NULL", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_OBJECT_ID_field(ASN_TYPE_Class, "ASN_OBJECT_ID", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_SEQUENCE_field(ASN_TYPE_Class, "ASN_SEQUENCE", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_IPADDRESS_field(ASN_TYPE_Class, "ASN_IPADDRESS", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_COUNTER_field(ASN_TYPE_Class, "ASN_COUNTER", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_GAUGE_field(ASN_TYPE_Class, "ASN_GAUGE", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_UNSIGNED_field(ASN_TYPE_Class, "ASN_UNSIGNED", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_TIMETICKS_field(ASN_TYPE_Class, "ASN_TIMETICKS", "Lorg/netsnmp/ASN_TYPE;") ;
jobject_static_field_proxy ASN_COUNTER64_field(ASN_TYPE_Class, "ASN_COUNTER64", "Lorg/netsnmp/ASN_TYPE;") ;


jobject ClassifyASNType(JNIEnv *env, int asn_type)
{
  char buffer[64] ;

  switch( asn_type ) {
  case ASN_BOOLEAN:
    return ASN_BOOLEAN_field ;
  case ASN_INTEGER:
    return ASN_INTEGER_field ;
  case ASN_BIT_STR:
    return ASN_BIT_STR_field ;
  case ASN_OCTET_STR:
    return ASN_OCTET_STR_field ;
  case ASN_NULL:
    return ASN_NULL_field ;
  case ASN_OBJECT_ID:
    return ASN_OBJECT_ID_field ;
  case ASN_SEQUENCE:
    return ASN_SEQUENCE_field ;
  case ASN_IPADDRESS:
    return ASN_IPADDRESS_field ;
  case ASN_COUNTER:
    return ASN_COUNTER_field ;
  case ASN_GAUGE:
    return ASN_GAUGE_field ;
    //case ASN_UNSIGNED:
    // return ASN_UNSIGNED_field ;
  case ASN_TIMETICKS:
    return ASN_TIMETICKS_field ;
  case ASN_COUNTER64:
    return ASN_COUNTER64_field ;
  }

  snprintf(buffer, sizeof(buffer), "unknown asn type (%d)", asn_type) ;
  env->ThrowNew(NetSNMPException, buffer) ;
  return 0L ;
}

/*
 * $Log: pdu_jobject.cc,v $
 * Revision 1.10  2003/06/07 16:01:08  aepage
 * support for 64bit counters
 *
 * Revision 1.9  2003/06/01 17:45:03  aepage
 * support for snmpwalk
 *
 * Revision 1.8  2003/05/03 23:37:31  aepage
 * changes to support agentX subagents
 *
 * Revision 1.7  2003/05/01 15:02:48  aepage
 * include and type correction for windows
 *
 * Revision 1.6  2003/04/29 01:32:47  aepage
 * Added support for sending traps
 *
 * Revision 1.5  2003/04/25 22:55:11  aepage
 * fixes from win32 compilation
 *
 * Revision 1.4  2003/04/18 01:26:36  aepage
 * cast modifications
 *
 * Revision 1.3  2003/03/30 22:02:31  aepage
 * fixes for win32
 *
 * Revision 1.2  2003/03/28 13:40:25  aepage
 * fix for ASNObject conversion
 *
 * Revision 1.1  2003/03/21 14:42:50  aepage
 * Initial checkins
 *
 * Revision 1.8  2003/03/20 00:06:51  aepage
 * removal of deprecated SetProxyEnv macro
 *
 * Revision 1.7  2003/03/17 16:17:58  aepage
 * extraction of some functions to convert native types(oids, ASN
 * varbinds etc) to java objects.
 *
 * Revision 1.6  2003/03/14 16:05:00  aepage
 * migration to use of array proxy utility classes
 *
 * Revision 1.5  2003/03/14 15:10:10  aepage
 * some tune ups to the printing of OIDs
 *
 * Revision 1.4  2003/02/27 17:51:15  aepage
 * pdus are now fully classified by command through a typesafe-enum
 *
 * Revision 1.3  2003/02/22 23:30:40  aepage
 * Win32 Port fixes
 *
 * Revision 1.2  2003/02/19 19:07:10  aepage
 * Support for GETBULK operations
 *
 * Revision 1.1.1.1  2003/02/07 23:56:54  aepage
 * Migration Import
 *
 * Revision 1.5  2003/02/07 22:05:36  aepage
 * culling of classes and fieldid object no longer in use
 *
 * Revision 1.4  2003/02/07 19:22:10  aepage
 * Namespace migrations
 *
 * Revision 1.3  2003/02/05 15:34:52  aepage
 * removal of old include snmp_PDU.h
 *
 * Revision 1.2  2003/02/05 14:23:54  aepage
 * fixes to allow compilation with a 1.3.1 jdk
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
