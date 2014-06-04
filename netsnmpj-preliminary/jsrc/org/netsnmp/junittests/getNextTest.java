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
import java.util.* ;
import org.netsnmp.* ;
import org.netsnmp.util.snmpgetRunner ;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 * test for the GETNEXT operation
 */public class getNextTest extends TestCase {

	static final String testHost = TestProperties.agent;
	static final String testCommunity = TestProperties.community;
	ArrayList getNextResults = new ArrayList();
	boolean testDone;
	boolean timeoutFlag ;

	public void testGETNEXT() throws Exception, NetSNMPSession.readInterrupt {
		final NetSNMPSession s = new NetSNMPSession(testHost, testCommunity);
		PDU start_pdu = new PDU(NetSNMP.MSG_GETNEXT);
		final OID start_oid = new DefaultOID("1.3.6.1.2.1.system");
		NetSNMPAction e;
		Iterator it;
		PDU.entry ent;

		testDone = false;

		getNextResults.clear();

		e = new NetSNMPAction() {
			public synchronized boolean actionPerformed(
				int result,
				NetSNMPSession session,
				PDU pdu,
				Object o)
				throws NetSNMPException {
				int i;
				PDU next = new PDU(NetSNMP.MSG_GETNEXT);

				if (result == NetSNMP.STAT_TIMEOUT) {
					timeoutFlag = true ;
					notify() ;
				}

				for (i = 0; i < pdu.entries.length; i++) {
					if (start_oid.compareTo(pdu.entries[i].oid, start_oid.length()) != 0) {
						//System.out.println("getnext done");

						// we're done, don't submit another request
						notify() ;
						return true;
					}
					// System.out.println("oid = " +  pdu.entries[i].oid.toString() + " " + pdu.entries[i].value.toJavaObject().toString());
					next.addNullEntry(pdu.entries[i].oid);

					// collect the results, then play them back against the snmpgetrunner
					getNextResults.add(pdu.entries[i]);

				}
				//System.out.println("pdu = " + pdu.toString());
				session.send(next, o);
				return true;
			}

		};

		s.addListener(e);
		start_pdu.addNullEntry(start_oid);
		
		timeoutFlag = false ;
		synchronized( e ) {
			s.send(start_pdu, null);
			e.wait() ;
		} // synchronized
		
		if( timeoutFlag )
			fail() ;

		it = getNextResults.iterator();
		snmpgetRunner runner = new snmpgetRunner(testHost, testCommunity);
		while (it.hasNext()) {
			ent = (PDU.entry) it.next();

			if (ent.value.type() == ASN_TYPE.ASN_OBJECT_ID) {
				//System.out.println("oid ent = " + ent);
				if (new DefaultOID(ent.value.toOBJECTID())
					.compareTo(runner.getOID(ent.oid))
					!= 0) {
					fail();
					return;
				}
				continue;
			}

			if (ent.value.type() == ASN_TYPE.ASN_OCTET_STR) {
				// System.out.println("str ent = " + ent) ;
				if (new String(ent.value.toOctetString())
					.compareTo(runner.getString(ent.oid))
					!= 0) {
					fail();
					return;
				}
				continue;
			}

			if (ent.value.type() == ASN_TYPE.ASN_TIMETICKS) {
				// time ticks are used for 'time counters' which
				// change between the reads
				long fetchedValue = ent.value.toInt64(),
					testValue = runner.getInt(ent.oid);
				if (fetchedValue - testValue > 250) {
					fail();
					return;
				}
				continue;
			}

			//System.out.println("ent = " + ent);
		}

	}

	public static Test suite() {
		return new TestSuite(getNextTest.class);

	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite()) ;
	}

}

/*
 * $Log: getNextTest.java,v $
 * Revision 1.4  2003/04/18 01:48:47  aepage
 * improvement in tests
 *
 * Revision 1.3  2003/03/29 00:06:53  aepage
 * adaptations for new thread architecture
 *
 * Revision 1.2  2003/02/09 23:36:09  aepage
 * failure for a timeout on an snmpgetnext operation
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
