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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.netsnmp.DefaultOID;
import org.netsnmp.NetSNMP;
import org.netsnmp.NetSNMPSession;
import org.netsnmp.NetSNMPSyncSession;
import org.netsnmp.PDU;
import org.netsnmp.ASN.OCTET_STR;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class SendTrapTests extends TestCase {

	static String trapCommunity = TestProperties.community ;
	static String trapHost = TestProperties.trapHost ;
	static Process proc ;
	static BufferedReader rd, rdErr ;
	static InputStream inStr ;
	
	/**
	 * bring the sync stream up to its end before doing some operation
	 *
	 */
	static void syncStream() {
		char buf[] = new char[1] ;
		/*
		 * sort of ugly, but this doesn't have to be too fast 
		 */
		 
		try {
      while( rd.ready() )
      	rd.read(buf, 0, 1) ;
      	
      while( rdErr.ready() )
      	rdErr.read(buf, 0, 1) ;
      	
    }
    catch (IOException e) {
      System.err.println("error reading stream " + e);
      e.printStackTrace();
    }
	}

	
	/**
	 * 
	 * @param target string to look for
	 * @param timeout limit on the amount of time to look for it
	 */
	static boolean findStringInStream(final String target, long timeout) {
		
		while( true ) {
			String line;
      try {
        line = rdErr.readLine();
      }
      catch (IOException e) {
        
        e.printStackTrace();
        return false ;
      }
			if( line.indexOf(target) != -1 )
				continue ;
			return true ;
		}
	}
	 
	/*
	 * Start the trap daemon in a Runtime process in the fore
	 */
	 
	static {
		try {
      proc = Runtime.getRuntime().exec(TestProperties.trapdCommand + " " + trapHost) ;
      
			rd = new BufferedReader(new InputStreamReader(proc.getInputStream())) ;
			rdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream())) ;
      
    }
    catch (IOException e) {
      System.err.println("starting snmptrapd failed") ;
      e.printStackTrace();
    }
    
    Thread t = new Thread() {
  		public void run() {
  			proc.destroy() ;
  			  	
  		}
  
    } ;
    Runtime.getRuntime().addShutdownHook(t) ;
	}
	
	
	public void xtestSendInform() throws Throwable {
		PDU pdu = new PDU(NetSNMP.MSG_INFORM) ;
		NetSNMPSyncSession sync = new NetSNMPSyncSession() ;
		
		sync.setSNMPVersion(NetSNMP.SNMPv1) ;
		sync.setPeerName(trapHost) ;
		sync.setCommunity(trapCommunity) ;
		
		sync.open() ;
		
		pdu.enterprise = new DefaultOID("SNMPv2-SMI::enterprises.3.1.1") ;
		pdu.trap_type = 3 ;
		pdu.specific_type = 0 ;
		pdu.addEntry("1.3", new OCTET_STR("INFORM")) ;
		pdu.addEntry("1.4", new OCTET_STR("INFORMX")) ;
		
		PDU result = sync.send(pdu) ;
		if( result == null )
			fail() ;
	}
	

	
	public void testSendv2Trap() throws Throwable {
		NetSNMPSession s = new NetSNMPSession() ;
		PDU pdu = new PDU(NetSNMP.MSG_TRAP2) ;
		
		s.setSNMPVersion(NetSNMP.SNMPv2c) ;
		s.setPeerName(trapHost) ;
		s.setCommunity(trapCommunity) ;
		
		s.open() ;
		
		pdu.enterprise = new DefaultOID("SNMPv2-SMI::enterprises.3.1.1") ;
		pdu.trap_type = 3 ;
		pdu.specific_type = 0 ;
		pdu.addEntry("1.3", new OCTET_STR("V2 TRAP")) ;
		
		// syncStream() ;
		
		s.send(pdu, null) ;
		
		if( !findStringInStream("V1 TRAP", 5000) )
			fail() ;
	}
	
	public void testSendv1Trap() throws Throwable {
			NetSNMPSession s = new NetSNMPSession() ;
			PDU pdu = new PDU(NetSNMP.MSG_TRAP) ;
		
			s.setSNMPVersion(NetSNMP.SNMPv1) ;
			s.setPeerName("localhost:162") ;
			s.setCommunity(trapCommunity) ;
		
			s.open() ;
		
			pdu.enterprise = new DefaultOID("SNMPv2-SMI::enterprises.3.1.1") ;
			pdu.trap_type = 3 ;
			pdu.specific_type = 0 ;
			pdu.addEntry("1.3", new OCTET_STR("V1 TRAP")) ;
		
			// syncStream() ;
		
			s.send(pdu, null) ;
			
			if( !findStringInStream("V1 TRAP", 5000) )
				fail() ;
		}
		
	

	public static Test suite() {
		return new TestSuite(SendTrapTests.class) ;
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite()) ;
	}
}
