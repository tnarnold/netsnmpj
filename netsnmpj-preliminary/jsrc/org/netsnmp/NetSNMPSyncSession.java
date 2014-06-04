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


package org.netsnmp;
import java.io.Serializable;

import org.netsnmp.ASN.* ;
import org.netsnmp.NetSNMPSession.serialEncryption;

/**
 * A helper object that implements a synchronous session.  This allows users to dispatch and receive
 * pdus on the same thread.   NOTE:  not recommended for heavy duty work due to the natural asynchronicity
 * of SNMP and network operations in general.  This is meant as a convenience object and as a way
 * for entry level personnel to get their 'feet wet'.  
 * 
 * <pre>
 * NetSNMPSyncSession sess = new NetSNMPSyncSession(host, community) ;
 *	
 * ASNValue response = sess.get(new DefaultOID("SNMPv2-MIB::sysDescr.0")) ;
 *	
 * System.out.println(response);
 * </pre>
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class NetSNMPSyncSession implements Serializable {
	
	private NetSNMPSession sess ;
	
	private static class pduCarrier {
		PDU myPDU ;	
		int result ;
	}
	
	private static class syncListener implements NetSNMPAction, Serializable {
		public boolean actionPerformed(int result, NetSNMPSession s, PDU rpdu, Object o) {
					synchronized( o ) {
						((pduCarrier)o).myPDU = rpdu ;
						((pduCarrier)o).result = result ;
						o.notify() ;
						return false ;
					} // synchronized		
		}
	}
	
	private static syncListener defaultLlistener = new syncListener() ;
	private syncListener listener = defaultLlistener ;
	
	/**
	 * Send a pdu and retrieve the result
	 * @param pdu  Pdu to send to remote agent
	 * @return pdu that the agent replies with
	 * 
	 * @throws java.io.IOException if an IO error occurs on the connection
	 * @throws NetSNMPSendError if an snmp error occurs (such as the pdu being too large)
	 * @throws Interupted exception if the thread is interupted while waiting for the result
	 */
	public PDU send(PDU pdu) throws java.io.IOException, NetSNMPSendError, InterruptedException {
		
		pduCarrier c = new pduCarrier() ;
		
		synchronized ( c ) {
			sess.send(pdu, c) ;
			c.wait() ;
		} // synchronized	
		
		if( c.result == NetSNMP.STAT_TIMEOUT ) {
			throw new java.io.IOException("operation timed out") ;	
		}
		
		return c.myPDU ;
	}
	
	/**
	 *   Sets a single value for a given OID
	 * @param oid Object Identifier for the target object
	 * @param v   Value to set the target object 
	 */
	public ASNValue set(OID oid, ASNValue v) throws java.io.IOException, NetSNMPSendError, InterruptedException  {
		PDU pdu = new PDU(NetSNMP.MSG_SET) ;
		PDU rpdu ;
		pdu.addEntry(oid, v) ;
		
		rpdu = send(pdu) ;
		if( rpdu.errStatus != 0 )
			throw new NetSNMPSendError(rpdu.errStatus, -1, "sync set failed") ;	
		
		return rpdu.entries[0].value ;
	}
	
	/**
	 *  Retrieves a single value for a given OID
	 * 
	 * @param oid Object Identifier of the desired value
	 * 
	 * @throws java.io.IOException if an IO error occurs on the connection
	 * @throws NetSNMPSendError if an snmp error occurs (such as the pdu being too large)
	 * @throws Interupted exception if the thread is interupted while waiting for the result
	 * 
	 */
	public ASNValue get(OID oid) throws java.io.IOException, NetSNMPSendError, InterruptedException {
		PDU pdu = new PDU(NetSNMP.MSG_GET) ;
		pdu.addNullEntry(oid)	;
		
		PDU rpdu = send(pdu) ;
		
		return rpdu.entries[0].value ;
	}
	
	/**
	 * Retrieves a single value for a given string descriptor
	 * @param descriptor for the requested object.  May be in a text form 
	 * such as SNMPv2-MIB::sysDescr.0 or iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0
	 * or a dot separated list of integers such as 1.3.6.1.2.1.1.1.0
	 * 
	 * @throws MIBItemNotFound if the specified descriptor is not in the currently loaded mibs
	 * @throws java.io.IOException if an IO error occurs on the connection
	 * @throws NetSNMPSendError if an snmp error occurs (such as the pdu being too large)
	 * @throws Interupted exception if the thread is interupted while waiting for the result
	 * 
	 * @return value of the object requested
	 */
	public ASNValue get(String descr) throws MIBItemNotFound, java.io.IOException, NetSNMPSendError, InterruptedException {
		return get(new DefaultOID(descr)) ;
	}
	
	/**
	 * Construct an unopened session
	 */
	public NetSNMPSyncSession() {
		sess = new NetSNMPSession() ;	
		sess.addListener(listener) ;
	}
	
	/**
	 *  Construct a new session to the given host and commnunity
	 * @see org.netsnmp.NetSNMPSession(String,String)
	 */
	public NetSNMPSyncSession(String host, String community) {
		sess = new NetSNMPSession(host,community) ;	
		sess.addListener(listener) ;
	}
	
	public NetSNMPSyncSession(NetSNMPSession sess) {
		this.sess = sess ;
		this.sess.addListener(listener) ;	
	}
	
	protected void finalize() throws Throwable {
		sess.deleteListener(listener) ;
		
	}


	

	/**
	 * @return
	 */
	public String getAuthPassword() {
		return sess.getAuthPassword();
	}

	/**
	 * @return
	 */
	public String getCommunity() {
		return sess.getCommunity();
	}

	/**
	 * @return
	 */
	public byte[] getContextEngineID() {
		return sess.getContextEngineID();
	}

	/**
	 * @return
	 */
	public String getContextName() {
		return sess.getContextName();
	}

	/**
	 * @return
	 */
	public String getPeerName() {
		return sess.getPeerName();
	}

	/**
	 * @return
	 */
	public String getPrivPassword() {
		return sess.getPrivPassword();
	}

	/**
	 * @return
	 */
	public long getRcvMsgMaxSize() {
		return sess.getRcvMsgMaxSize();
	}

	/**
	 * @return
	 */
	public int getRetries() {
		return sess.getRetries();
	}

	/**
	 * @return
	 */
	public byte[] getSecurityAuthKey() {
		return sess.getSecurityAuthKey();
	}

	/**
	 * @return
	 */
	public OID getSecurityAuthOID() {
		return sess.getSecurityAuthOID();
	}

	/**
	 * @return
	 */
	public byte[] getSecurityEngineID() {
		return sess.getSecurityEngineID();
	}

	/**
	 * @return
	 */
	public SecurityLevel getSecurityLevel() {
		return sess.getSecurityLevel();
	}

	/**
	 * @return
	 */
	public int getSecurityModel() {
		return sess.getSecurityModel();
	}

	/**
	 * @return
	 */
	public String getSecurityName() {
		return sess.getSecurityName();
	}

	/**
	 * @return
	 */
	public byte[] getSecurityPrivKey() {
		return sess.getSecurityPrivKey();
	}

	/**
	 * @return
	 */
	public OID getSecurityPrivOID() {
		return sess.getSecurityPrivOID();
	}

	/**
	 * @return
	 */
	public long getSndMsgMaxSize() {
		return sess.getSndMsgMaxSize();
	}

	/**
	 * @return
	 */
	public SNMPVersion getSnmpVersion() {
		return sess.getSnmpVersion();
	}

	/**
	 * @return
	 */
	public long getTimeout() {
		return sess.getTimeout();
	}

	/**
	 * @return
	 */
	public SNMPVersion getVersion() {
		return sess.getVersion();
	}

	/**
	 * @return
	 */
	public boolean isOpen() {
		return sess.isOpen();
	}

	/**
	 * @throws IllegalStateException
	 */
	public void open() throws IllegalStateException {
		sess.open();
	}

	/**
	 * @param oid
	 */
	public void setAuthenticationProtocol(OID oid) {
		sess.setAuthenticationProtocol(oid);
	}

	/**
	 * @param string
	 */
	public void setAuthPassword(String string) {
		sess.setAuthPassword(string);
	}

	/**
	 * @param string
	 */
	public void setCommunity(String string) {
		sess.setCommunity(string);
	}

	/**
	 * @param bs
	 */
	public void setContextEngineID(byte[] bs) {
		sess.setContextEngineID(bs);
	}

	/**
	 * @param string
	 */
	public void setContextName(String string) {
		sess.setContextName(string);
	}

	/**
	 * @param string
	 */
	public void setPeerName(String string) {
		sess.setPeerName(string);
	}

	/**
	 * @param oid
	 */
	public void setPrivacyProtocol(OID oid) {
		sess.setPrivacyProtocol(oid);
	}

	/**
	 * @param string
	 */
	public void setPrivPassword(String string) {
		sess.setPrivPassword(string);
	}

	/**
	 * @param l
	 */
	public void setRcvMsgMaxSize(long l) {
		sess.setRcvMsgMaxSize(l);
	}

	/**
	 * @param i
	 */
	public void setRetries(int i) {
		sess.setRetries(i);
	}

	/**
	 * @param bs
	 */
	public void setSecurityAuthKey(byte[] bs) {
		sess.setSecurityAuthKey(bs);
	}

	/**
	 * @param oid
	 */
	public void setSecurityAuthOID(OID oid) {
		sess.setSecurityAuthOID(oid);
	}

	/**
	 * @param bs
	 */
	public void setSecurityEngineID(byte[] bs) {
		sess.setSecurityEngineID(bs);
	}

	/**
	 * @param level
	 */
	public void setSecurityLevel(SecurityLevel level) {
		sess.setSecurityLevel(level);
	}

	/**
	 * @param i
	 */
	public void setSecurityModel(int i) {
		sess.setSecurityModel(i);
	}

	/**
	 * @param string
	 */
	public void setSecurityName(String string) {
		sess.setSecurityName(string);
	}

	/**
	 * @param bs
	 */
	public void setSecurityPrivKey(byte[] bs) {
		sess.setSecurityPrivKey(bs);
	}

	/**
	 * @param oid
	 */
	public void setSecurityPrivOID(OID oid) {
		sess.setSecurityPrivOID(oid);
	}

	/**
	 * @param l
	 */
	public void setSndMsgMaxSize(long l) {
		sess.setSndMsgMaxSize(l);
	}

	/**
	 * @param version
	 */
	public void setSNMPVersion(SNMPVersion version) {
		sess.setSNMPVersion(version);
	}

	/**
	 * @param l
	 */
	public void setTimeout(long l) {
		sess.setTimeout(l);
	}

	/**
	 * @param encryption
	 */
	public void setSerialEncryptor(serialEncryption encryption) {
		sess.setSerialEncryptor(encryption);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return sess.hashCode();
	}

}


/*
* $Log: NetSNMPSyncSession.java,v $
* Revision 1.5  2003/04/25 23:12:35  aepage
* fixes from eclipse on win32
*
* Revision 1.4  2003/04/19 01:44:00  aepage
* session methods delegated and class serialized
*
* Revision 1.3  2003/04/15 19:38:13  aepage
* support for SET operations
*
* Revision 1.2  2003/03/30 22:59:44  aepage
* removal of unneeded imports
*
* Revision 1.1  2003/03/28 23:58:37  aepage
* A session that works synchronously.  Useful for quick scripts,
* experiments, teaching and other worthy endevors.
*
*/
