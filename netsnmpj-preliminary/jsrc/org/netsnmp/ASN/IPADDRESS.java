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
 * IPADDRESS.java
 *
 * Created on January 1, 2003, 9:21 AM
 */

package org.netsnmp.ASN;

import org.netsnmp.*;
/**
 * Class represents an IP Address.  data consists of a array of 4 bytes representing the elements
 * of an IPv4 address.
 * 
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class IPADDRESS extends OCTET_STR {
       
    public static final ASN_TYPE type = ASN_TYPE.ASN_IPADDRESS ;
  
    /** Creates a new instance of IPADDRESS */
    public IPADDRESS(byte [] data) {
        super(data) ;
    }
    
    public int asn_type() { return type.toByte() ; }
    
    /**
     * returns the raw bytes of the address
     */
    public Object toJavaObject() { 
    	return data ;
    }
    
    public ASN_TYPE type() {
        return type ;
    }
    /**
     *  return a dotted decimal representation of this ip address.
     */
    public String toString() {
    	StringBuffer sb = new StringBuffer() ;
    	
    	sb.append(Byte.toString(data[0])) ;
    	sb.append(".") ;
    	sb.append(Byte.toString(data[1])) ;
    	sb.append(".") ;
    	sb.append(Byte.toString(data[2])) ;
    	sb.append(".") ;
    	sb.append(Byte.toString(data[3])) ;
    	   	
    	return sb.toString() ;
    }
}

/*
 * $Log: IPADDRESS.java,v $
 * Revision 1.3  2003/06/01 15:22:00  aepage
 * pre merge checkins
 *
 * Revision 1.2  2003/04/01 16:57:02  aepage
 * added toString() method.
 *
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
