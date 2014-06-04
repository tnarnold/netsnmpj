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

import junit.framework.* ;

import org.netsnmp.* ;
import org.netsnmp.util.snmpgetRunner ;

/**
 * Test class to exercise various get functions through sesions and pdu's
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class getTests extends TestCase {
	
	abstract static class testBase implements NetSNMPAction {
		boolean success = false ;	
	}
	
	static final String testHost = TestProperties.agent ;
	static final String testCommunity = TestProperties.community ;
	
	static final OID sysDescrOid = MIB.readObjIDOrAbort("1.3.6.1.2.1.system.sysDescr.0") ;
	static final OID sysOBJIDOid = MIB.readObjIDOrAbort("1.3.6.1.2.1.system.sysObjectID.0") ;
	static final OID ifNumberOid = MIB.readObjIDOrAbort("1.3.6.1.2.1.interfaces.ifNumber.0") ;

	snmpgetRunner defaultRunner = new snmpgetRunner(testHost, testCommunity) ;
	
	String fetchedString ;
	OID fetchedOID ;
	int fetchedInt ;
	
	boolean testPassed ;
	boolean testDone ;
		
	/**
	 * Simple test on several common system values
	 */
	public void testSysValues()
		throws Exception {
		NetSNMPSession s = new NetSNMPSession(testHost, testCommunity);
		snmpgetRunner runner = new snmpgetRunner(testHost, testCommunity);
		PDU pdu = new PDU(NetSNMP.MSG_GET);

		testBase l = new testBase() {
			public synchronized boolean actionPerformed(
				int result,
				NetSNMPSession session,
				PDU rpdu,
				Object o)
				throws NetSNMPException {

				success = true ;
				if (result == NetSNMP.STAT_TIMEOUT)
					success = false ;

				fetchedString = new String(rpdu.findValue(sysDescrOid).toOctetString());
				fetchedOID = new DefaultOID(rpdu.findValue(sysOBJIDOid).toOBJECTID());
				fetchedInt = rpdu.findValue(ifNumberOid).toInt();

				notify() ;

				return true;

			}
		};

		s.addListener(l);

		pdu.addNullEntry(sysDescrOid);
		pdu.addNullEntry(sysOBJIDOid);
		pdu.addNullEntry(ifNumberOid);

		testDone = false;

		synchronized( l ) {
			s.send(pdu, this);			
			l.wait() ;
		} // synchronized
		if( !l.success )
			fail() ;

		String stringFromRunner = runner.getString(sysDescrOid);
		if (fetchedString == null
			|| fetchedString.compareTo(stringFromRunner) != 0)
			fail();

		OID OIDfromRunner = runner.getOID(sysOBJIDOid);
		if (fetchedOID == null || fetchedOID.compareTo(OIDfromRunner) != 0)
			fail();

		int intFromRunner = runner.getInt(ifNumberOid);
		if (intFromRunner != fetchedInt)
			fail();

	}
	

	/**
	 * Test that the session throws and error when someone
	 *  attempts to construct an object that is 'too big'.
	 *  If the user builds attempts to send a pdu that is 'too large'
	 *  an SNMPErrTooBig exception should be thrown
	 *
	 *    TBD tune this up since I'm suspicious that we're not reporting
	 * on an object that should be too large for a single packet.
	 */
		public void testObjectTooBig() throws Exception {
		NetSNMPSession sess = new NetSNMPSession(testHost, testCommunity);
		PDU pdu = new PDU(NetSNMP.MSG_GET);
		OID ifInOctets =
			new DefaultOID("IF-MIB::ifInOctets");
		boolean ExceptionCaught = false;
		int i, n = 4000;
		org.netsnmp.ASN.INTEGER intValue = new org.netsnmp.ASN.INTEGER(0);

		for (i = 0; i < n; i++)
			pdu.addEntry(new InstanceOID(ifInOctets, i + 1), intValue);

		try {

			sess.send(pdu, null);

		} catch (NetSNMPErrTooBig e) {
			ExceptionCaught = true;
		}

		if (!ExceptionCaught)
			fail();

	}

	/**
	 * Tests that we get a proper response for an object that does not exist
	 * and instance that does not exist
	 */
	
	public void testNoSuchObjectAndInstance() throws Throwable {
		NetSNMPSession sess = new NetSNMPSession() ;
		
		sess.setPeerName(testHost) ;
		sess.setCommunity(testCommunity) ;
		sess.setSNMPVersion(NetSNMP.SNMPv2c) ;
		sess.open() ;
		
    PDU pdu = new PDU(NetSNMP.MSG_GET) ;
		int oids[] = { 1,4,999 } ;
		
    final OID bogusObject = new DefaultOID(oids) ;
		final OID goodInstance = new DefaultOID("SNMPv2-MIB::sysDescr.0") ;
		final OID bogusInstance = new DefaultOID("SNMPv2-MIB::sysDescr.1") ;
		
		pdu.addNullEntry(goodInstance) ;
		pdu.addNullEntry(bogusObject) ;
		pdu.addNullEntry(bogusInstance) ;
		
		testBase l = new testBase() {
           public synchronized boolean actionPerformed(int result, NetSNMPSession session, PDU rpdu, Object o)
            throws NetSNMPException {
							org.netsnmp.ASN.ASNValue v ;
							success = true ;
							if( result == NetSNMP.STAT_TIMEOUT )
								success = false ;
							
							v = rpdu.findValue(bogusObject) ;
							if( v.getClass() != org.netsnmp.ASN.NO_SUCH_OBJECT.class )
								success = false ;
							
							v = rpdu.findValue(bogusInstance) ;
							if( v.getClass() != org.netsnmp.ASN.NO_SUCH_INSTANCE.class )
								success = false ;
							
							notify() ; // notify someone waiting for result
				
             return true ;
               
           } // actionPerformed
        } ;
		
		sess.addListener(l) ;
		
		synchronized( l ) {
			sess.send(pdu, this) ;
			l.wait() ; // wait until callback notifies us
		} // synchronized
		
		if( !l.success )
			fail() ;
		
	}

	
	public static Test suite() {
		return new TestSuite(getTests.class) ;
		
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite()) ;
	}
	
}

/*
 * $Log: getTests.java,v $
 * Revision 1.8  2003/04/18 01:49:21  aepage
 * refactoring
 *
 * Revision 1.7  2003/03/30 23:04:50  aepage
 * removed unncessary import and class dereference.
 *
 * Revision 1.6  2003/03/29 00:06:52  aepage
 * adaptations for new thread architecture
 *
 * Revision 1.5  2003/02/15 19:41:21  aepage
 * tuned up the no such instance and no such object tests.
 *
 * Revision 1.4  2003/02/15 18:08:40  aepage
 * added a test for an unopened session.
 *
 * Revision 1.3  2003/02/10 00:46:21  aepage
 * fix for some javac compilers
 *
 * Revision 1.2  2003/02/10 00:14:28  aepage
 * timeouts on get tests
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.5  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */


