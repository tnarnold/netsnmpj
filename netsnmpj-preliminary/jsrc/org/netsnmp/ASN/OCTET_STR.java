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
/*
 * OCTET_STR.java
 *
 * Created on December 31, 2002, 9:09 AM
 */

package org.netsnmp.ASN;

import org.netsnmp.*;
/**
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class OCTET_STR extends ASNValue {
    
    public static final ASN_TYPE type = ASN_TYPE.ASN_OCTET_STR ;
    
    public byte [] data ;
    /** Creates a new instance of OCTET_STR */
    public OCTET_STR(byte [] data) {
        this.data = data ;
    }
	public OCTET_STR(String s) {
		this(s.getBytes()) ;
	}
    public byte [] toOctetString() {
        return data ;
    }
    
    public int asn_type() { return type.toByte() ; }
    
    public ASN_TYPE type() {
        return type ;
    }
    
    /**
     * Converts the data to a Java String
     */
    public String toString() {
    	return new String(data) ;	
    }
    
    public Object toJavaObject() {
        return new String(data) ;
    }
    
 ;
}

/*
 * $Log: OCTET_STR.java,v $
 * Revision 1.4  2003/04/30 21:05:49  aepage
 * doc fixes
 *
 * Revision 1.3  2003/03/26 21:58:01  aepage
 * Convenience function to convert value to a String object
 *
 * Revision 1.2  2003/03/17 16:08:48  aepage
 * convenience function that allows initializationg from a String
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
