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

/**
 * Base class for value types used for getting/setting data from remote Agents
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public abstract class ASNValue implements Comparable, java.io.Serializable {
       
    /**
     * Converts the value to a boolean.
     *
     * @throws NetSNMPBadValue if this value is not a BOOLEAN
     */
    public boolean toBoolean() throws org.netsnmp.NetSNMPBadValue { throw new org.netsnmp.NetSNMPBadValue("attempt to convert " + this.getClass().getName() + " to boolean") ; }


    /**
     * Converts the value to a java int.
     *
     * @throws NetSNMPBadValue if this value is not some integer type
     */
     public int     toInt() throws org.netsnmp.NetSNMPBadValue { throw new org.netsnmp.NetSNMPBadValue("attempt to convert " + this.getClass().getName() + " to Int") ; }

    
    /**
     * Converts the value to java long.
     *
     * @throws NetSNMPBadValue if this value is not some long type
     */
    public long    toInt64() throws org.netsnmp.NetSNMPBadValue { throw new org.netsnmp.NetSNMPBadValue("attempt to convert " + this.getClass().getName() + " to int64") ; }

    /**
     * Converts the value to an integer.
     *
     * @throws NetSNMPBadValue if this value is not an OCTECT_STR
     */

    public byte [] toOctetString() throws org.netsnmp.NetSNMPBadValue { throw new org.netsnmp.NetSNMPBadValue("attempt to convert " + this.getClass().getName() + " to OctetString") ; }

    /**
     * Converts the value to an OBJECTOID
     *
     *  @throws NetSNMPBadValue if this value cannot be converted to an OBJECTID
     */
    public int []  toOBJECTID() throws org.netsnmp.NetSNMPBadValue { throw new org.netsnmp.NetSNMPBadValue("attempt to convert " + this.getClass().getName() + " to OBJECTID") ; }

    /**
     * Converts the value to a Java Object
     */
    public abstract Object toJavaObject() ;
    
    public abstract org.netsnmp.ASN_TYPE type() ;
    public abstract int asn_type() ;
         
    public int compareTo(Object o) {
    	return compareTo((Comparable)o) ;
    }
    
    
    
}

/*
 * $Log: ASNValue.java,v $
 * Revision 1.6  2003/06/07 16:00:25  aepage
 * remove unncessary import
 *
 * Revision 1.5  2003/06/01 15:22:00  aepage
 * pre merge checkins
 *
 * Revision 1.4  2003/04/30 21:05:50  aepage
 * doc fixes
 *
 * Revision 1.3  2003/04/22 17:55:44  aepage
 * Serializable
 *
 * Revision 1.2  2003/03/24 12:35:18  aepage
 * added comparable interface
 *
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
