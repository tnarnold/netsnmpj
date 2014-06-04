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



/*
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */


package org.netsnmp.junittests;

import org.netsnmp.* ;
import org.netsnmp.util.* ;
import org.netsnmp.ASN.* ;
import junit.framework.* ;

public class setTests extends TestCase {
	
	static String setResult ;
	static NetSNMPAction setListener = new NetSNMPAction() {
		public synchronized boolean actionPerformed(int result, NetSNMPSession session, PDU pdu, Object o) throws Throwable {
			
			//System.out.println("got result = " + result + " pdu = " + pdu);
			//System.out.println("err = " + pdu.errStatus + "/" + pdu.errIndex) ;
			
			setResult = "" + pdu.errIndex + "/" + pdu.errStatus ;
			
			notify() ;
			return false ;
		}
		
	} ;
	
	static String setValue(NetSNMPSession s, OID oid, ASNValue v) throws Throwable {
		PDU pdu = new PDU(NetSNMP.MSG_SET) ;
		s.addListener(setListener) ;
		
		pdu.addEntry(oid, v) ;
		
		synchronized( setListener ) {
			s.send(pdu, null) ;
			setListener.wait() ;
		} // synchronized
		
		s.deleteListener(setListener) ;
		return setResult ;
	}
	
	static final String testHost = TestProperties.agent ;
	static final String testCommunity = TestProperties.community ;
	static final String testCommunity_ro = TestProperties.community_ro ; // read only community
	static final OID sysLocation = MIB.readObjIDOrAbort("SNMPv2-MIB::sysLocation.0") ;
	static final OID sysContact = MIB.readObjIDOrAbort("SNMPv2-MIB::sysContact.0") ;
	
	static final String setString = "bogus location set by setTests junit object please reset" ;
	static snmpgetRunner runner = new snmpgetRunner(testHost, testCommunity) ;
	
	boolean testDone ;

	public void testSet() throws Throwable {
		NetSNMPSession s = new NetSNMPSession(testHost, testCommunity) ;
        
		String originalLocation = runner.getString(sysLocation) ;
		String newLocation ;
		String setResult ;
		
		/*
		 * this assertion will fail if there's left over data from a failed test
		 */
		assertFalse(originalLocation.equals(setString)) ;

		setResult = setValue(s, sysLocation, new OCTET_STR(setString.getBytes())) ;
		assertTrue(setResult.equals("0/0")) ;
		
		newLocation = runner.getString(sysLocation) ;
		
		assertTrue(newLocation.equals(setString)) ;
		
		setResult = setValue(s, sysLocation, new OCTET_STR(originalLocation.getBytes())) ;
		assertTrue(setResult.equals("0/0")) ;
		
		newLocation = runner.getString(sysLocation) ;
		
		assertTrue(newLocation.equals(originalLocation)) ;
		
	}
	
	/**
	 * Tests that we get a proper result back for trying to set an object
	 * that is not writable
	 */
	public void testSetNonWritable() throws Throwable {
		NetSNMPSession s = new NetSNMPSession() ;
		String newContact ;
		String setResult ;
		
		s.setPeerName(testHost) ;
		s.setCommunity(testCommunity) ;
		s.setSNMPVersion(NetSNMP.SNMPv2c) ;
		s.open() ;
		
		setResult = setValue(s, sysContact, new OCTET_STR(setString)) ;
		
		/*
		 * we should see a failure code of 17(SNMP_ERR_NOTWRITABLE) on the first value
		 */
		assertTrue(setResult.equals("1/17")) ;
		
		newContact = runner.getString(sysContact) ;
		
		assertFalse(newContact.equals(setString)) ; // check to make sure it didn't set
		
	}
	
	/**
	 * tests that we cannot set values the community has no access to
	 * NOTE:
	 *   This test will fail if we use a community that has rw access, or 
	 * there is not readonly community setup
	 */
	public void testNoAccess() throws Throwable {
		NetSNMPSession s = new NetSNMPSession() ;
		String newLocation ;
		String setResult ;
		
		s.setPeerName(testHost) ;
		s.setCommunity(testCommunity_ro) ;
		s.setSNMPVersion(NetSNMP.SNMPv2c) ;
		s.open() ;
		
		setResult = setValue(s, sysLocation, new OCTET_STR(setString.getBytes())) ;
		assertTrue(setResult.equals("1/1")) ; // request 'succeeds' 
		
		newLocation = runner.getString(sysLocation) ;
		
		assertFalse(newLocation.equals(setString)) ; // check to make sure it didn't set	
		
	}
	
	/**
	 * Tests that we get a proper result back if we attempt to 
	 * write an mismatched type
	 */
	public void testSetWrongType() throws Throwable {
		NetSNMPSession s = new NetSNMPSession() ;
		String setResult ;
		
		s.setPeerName(testHost) ;
		s.setCommunity(testCommunity) ;
		s.setSNMPVersion(NetSNMP.SNMPv2c) ;
		s.open() ;
		
		setResult = setValue(s, sysContact, new INTEGER(0)) ;
		
		/*
		 * we should see a failure code of 17(SNMP_ERR_WRONGTYPE) on the first value
		 */
		assertTrue(setResult.equals("1/7")) ;
	}
	
	
	public static Test suite() {
		return new TestSuite(setTests.class) ;
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(setTests.suite()) ;
	}
	
}

