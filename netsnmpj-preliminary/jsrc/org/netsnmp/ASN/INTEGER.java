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
 * INTEGER.java
 *
 * Created on December 31, 2002, 9:14 AM
 */

package org.netsnmp.ASN;

import org.netsnmp.*;

/**
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class INTEGER extends ASNValue {
    
    public static final ASN_TYPE type = ASN_TYPE.ASN_INTEGER ;
    
    public int data ;
    /** Creates a new instance of INTEGER */
    public INTEGER(int data) {
        this.data = data ;
    }
    
    public int toInt() { return data ; }
	public long toInt64() { return data ; }
    public int asn_type() { return type.toByte() ; }
    
    public ASN_TYPE type() {
        return type ;
    }
    
    public Object toJavaObject() {
        return new Integer(data) ;
    }
    
}

/*
 * $Log: INTEGER.java,v $
 * Revision 1.1.1.1  2003/02/07 23:56:50  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:03:20  aepage
 * value tune ups
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
