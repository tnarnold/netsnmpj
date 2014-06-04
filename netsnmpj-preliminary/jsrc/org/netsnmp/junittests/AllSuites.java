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
import org.netsnmp.util.* ;

/**
 * Convenience class that groups and tests all of our tests
 *
 * NOTE:   Due to a known problem with the classloader the swingui
 * has problems doing multiple runs on tests.
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class AllSuites {
	
	public static Test suite() {
		TestSuite allSuites = new TestSuite() ;
		
		allSuites.addTest(SerializeTests.suite()) ;
		allSuites.addTest(SNMPv3Tests.suite()) ;
		allSuites.addTest(TimeoutTest.suite()) ;
		allSuites.addTest(getRunnerTests.suite()) ;
		
		allSuites.addTest(ASNtests.suite()) ;
		allSuites.addTest(MIBTest.suite()) ;
		allSuites.addTest(getNextTest.suite()) ;
		
		
		allSuites.addTest(getTests.suite()) ;
		allSuites.addTest(setTests.suite()) ;
		allSuites.addTest(SessionTests.suite()) ;
		allSuites.addTest(getBulkTest.suite()) ;
		
		allSuites.addTest(SendTrapTests.suite()) ;
		
		return allSuites ;
	}
	
	public static void main(String [] args) {
		
		snmpgetRunner r = new snmpgetRunner(TestProperties.agent, TestProperties.community) ;
		
		try {
			r.getString(TestProperties.sysDescriptorOID) ;
		}
		catch( Exception e ) {
			System.err.println("The test agent(" + TestProperties.agent + ") is not responding") ;
			System.err.println("Check the settings in netsnmpjTest.properties") ;
			System.err.println(e.toString()) ;
			System.exit(2) ;
		}
		
		System.out.println("net-snmp library " + NetSNMP.getNetSNMPVersion()) ;
		System.out.println("NetSNMP Java Library " + NetSNMP.getReleaseVersion()) ;
		
		
		junit.textui.TestRunner.run(AllSuites.suite()) ;
		System.exit(0) ; // some vms may not be able to exit
	}
	
}

/*
 * $Log: AllSuites.java,v $
 * Revision 1.9  2003/04/29 16:59:17  aepage
 * trap tests
 *
 * Revision 1.8  2003/04/18 01:32:25  aepage
 * new tests
 *
 * Revision 1.7  2003/04/15 19:40:37  aepage
 * fix for VMs that might not exit cleanly.
 *
 * Revision 1.6  2003/03/02 18:23:29  aepage
 * added version property and added to test suites.
 *
 * Revision 1.5  2003/02/28 20:16:34  aepage
 * Added setTests
 *
 * Revision 1.4  2003/02/19 19:12:59  aepage
 * added test for GETBULK
 *
 * Revision 1.3  2003/02/15 19:39:57  aepage
 * Added session tests
 *
 * Revision 1.2  2003/02/10 00:15:29  aepage
 * test for test agent failure before beginning test suites
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
