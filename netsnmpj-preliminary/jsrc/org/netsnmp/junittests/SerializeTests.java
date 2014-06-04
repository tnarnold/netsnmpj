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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import org.netsnmp.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test our ablilty to serialize/deserialize objects
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class SerializeTests extends TestCase {
	
	static String testHost = TestProperties.agent ;
	static String testCommunity = TestProperties.community ;
	
	
	
	static OID oids[] = { MIB.readObjIDOrAbort("SNMPv2-MIB::sysDescr.0") ,
												MIB.readObjIDOrAbort("SNMPv2-MIB::sysLocation.0"),
												MIB.readObjIDOrAbort("SNMPv2-MIB::sysDescr.0"),
												MIB.readObjIDOrAbort("SNMPv2-MIB::sysLocation.0"),
												new InstanceOID(MIB.readObjIDOrAbort("SNMPv2-MIB::sysDescr"), 1)
											} ;
	static OID sysDescrOID = oids[0] ;
	
	
	private static class tmpSerializer {
		
		FileInputStream ifStr ;
		FileOutputStream ofStr ;
		
		ObjectInputStream istr ;
		ObjectOutputStream ostr ;
		
		File tmpFile ;
		
		void flush() throws IOException {
			ostr.flush() ;	
			
		}
		
		void rewind() throws IOException {
			flush() ;
			
		}
		
		Object read() throws IOException, ClassNotFoundException {
			
			if( istr == null ) {
				ostr.flush() ;
				ifStr = new FileInputStream(tmpFile) ;
				istr = new ObjectInputStream(ifStr) ;
			}
			
			return istr.readObject() ;
		}
		void write(Object o) throws IOException {
			ostr.writeObject(o) ;
		}
		
		tmpSerializer() {
			try {
				tmpFile = File.createTempFile("junittest", "objs") ;
				tmpFile.deleteOnExit() ;
				
				ifStr = new FileInputStream(tmpFile) ;
				ofStr = new FileOutputStream(tmpFile) ;
				
				istr = null ;
				ostr = new ObjectOutputStream(ofStr) ;
				
			}
			catch( Exception e ) {
				e.printStackTrace(System.err) ;
				System.err.println("tmpfile failed");
				System.exit(1) ;
			}
			
		}

	}
	
	public void testSerializeSession() throws Throwable {
			NetSNMPSyncSession s1, s2 ;
			String str, str2 ;
			tmpSerializer tmp = new tmpSerializer() ;
			s1 = new NetSNMPSyncSession(testHost, testCommunity) ;
		
			str = (String) s1.get(sysDescrOID).toJavaObject() ;
		
			//System.out.println("1 got " + str);
	
			tmp.write(s1) ; 
			
			s2 = (NetSNMPSyncSession)tmp.read() ;
			
			str2 = (String) s2.get(sysDescrOID).toJavaObject() ;
			
			//System.out.println("2 got " + str2);
			
			if( !str.equals(str2) )
				fail() ;
		
		}
	
	
	public void testSerializeOID() throws Throwable {
		
		tmpSerializer tmp = new tmpSerializer() ;
		
		OID o ;
		
		for (int i = 0; i < oids.length; i++) {
			tmp.write(oids[i]) ;
		}
		
		tmp.flush() ;
		
		for (int i = 0; i < oids.length; i++) {
			o = (OID)tmp.read() ;
			if( o.compareTo(oids[i]) != 0 )
						fail() ;
		}
	}

	public static Test suite() {
		return new TestSuite(SerializeTests.class) ;
	}
	
	public static void main(String[] args) throws Throwable {
		junit.textui.TestRunner.run(suite()) ;
		
	}
	
}
