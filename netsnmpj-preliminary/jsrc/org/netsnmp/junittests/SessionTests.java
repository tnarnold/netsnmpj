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
 * SessionTests.java
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

package org.netsnmp.junittests;
import junit.framework.* ;
import org.netsnmp.* ;

/**
 * Tests for various functionalties of the NetSNMPSession object
 */
public class SessionTests extends TestCase {
	
	static final String testHost = TestProperties.agent ;
	static final String testCommunity = TestProperties.community ;
	
	static final OID sysDescrOid = MIB.readObjIDOrAbort("1.3.6.1.2.1.system.sysDescr.0") ;
	
	/**
	 * Test our ability to interupt a session read while it's reading
	 */
	boolean testSessionInterruptPass ;
	
	
	/**
	 * test that we get a NetSNMPException when
	 * we attempt to use an unopened session
	 */
	
	public void testUnopenedSession() {
		boolean pass = false ;
		NetSNMPSession sess = new NetSNMPSession() ;
		
		sess.setPeerName(testHost) ;
		sess.setCommunity(testCommunity) ;
		
		if( sess.isOpen() ) fail() ;
		
		try {
			// shoulnd't be able to send on an unopened session
			sess.send(new PDU(NetSNMP.MSG_GET), null) ;
		}
		catch (NetSNMPSendError e) {
			
		}
		catch ( IllegalStateException e) {
			pass = true ;
		}
		
		if( !pass ) fail() ;
		
	}
	
	/**
	 * test to make sure that we throw a illegalStateException when we use
	 * an unknown host.  
	 * 
	 * @throws Throwable
	 */
	public void testBadHost() throws Throwable {
		NetSNMPSession s = new NetSNMPSession() ;
		s.setPeerName("fkdksldslbogusbogus") ;
		s.setCommunity("public") ;
		
		try {
			s.open() ;
			fail() ;
		}
		catch (IllegalStateException e) {
			return ; // passed
		}
		
		fail() ;

	}
	
	/**
	 * test that we're catching null pointer exceptions
	 */
	public void testNullParams() {
		NetSNMPSession sess = new NetSNMPSession() ;
					
		try {
			sess.setPeerName(null) ;
			fail() ; // should have thrown
		}
		catch( NullPointerException ex ) {
			// 	we're expecting this
			return ;
		}

		fail() ;
	}
	
	public static Test suite() {
		return new TestSuite(SessionTests.class) ;
	}

    public static void main(String args[]) {

	junit.textui.TestRunner.run(SessionTests.suite()) ;
    }
}


