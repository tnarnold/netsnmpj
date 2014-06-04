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

package org.netsnmp.ASN;

import org.netsnmp.ASN_TYPE;

/**
 * @author
 *
 */
public class END_OF_MIBVIEW extends ASNValue {

	private END_OF_MIBVIEW() {
	}
	
	public static final END_OF_MIBVIEW END_OF_MIBVIEW = new END_OF_MIBVIEW() ;
	public static final ASN_TYPE type = ASN_TYPE.ASN_NULL ;
	
	/**
	 * Convenience function to test whether an object is the end of the mib
	 * @param v value to test
	 * @return whether or not this value is at the end of the mib tree
	 */
	public static boolean isEndOfMib(ASNValue v) {
		return v == END_OF_MIBVIEW ;
	}
	
    
	public int asn_type() { return type.toByte() ; }
    
	public ASN_TYPE type() {
			return type ;
	}
    
	public Object toJavaObject() {
			return END_OF_MIBVIEW ;
	}

}
