
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


package org.netsnmp.junittests ;

import java.io.* ;
import org.netsnmp.* ;



/**
 * Local properties for JUnit Tests.  Properties may be modified from the defaults
 * by adding a properties file in the working directory of this test.
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
class TestProperties extends java.util.Properties {
	
	/** home direcotry to look for things such as
	 includes, mibs, libs.
	 loaded from org.netsnmp.junittests.netsnmpHomeDir
	 */
	public static final String netsnmpHomeDir ;
	
	/** host name of the remote agent used for testing
	    loaded from org.netsnmp.junittests.agent */
	public static final String agent ;
	public static final String trapHost ;
	public static final String trapdCommand ;
	/** community of the remote agent used for testing
		loaded from org.netsnmp.junittests.community
	 */
	public static final String community ;
	/**
	 * read only community of the remote agent used for testing
	 * Defaults to the "ro" + community 
	 */
	public static final String community_ro ;
	/** path to mib file to be loaded for testing reading new MIB */
	public static final String newMIBTestFile ;
	/** OID name to look for after a new mib has been loaded */
	public static final String newMIBTestOID ;
	/** Module to load for testing reading a new module */
	public static final String newModuleTestModule ;
	/** OID name to look for after a new module has been loaded */
	public static final String newModuleTestOID ;
	
	/** address of master agent to connect to */
	public static final String agentXSocket ; 
	
	public static final String agentXMaster ;
	public static final String agentXCommunity ;
	
	
	static final int sysDescriptorOIDs [] = { 1,3,6,1,2,1,1,1,0 } ;
	public static final OID sysDescriptorOID = new DefaultOID(sysDescriptorOIDs) ;
	
	
	public static OID ifDescrOID ;
	public static String loopbackName ;
	private static TestProperties localProperties ;
	
	
	static {
		OID oid = null ;
		/* check for properties in the local working directory */
		
		localProperties = new TestProperties() ;
		
		try {
			localProperties.load(new FileInputStream("netsnmpjTest.properties")) ;
		}
		catch ( FileNotFoundException  e ) {
			// go with the defaults
		}
		catch( IOException e ) {
			System.err.println("Caught unexpected IOException loading test properties " + e) ;
			System.exit(2) ;
		}
		
		netsnmpHomeDir = localProperties.getProperty("org.netsnmp.junittests.netsnmpHomeDir", "/usr") ;
		trapdCommand = localProperties.getProperty("org.netsnmp.junittests.trapdCommand", "snmptrapd -f ") ;
		
		agent = localProperties.getProperty("org.netsnmp.junittests.agent", "localhost") ;
		trapHost = localProperties.getProperty("org.netsnmp.junittests.trapHost", "localhost:4445") ;
		community = localProperties.getProperty("org.netsnmp.junittests.community", "public") ;
		community_ro = localProperties.getProperty("org.netsnmp.junittests.community_ro", "ro" + community) ;
		
		newMIBTestFile = localProperties.getProperty("org.netsnmp.junittests.newMIBTestFile", netsnmpHomeDir + "/share/snmp/mibs/EtherLike-MIB.txt") ;
		newMIBTestOID  = localProperties.getProperty("org.netsnmp.junittests.newMIBTestOID", "iso.org.dod.internet.mgmt.mib-2.transmission.dot3.dot3StatsTable") ;
		
		newModuleTestModule = localProperties.getProperty("org.netsnmp.junittests.newModuleTestModule", "SMUX-MIB") ;
		newModuleTestOID    = localProperties.getProperty("org.netsnmp.junittests.newModuleTestOID", "iso.org.dod.internet.private.enterprises.unix.smux") ;
	

		loopbackName = localProperties.getProperty("org.netsnmp.junittests.loopbackIfName", "lo") ;
		
		agentXSocket = localProperties.getProperty("org.netsnmp.junittests.agentXSocket") ;
		agentXMaster = localProperties.getProperty("org.netsnmp.junittests.agentXMaster") ;
		agentXCommunity = localProperties.getProperty("org.netsnmp.junittests.agentXCommunity") ;
	  

		try {
			oid = new DefaultOID("IF-MIB::ifDescr") ;
		}
		catch( MIBItemNotFound e ) {
			ifDescrOID = null ;
			System.err.println("##") ;
			System.err.println("## Could not find a mib item necessary for testing " + e) ;
			System.err.println("##") ;
			System.exit(2) ;
			
		}
		finally {
			ifDescrOID = oid ;
		}
		
		
	}
	
	private TestProperties() {
		
	}
	
}

/*
 * $Log: TestProperties.java,v $
 * Revision 1.6  2003/05/04 21:38:22  aepage
 * support for sub-agents
 *
 * Revision 1.5  2003/04/29 16:59:17  aepage
 * trap tests
 *
 * Revision 1.4  2003/04/27 11:44:00  aepage
 * fixes from compile farm machine
 *
 * Revision 1.3  2003/04/12 02:21:04  aepage
 * added tests for more incorrect set operations
 *
 * Revision 1.2  2003/02/10 01:02:49  aepage
 * test fix
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
