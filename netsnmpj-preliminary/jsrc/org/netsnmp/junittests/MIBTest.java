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
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
package org.netsnmp.junittests;

import junit.framework.* ;

import org.netsnmp.* ;

public class MIBTest extends TestCase {
	
	public void testTypeLookups() throws MIBItemNotFound {
		OID oid ;
		
		oid = new DefaultOID("IF-MIB::ifInOctets") ;
		if( oid.getASNType() != ASN_TYPE.ASN_COUNTER )
			fail() ;
			
		oid = new DefaultOID("SNMPv2-MIB::sysDescr.0") ;
		if( oid.getASNType() != ASN_TYPE.ASN_OCTET_STR )
			fail() ;
			
		oid = new DefaultOID("SNMPv2-MIB::sysUpTime.0") ;
		if( oid.getASNType() != ASN_TYPE.ASN_TIMETICKS )
			fail() ;
			
		oid = new DefaultOID("SNMPv2-MIB::sysServices.0") ;
		if( oid.getASNType() != ASN_TYPE.ASN_INTEGER )
			fail() ;
			
		oid = new DefaultOID("SNMPv2-MIB::sysObjectID.0") ;
		if( oid.getASNType() != ASN_TYPE.ASN_OBJECT_ID )
			fail() ;		
	}
	
	/**
	 * Looks up the sysDescr OID and compares it to its known value
	 */
	public void testSimpleLookup() throws MIBItemNotFound {
		OID sysDescrOID = MIB.readObjID("iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0") ;
		int [] oids = sysDescrOID.oids() ;
		
		if( !java.util.Arrays.equals(oids, TestProperties.sysDescriptorOIDs) )
			fail() ;
	}
	
	/**
	 * Test the functionality of an exception being thrown when we lookup
	 * an object that is either incorrectly entered, or doesn't existing
	 */
	public void testItemNotFound() {
		OID sysDescrOID ;
		try {
			/*
			 * Try to look up an object KNOWN to be incorrect.  We should throw
			 * an exception and we should catch it
			 */
			sysDescrOID = MIB.readObjID("isoXXX.org.dod.internet.mgmt.mib-2.system.sysDescr.0") ;
		}
		catch( MIBItemNotFound e ) {
			return ; // we passed
		}
		
		/**
		 * We found an entry for something that should not exist
		 */
		fail() ;
		System.err.println("found bogus oid " + sysDescrOID);
		
	}
	
	/**
	 * Tests that the readMIB method will throw an exception
	 * when presented with a file that doesn't exist
	 */
	public void testMIBFileNotFound() {
		
		try {
			// NetSNMP.enableStderrLogging(true) ; // if someone wants to confirm logging is working
			MIB.readMIB("bogusfile.xxxxxx IGNORE WARNING") ;
		}
		catch( java.io.FileNotFoundException e ) {
			return ; // we pass
		}
		fail() ;
	}
	
	/**
	 * Tests the functionality of loading a new MIB
	 *
	 *
	 *
	 * @throws   MIBItemNotFound if the test fails
	 *
	 */
	public void testAddMib() throws Exception {
		boolean wasFound = false ;
		OID checkOID = null ;
		/*
		 * First attempt to read the object to make sure it's not
		 * already there
		 */
		
		try {
			checkOID = MIB.readObjID(TestProperties.newMIBTestOID) ;
			wasFound = true ;
		}
		catch( MIBItemNotFound e ) {
			wasFound = false ;
		}
		
		if( wasFound ) {
			/*
			 * This test needs have a case where we look for an object
			 * from an unloaded MIB, then we load then MIB then find
			 * the object.
			 */
			
			fail() ;
			System.out.println("oid " + checkOID + " not found");
			return ;
		}
		
		
		MIB.readMIB(TestProperties.newMIBTestFile) ;
		
		checkOID = MIB.readObjID(TestProperties.newMIBTestOID) ;
	}
	
	/**
	 * Tests the functionality of loading a new module
	 * and finding a new OID specified therein.
	 */
	public void testLoadModule() throws MIBItemNotFound {
		boolean wasFound ;
		OID checkOID ;
		try {
			checkOID = MIB.readObjID(TestProperties.newModuleTestOID) ;
			if( checkOID == null )
				fail() ;
			wasFound = true ;
		}
		catch( MIBItemNotFound e ) {
			wasFound = false ;
		}
		
		if( wasFound ) {
			/*
			 * This test needs have a case where we look for an object
			 * from an unloaded MIB, then we load then MIB then find
			 * the object.
			 */
			fail() ;
			return ;
		}
		
		MIB.readModule(TestProperties.newModuleTestModule) ;
		
		checkOID = MIB.readObjID(TestProperties.newModuleTestOID) ;
		if( checkOID == null ) 
			fail() ;
	}
	
	public static Test suite() {
		return new TestSuite(MIBTest.class) ;
	}
	
	public static void main(String argsp[]) {
		junit.textui.TestRunner.run(suite()) ;
	}

}


/*
 * $Log: MIBTest.java,v $
 * Revision 1.3  2003/06/01 15:23:30  aepage
 * additional tests
 *
 * Revision 1.2  2003/03/31 00:43:08  aepage
 * removal of unuzed variables.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
