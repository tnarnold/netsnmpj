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

/**
 * Test the timeout operations
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class TimeoutTest extends TestCase {
	
	boolean responseReceived = false ;
	
	/**
	 * Either the host or the community has to be bogus
	 * to produce a timeout
	 */
	String bogusAgent     = "127.0.0.2";
	String bogusCommunity = "foobar" ;
	boolean timeoutReceived = false ;

	/**
	 * Checks the timing of the first timeout
	 */
	
	
	public void testTiming() throws Throwable {
		NetSNMPSession sess = new NetSNMPSession() ;
		PDU pdu = new PDU(NetSNMP.MSG_GET) ;
		long t0 ;
		
		sess.setPeerName("127.0.0.2") ;
		sess.setCommunity("foobar") ;
		sess.setSNMPVersion(NetSNMP.SNMPv2c) ;
		sess.setTimeout((long)2e6) ;
		sess.setRetries(0) ;
		sess.open() ;
		
		
		
		NetSNMPAction l = new NetSNMPAction() {
			public synchronized boolean actionPerformed(
				int result,
				NetSNMPSession s,
				PDU rpdu,
				Object o) {
				responseReceived = true;
				if (result == NetSNMP.STAT_TIMEOUT)
					timeoutReceived = true;
				notify();
				return true;
			}
		};
		
		sess.addListener(l) ;
		
		pdu.addNullEntry(TestProperties.sysDescriptorOID) ;
		
		responseReceived = false ;
		timeoutReceived = false ;
		synchronized( l ) {
			t0 = System.currentTimeMillis() ;	
			sess.send(pdu, null) ;
		
			l.wait() ;
			
		} // synchronized
		long dt = (System.currentTimeMillis() - t0) ;
		
		if( !responseReceived )
			fail() ;
		if( !timeoutReceived )
			fail() ;
		if( dt/1000 != 2 )
			fail() ;
	}
	public static Test suite() {
		return new TestSuite(TimeoutTest.class);
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}
	
}

/*
 * $Log: TimeoutTest.java,v $
 * Revision 1.6  2003/05/02 13:20:33  aepage
 * fix for making sure that we change the timeout of the session.
 *
 * Revision 1.5  2003/04/23 00:34:17  aepage
 * Change a type cast to satisfy the solaris jjm, whereas the linux build
 * doesn't seem to have a problem.  Although this could be 'jikes' issue.
 *
 * Revision 1.4  2003/04/18 01:47:57  aepage
 * improvement in tests
 *
 * Revision 1.3  2003/03/29 00:06:54  aepage
 * adaptations for new thread architecture
 *
 * Revision 1.2  2003/02/25 23:53:21  aepage
 * due to what seems to be a difference between how win32 handles select
 * vs unix, it does not seem possible to get an exact match on how many
 * read operations it takes to complete.
 *
 * Revision 1.1  2003/02/09 13:17:25  aepage
 * Initial checkin
 *
 */
