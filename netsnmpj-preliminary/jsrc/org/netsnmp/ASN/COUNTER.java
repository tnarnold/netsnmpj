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
 * COUNTER.java
 *
 * Created on December 31, 2002, 9:14 AM
 */

package org.netsnmp.ASN;

import org.netsnmp.*;

/**
 *  Used to represent a COUNTER type.
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class COUNTER  extends ASNValue {
    
    public static final ASN_TYPE type = ASN_TYPE.ASN_COUNTER ;
    
     /**
      * long is used instead of int, since java has no 'unsigned'
      * types and SNMP Counters are 32-bit unsigned integers
      */
    public long data ;
    /** Creates a new instance of COUNTER */
    public COUNTER(long data) {
	this.data = data ;
    }
    
    public long toInt64() { return data ; }
    public int asn_type() { return type.toByte() ; }
    
    public ASN_TYPE type() {
        return type ;
    }
    
    public Object toJavaObject() {
        return new Long(data) ;
    }
    
    
    
}

/*
 * $Log: COUNTER.java,v $
 * Revision 1.1.1.1  2003/02/07 23:56:50  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
