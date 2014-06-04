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
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
package org.netsnmp;

import java.util.Map;
import java.util.StringTokenizer;

import org.netsnmp.agentx.AgentX;
import org.netsnmp.util.HostLister;


/**
 *  The NetSNMP class contains useful fields and methods.  Similar to the
 *   java.lang.System class.
 */
public class NetSNMP
{
	/**
	 *  internal class that will keep a simple list of hosts
	 * Contains a list of hosts taken from a comma separated list
	 * of hosts contained in the property org.netsnmp.hosts
	 * 
	 */
	private static class simpleHostList implements HostLister {
		static String hosts[] ;
		
		public String [] hosts() { return hosts ; }	
		
		static {
			String hostListString = System.getProperty("org.netsnmp.hosts", "localhost") ;
			StringTokenizer tokenizer = new StringTokenizer(hostListString, ",") ;
			int i ;
			
			hosts = new String[tokenizer.countTokens()] ;
			
			i = 0 ;
			while( tokenizer.hasMoreTokens() ) 
				hosts[i++] = tokenizer.nextToken() ;
		}
		
	}
	 public static final SecurityLevel authNoPriv = SecurityLevel.authNoPriv ;
	 public static final SecurityLevel authPriv   = SecurityLevel.authPriv ;
	
	public static HostLister defaultHostList = new simpleHostList() ;
	 private static int [] desPrivInts  = { 1,3,6,1,6,3,10,1,2,2 } ;
	 private static int [] md5Auth5Ints = { 1,3,6,1,6,3,10,1,1,2 } ;
	
	
	/**
	 * Command type to construct pdu to be sent to a remote agent to perform
	 * a GET operation.  (i.e. sending a pdu to a remote agent in order to have
	 * it send back a pdu containing information)
	 */
	public static final PDU.PDU_COMMAND MSG_GET = PDU.SNMP_MSG_GET ;
	
	/**
	 * Command type to construct a pdu to be sent to a remote agent to perform
	 * a GETBULK operation(i.e. large request is made to an agent, and the agent
	 * can reply with multiple PDUs instead of one large one.)
	 */
	public static final PDU.PDU_COMMAND MSG_GETBULK = PDU.SNMP_MSG_GETBULK ;
	
	/**
	 * Command type to construct a pdu to be sent to a remote agent to perform
	 * a GETNEXT operation (i.e. sending a pdu to remote agent in order to retrieve
	 * the next object from the object(s) contained in the pdu.
	 */
	public static final PDU.PDU_COMMAND MSG_GETNEXT = PDU.SNMP_MSG_GETNEXT ;
	public static final PDU.PDU_COMMAND MSG_INFORM = PDU.SNMP_MSG_INFORM ;
	public static final PDU.PDU_COMMAND MSG_REPORT = PDU.SNMP_MSG_REPORT ;
	
	public static final PDU.PDU_COMMAND MSG_RESPONSE = PDU.SNMP_MSG_RESPONSE ;
	/**
	 * Command type to construct a pdu to be sent to a remote agent to perform
	 * a SET operation.  (i.e. sending a pdu to a remote agent to set values
	 * internally).
	 *<BR><BR>
	 *   NOTE:  SNMPv2 and above compliant agents may require extra privelages
	 * in order to successfully perform this operation.  In addtion not all fields
	 * have write access, and agent configurations can restrict write access to
	 * certain OID trees while granting others.  Consult the documentation
	 * for the remote agent.
	 *
	 *  @see org.netsnmp.PDU#PDU(PDU.PDU_COMMAND)
	 *  @see man(5) snmpd.conf
	 */
	public static final PDU.PDU_COMMAND MSG_SET = PDU.SNMP_MSG_SET ;
	public static final PDU.PDU_COMMAND MSG_TRAP = PDU.SNMP_MSG_TRAP ;
	public static final PDU.PDU_COMMAND MSG_TRAP2 = PDU.SNMP_MSG_TRAP2 ;
	
	/*
	 * Security Levels
	 */ 
	 public static final SecurityLevel noAuth = SecurityLevel.noAuth ;
	 
	 /*
	  * Security OIDs
	  */
	  
	 // authentication
	 private static int [] noAuthInts   = { 1,3,6,1,6,3,10,1,1,1 } ;
	 
	 // privacy
	 private static int [] noPrivInts   = { 1,3,6,1,6,3,10,1,2,1 } ;
	 private static int [] shaAuthInts  = { 1,3,6,1,6,3,10,1,1,3 } ;
	
	/**
	 *  SNMPVersion object to use to request a SNMP Version 1 session
	 *  <pre>
	 *  NetSNMPSession sess = new NetSNMPSession() ;
	 *     sess.{@link org.netsnmp.NetSNMPSession#setVersion setSNMPVersion}(NetSNMP.SNMPv1) ;
	 *     sess.setPeerName("localhost") ;
	 *     sess.setCommunityName("public") ;
	 *     sess.open() ;
	 * </pre>
	 */
	public static final SNMPVersion SNMPv1 = SNMPVersion.v1 ;
	/**
	 *  SNMPVersion object to use to request a SNMP Version 2c session
	 */
	public static final SNMPVersion SNMPv2c = SNMPVersion.v2c ;
	
	/**
	 *  SNMPVersion object to use to request a SNMP Version 3 session
	 */
	public static final SNMPVersion SNMPv3  = SNMPVersion.v3 ;
	
	/**
	 * Result code indicating that there was no response from the agent
	 * TDB transfer this to a type-safe enum
	 */
	public static final int STAT_TIMEOUT = 2 ;
	 public static final OID usmDESPrivOID = new DefaultOID(desPrivInts) ; 
	 public static final OID usmMD5AuthOID = new DefaultOID(md5Auth5Ints) ;
	 
	 public static final OID usmNoAuthOID  = new DefaultOID(noAuthInts) ;
	 
	 public static final OID usmNoPrivOID  = new DefaultOID(noPrivInts) ;
	 public static final OID usmSHAAuthOID = new DefaultOID(shaAuthInts) ;
	private NetSNMP() {
		// never meant to be instantiated
	}
	
	/**
	 * Enables net-snmp stderr debugging
	 */
	public static native void doDebugging(boolean flag) ;
	
	/**
	 * Method enableStderrLogging
	 *  Enables or disables the stderrlogging Equivalent to the netsnmplib
	 * calls snmp_disable_stderrlog and snmp_enable_stderrlog
	 *
	 * @param    flag whether or not to enable stderr logging
	 *
	 */
	public static native void enableStderrLogging(boolean flag) ;
	
	/**
	 * Retrieves the version of net-snmp that the native library is
	 * is using.
	 */
	
	public static native String getNetSNMPVersion() ;
	
	/**
	 * Retrieves the netsnmpj release name
	 */
	public static String getReleaseVersion() {
		return "$Name:  $" ; // CVS tag
	}
	
	/**
	 * 
	 * @param portSpec hostname, protocol:hostname or protocol:host:portNum
	 * @param pingInterval time in seconds between subagent 'pings' to master agent
	 */
	public static native void setAgentXSocket(String portSpec, int pingInterval) ;
	
	public static void setAgentXSocket(String portSpec) {
		setAgentXSocket(portSpec, 0) ;
	}
	
	/**
	 * @param oid objectIdentifier or Tree identifier to monitor
	 * @param agentObject object to handle agent requests
	 * @param flags bitmasked flags for controlling read access 
	 * @return any previously registered agent
	 * @see org.netsnmp.agentx.AgentX#RWRITE
	 * @throws IllegalStateException if the agentX socket has not been set 
	 * @see org.netsnmp.NetSNMP#setAgentXSocket
	 */
	public static native AgentX registerAgentX(OID oid, AgentX agentObject, int flags) throws IllegalStateException ;	
	
	/**
	 * Unregisters an agent at OID
	 * @param oid the OID
	 * @return the AgentX registered at this location
	 */
	public static native AgentX unregisterAgentX(OID oid) ;
	
	private static Map registeredAgents ; // manipulated by native routines
	
	static {
		 Warnings.loadNativeLibraryOrProvideWarning("netsnmpj") ;
	 }
}

/*
 * $Log: NetSNMP.java,v $
 * Revision 1.15  2003/05/04 21:38:49  aepage
 * support for subagents
 *
 * Revision 1.14  2003/05/03 23:38:49  aepage
 * support for agentX subagents
 *
 * Revision 1.13  2003/04/30 14:18:32  aepage
 * doc fixes and member sorting
 *
 * Revision 1.12  2003/04/15 19:23:11  aepage
 * SNMPv3 Support
 *
 * Revision 1.11  2003/03/28 23:47:00  aepage
 * class and modification to support a list of hosts that will be used
 * for defaults in UI tools
 *
 * Revision 1.10  2003/03/19 15:49:22  aepage
 * library loading done through Warnings class.  This allows the output a
 * common tutorial message to correct common library issues.
 *
 * Revision 1.9  2003/03/17 16:11:14  aepage
 * added doDebugging method and now using jthrow faclilities.
 *
 * Revision 1.8  2003/03/02 18:23:30  aepage
 * added version property and added to test suites.
 *
 * Revision 1.7  2003/03/01 01:42:26  aepage
 * Added support to get the version of net-snmp is use with this library.
 *
 * Revision 1.6  2003/02/27 17:33:49  aepage
 * Moved loadLibary call to bottom of class definition.  Added PDU
 * Commands in preparation for agent sessions.
 *
 * Revision 1.5  2003/02/19 19:07:11  aepage
 * Support for GETBULK operations
 *
 * Revision 1.4  2003/02/15 19:54:04  aepage
 * doc comments
 *
 * Revision 1.3  2003/02/15 18:04:26  aepage
 * support for use of a typesafe enum to specify the version of snmp to use
 *
 * Revision 1.2  2003/02/09 22:36:47  aepage
 * Added STAT_TIMEOUT constant
 *
 * Revision 1.1.1.1  2003/02/07 23:56:49  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
