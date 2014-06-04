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
import org.netsnmp.ASN.* ;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 * tests various ASN Value functions
 */
public class ASNtests extends TestCase {
	
	public void badInt64(ASNValue v) {
		boolean caught ;
		
		caught = false ;
		try {
			v.toOctetString() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toOBJECTID() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toBoolean() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toInt() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toInt64() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( caught ) fail() ; // should be able to convert
		
	}
	
	public void badInt(ASNValue v) {
		boolean caught ;
		
		caught = false ;
		try {
			v.toOctetString() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toOBJECTID() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toBoolean() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toInt() ; // should be able to convert
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( caught ) fail() ;
		
		caught = false ;
		try {
			v.toInt64() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( caught ) fail() ; // should be able to convert
		
	}
	
	public void badOctetString(ASNValue v) {
		boolean caught ;
		
		caught = false ;
		try {
			v.toOctetString() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( caught ) fail() ; // should be able to convert
		
		caught = false ;
		try {
			v.toOBJECTID() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toBoolean() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toInt() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
		
		caught = false ;
		try {
			v.toInt64() ;
		}
		catch( NetSNMPBadValue e ) {
			caught = true ;
		}
		if( !caught ) fail() ;
	}
	
	public void testIPAddrToString() throws Exception {
		NetSNMPSyncSession sess = new NetSNMPSyncSession(TestProperties.agent, TestProperties.community) ;
		ASNValue v ;
		
		v = sess.get("IP-MIB::ipAdEntAddr.127.0.0.1") ;
		
		if( !v.toString().equals("127.0.0.1")) 
			fail() ;
		
		
	}
	
	public void testBadValues() {
		
		
		/* types that can be converted to a long */
		badInt64(new COUNTER(0)) ;
		badInt64(new TIMETICKS(0)) ;
		badInt64(new UNSIGNED(0)) ;
		
		/* test types that can be converted to ints */
		badInt(new INTEGER(0)) ;
		
		
		badOctetString(new OCTET_STR("FOO".getBytes())) ;
		badOctetString(new IPADDRESS("xxxx".getBytes())) ;
		
	}
	
	public static Test suite() {
		return new TestSuite(ASNtests.class) ;
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite()) ;
	}
}

/*
 * $Log: ASNtests.java,v $
 * Revision 1.2  2003/04/11 19:27:00  aepage
 * test for conversion from IP address type.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
