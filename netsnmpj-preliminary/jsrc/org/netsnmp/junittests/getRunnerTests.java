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
 * Test the utility class snmpgetRunner to ensure that it's providing
 * reasonable output before we start using it for extensive testing
 *
 *  NOTE:  the snmpgetRunner is a utility class used to faclitate unit
 * testing within the netsnmpj library <b>and is not intended to be used</b>
 * as a general device for performing snmp operations
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
package org.netsnmp.junittests;

import junit.framework.* ;
import org.netsnmp.util.* ;
import org.netsnmp.* ;

public class getRunnerTests extends TestCase {
	
	static snmpgetRunner defaultRunner = new snmpgetRunner(TestProperties.agent, TestProperties.community) ;
	
	/**
	 * Ensure that we're getting an exception
	 * when we fail receive data from a non-existant
	 * or non functioning agent
	 */
	public void testGetRunnerTimeout() {
		snmpgetRunner runner = new snmpgetRunner("localhost", "boguscommunity") ;
		
		
		// add options so that this program will timeout almost instantly
		// the 'boguscommunity' should gaurentee that we get no repsonse at all
		runner.addOption("-r") ;
		runner.addOption("1") ;
		runner.addOption("-t") ;
		runner.addOption("1") ;
		
		try {
			runner.getString(TestProperties.sysDescriptorOID) ;
		}
		catch( java.io.IOException e ) {
			// if it times out it will throw a IOException with the message
			// "timed out" in it.  This is what we are expecting and checking
			// for
			if( e.getMessage().compareToIgnoreCase("timed out") == 0 )
				return ;
		}
		catch( Exception e ) {
			// any other exception is a failure
			fail() ;
		}
		
		fail() ;
	}
	
	/**
	 * A very light string test.  The typical name for the 1 interface an
	 * agent has is 'lo'.
	 *
	 * @throws any exception is a failure for this test
	 */
	public void testGetString() throws Exception {
		OID loDescriptor = new InstanceOID(TestProperties.ifDescrOID, 1) ;
		String s = defaultRunner.getString(loDescriptor) ;

		if( s.compareTo(TestProperties.loopbackName) != 0 )
			fail() ;
	}
	
	/**
	 * Simple test that we get a reasonable oid result
	 */
	public void testGetOID() throws Exception {
		boolean passed ;
		OID sysObjectOID = new DefaultOID("SNMPv2-MIB::sysObjectID.0") ;
		
		OID resultOID = defaultRunner.getOID(sysObjectOID) ;
		if( resultOID.length() < 5 )
			fail() ;
		
		passed = false ;
		try {
			// test for something that is not an oid
			
			defaultRunner.getOID(TestProperties.sysDescriptorOID) ;
		}
		catch( MIBItemNotFound e ) {
			passed = true ;
		}
		
		if( !passed )
			fail() ;
		
	}
	
	/**
	 * test that we can fetch an int
	 */
	public void testGetInt() throws Exception {
		boolean passed ;
		OID uptimeOID = new DefaultOID("SNMPv2-MIB::sysUpTime.0") ;
		
		// assume agent is up for at least 10 seconds
		int resultInt = defaultRunner.getInt(uptimeOID) ;
		if( resultInt < 100 )
			fail() ;
		
		passed = false ;
		try {
			// test for something that is not an int and will not parse well
			defaultRunner.getInt(TestProperties.sysDescriptorOID) ;
		}
		catch( NumberFormatException e ) {
			// we should get this
			passed = true ;
		}
		
		if( !passed )
			fail() ;
	}
	
	public static Test suite() {
		return new TestSuite(getRunnerTests.class) ;
	}
	
	public static void main(String argsp[]) {
		junit.textui.TestRunner.run(suite()) ;
	}
}

/*
 * $Log: getRunnerTests.java,v $
 * Revision 1.4  2003/04/30 14:47:05  aepage
 * doc correction
 *
 * Revision 1.3  2003/04/27 11:43:59  aepage
 * fixes from compile farm machine
 *
 * Revision 1.2  2003/02/09 23:35:26  aepage
 * tuning of parameters for snmpgetRunner tests
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
