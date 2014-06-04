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




package org.netsnmp.junittests;

import org.netsnmp.* ;
import junit.framework.* ;
import org.netsnmp.util.* ;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 * test for the GETBULK operation
 */
public class getBulkTest extends TestCase implements NetSNMPAction {
		
	static final String testHost = TestProperties.agent ;
	static final String testCommunity = TestProperties.community ;
	
	static final OID sysDescrScalar = MIB.readObjIDOrAbort("SNMPv2-MIB::sysDescr") ;
	static final OID ifInRepeater   = MIB.readObjIDOrAbort("IF-MIB::ifInOctets") ;
	static final OID ifOutRepeater  = MIB.readObjIDOrAbort("IF-MIB::ifOutOctets") ;
	
	static final OID ifNumber       = MIB.readObjIDOrAbort("IF-MIB::ifNumber.0") ;
	
	static snmpgetRunner runner = new snmpgetRunner(testHost, testCommunity) ;
	
	
	NetSNMPSession session ;
	
	int nInterfaces = runner.getIntOrAbort(ifNumber) ;
	
	boolean success = true ;

	PDU resultPDU ;	
	
	public synchronized boolean actionPerformed(int result, NetSNMPSession session, PDU pdu, Object o) throws Throwable {
		if( result == NetSNMP.STAT_TIMEOUT )
			success = false ;
		resultPDU = pdu ;
		notify() ;
		return false;
	} // action performed
	
	
	public getBulkTest() {
		session = new NetSNMPSession() ;
		session.setSNMPVersion(NetSNMP.SNMPv2c) ;
		session.setCommunity(testCommunity) ;
		session.setPeerName(testHost) ;
		session.open() ;
		
		session.addListener(this) ;
	}
	
	public void testBulkSimple() throws Throwable {
		PDU pdu = new PDU(NetSNMP.MSG_GETBULK) ;
		int i ;
		
		pdu.addNullEntry(sysDescrScalar) ; // scalar
		pdu.addNullEntry(ifInRepeater) ;   // repeaters
		pdu.addNullEntry(ifOutRepeater) ;
		
		pdu.maxRepetitions = 2 ;
		pdu.nonRepeaters = 1 ;
		
		
		synchronized( this ) {
			
			session.send(pdu, null) ;
			
			this.wait() ;	
		} // synchronized

		if( resultPDU == null )
			fail() ;
		
		OID sysDescrOID = new InstanceOID(sysDescrScalar, 0) ;
		
		
		String str = new String(resultPDU.findValue(sysDescrOID).toOctetString()) ;
		if( !str.equals(runner.getString(sysDescrOID)) )
			success = false ;	
		
		/*
		 * this could possibly fail if there is a large ammount of
		 * traffic running through one of the interfaces when this test
		 * is being run
		 */
		
		for( i = 1 ; i <= nInterfaces ; i++ ) {
			OID oid = new InstanceOID(ifInRepeater, i) ;
			// System.out.println("diff = " + (runner.getInt(oid) - resultPDU.findValue(oid).toInt64()));
			if( runner.getInt(oid) - resultPDU.findValue(oid).toInt64() > 10000 )
				fail() ;
		
			oid = new InstanceOID(ifOutRepeater, i) ;
			if( runner.getInt(oid) -  resultPDU.findValue(oid).toInt64() > 10000 )
				fail() ;
		}

		if( !success )
			fail() ;		
	}
	
	
	public static Test suite() {
		return new TestSuite(getBulkTest.class) ;
	}
	
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite()) ;
	}
}

