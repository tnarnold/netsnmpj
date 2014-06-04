
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netsnmp.* ;
import org.netsnmp.ASN.ASNValue;
import org.netsnmp.util.snmpgetRunner;


/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class SNMPv3Tests extends TestCase implements NetSNMPAction {
	
	NetSNMPSession s ;
	PDU rPDU ;
	static final String testHost = TestProperties.agent ;
	static final String testCommunity = TestProperties.community ;
	
	public void testAuthenticationFailure() throws Throwable {
		PDU pdu ;
		s = new NetSNMPSession() ;
		s.addListener(this) ;

		s.setSNMPVersion(NetSNMP.SNMPv3) ;
		s.setPeerName(testHost) ;
		s.setSecurityLevel(NetSNMP.authNoPriv) ;
		// s.setAuthPassword("abecrombe") ;
		s.setSecurityName("privdefault") ;

		pdu = new PDU(NetSNMP.MSG_GET) ;
		pdu.addNullEntry(new DefaultOID("SNMPv2-MIB::sysDescr.0")) ;

		s.open() ;
	
		synchronized ( this  ) {
			try  {
				s.send(pdu, null) ;
			}
			catch( NetSNMPAuthenticationException je ) {
				return ; // passed
			}
			fail() ; // should have produced
			wait() ;
		} // synchronized

		
	}
	
	/**
	 * Fetch the sysDescr
	 * 
	 * @throws Throwable
	 */
	public void testSysDescr() throws Throwable {
		snmpgetRunner runner = new snmpgetRunner(testHost, testCommunity) ;
		OID oid = new DefaultOID("1.3.6.1.2.1.system.sysDescr.0") ;
		String result ;
		
		result = runner.getString(oid) ;			
		PDU pdu ;
		s = new NetSNMPSession() ;
		s.addListener(this) ;

		//NetSNMP.enableStderrLogging(true) ;
		//NetSNMP.doDebugging(true) ;

		s.setSNMPVersion(NetSNMP.SNMPv3) ;
		s.setPeerName(testHost) ;
		s.setAuthPassword("abecrombe") ;
		s.setPrivPassword("abecrombe") ;
		s.setSecurityLevel(NetSNMP.authNoPriv) ;
		s.setSecurityName("default") ;

		pdu = new PDU(NetSNMP.MSG_GET) ;
		pdu.addNullEntry(oid) ;

		s.open() ;
		
		synchronized ( this  ) {
			s.send(pdu, null) ;
			
			wait() ;
		} // synchronized

		try {
			ASNValue v = rPDU.findValue(oid) ;
			if( result.equals(v.toJavaObject()) )
				return ; // pass
		}
		finally {
			
		}
		// should not reach here
		fail() ;
	}
	

	/* (non-Javadoc)
	 * @see org.netsnmp.NetSNMPAction#actionPerformed(int, org.netsnmp.NetSNMPSession, org.netsnmp.PDU, java.lang.Object)
	 */
	public synchronized boolean  actionPerformed(int result, NetSNMPSession session, PDU pdu, Object o) throws Throwable {
		
		rPDU = pdu ;
		notify() ;
		
		return false;
	}
	
	public static Test suite() {
			return new TestSuite(SNMPv3Tests.class) ;
	}
	
	public static void main(String[] args) throws Throwable {
		junit.textui.TestRunner.run(suite()) ;
		
	}
	
}

/*
 * $Log: SNMPv3Tests.java,v $
 * Revision 1.4  2003/04/27 11:44:00  aepage
 * fixes from compile farm machine
 *
 * Revision 1.3  2003/04/25 23:12:35  aepage
 * fixes from eclipse on win32
 *
 * Revision 1.2  2003/04/18 01:31:56  aepage
 * new tests
 *
 * Revision 1.1  2003/04/15 19:17:15  aepage
 * initial checkin
 *
 */
