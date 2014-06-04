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
 * ASN_TYPE.java
 *
 * Created on December 29, 2002, 1:41 PM
 */

package org.netsnmp;

/**
 *  typesafe enum pattern class for ASN_TYPES from Effective Java
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class ASN_TYPE {

    /** Creates a new instance of ASN_TYPE */
    public final String name ;
    public final byte value ;
    private ASN_TYPE(String name, byte value) {
        this.name = name ;
        this.value = value ;
        lookup_table[value] = this ;
    }
    
    public static ASN_TYPE [] lookup_table = new ASN_TYPE[255] ;
    public static ASN_TYPE intToType(int t) { return lookup_table[t] ; }

    public static class asn_boolean extends ASN_TYPE {
        private asn_boolean(String name, byte value) {
            super(name, value) ;
        }
	public boolean toBoolean(byte [] data) { return data[0] == 0x00 ? false : true ; }

    }

    
    public static class asn_integer extends ASN_TYPE {
	private asn_integer(String name, byte value) {
            super(name, value) ;
        }
    }
    
    public static class asn_bit_str extends ASN_TYPE {
	private asn_bit_str(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_octet_str extends ASN_TYPE {
	private asn_octet_str(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_null extends ASN_TYPE {
	private asn_null(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_object_id extends ASN_TYPE {
	private asn_object_id(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_sequence extends ASN_TYPE {
	private asn_sequence(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_ipaddress extends ASN_TYPE {
	private asn_ipaddress(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_counter extends ASN_TYPE {
	private asn_counter(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_gauge extends ASN_TYPE {
	private asn_gauge(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_unsigned extends ASN_TYPE {
	private asn_unsigned(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_timeticks extends ASN_TYPE {
	private asn_timeticks(String name, byte value) {
            super(name, value) ;
        }
    }

    public static class asn_counter64 extends ASN_TYPE {
	private asn_counter64(String name, byte value) {
            super(name, value) ;
        }
    }
    

    
    public String toString() { return name ; }
    public byte toByte() { return value ; }

    public static final byte ASN_APPLICATION = (byte)0x40 ;

    public static final ASN_TYPE ASN_BOOLEAN = new asn_boolean("ASN_BOOLEAN", (byte)0x01) ;
    public static final ASN_TYPE ASN_INTEGER = new asn_integer("ASN_INTEGER", (byte)0x02) ;
    public static final ASN_TYPE ASN_BIT_STR = new asn_bit_str("ASN_BIT_STR", (byte)0x03) ;
    public static final ASN_TYPE ASN_OCTET_STR = new asn_octet_str("ASN_OCTET_STR", (byte)0x04) ;
    public static final ASN_TYPE ASN_NULL = new asn_null("ASN_NULL", (byte)0x05) ;
    public static final ASN_TYPE ASN_OBJECT_ID = new asn_object_id("ASN_OBJECT_ID", (byte)0x06) ;
    public static final ASN_TYPE ASN_SEQUENCE = new asn_sequence("ASN_SEQUENCE", (byte)0x10) ;
    
    public static final ASN_TYPE ASN_IPADDRESS = new asn_ipaddress("ASN_IPADDRESS", (byte)(ASN_APPLICATION | 0x00)) ;
    public static final ASN_TYPE ASN_COUNTER = new asn_counter("ASN_COUNTER", (byte)(ASN_APPLICATION | 0x01)) ;
    
    public static final ASN_TYPE ASN_GAUGE = new asn_gauge("ASN_GAUGE", (byte)(ASN_APPLICATION | 0x02)) ;
    public static final ASN_TYPE ASN_UNSIGNED = new asn_unsigned("ASN_UNSIGNED", (byte)(ASN_APPLICATION | 0x02)) ; // RFC 1902 same as gauge
    
    public static final ASN_TYPE ASN_TIMETICKS = new asn_timeticks("ASN_TIMETICKS", (byte)(ASN_APPLICATION | 0x03)) ;
    public static final ASN_TYPE ASN_COUNTER64 = new asn_counter64("ASN_COUNTER64", (byte)(ASN_APPLICATION | 0x06)) ;
}

/*
 * $Log: ASN_TYPE.java,v $
 * Revision 1.2  2003/06/07 15:58:14  aepage
 * removal of unused code
 *
 * Revision 1.1.1.1  2003/02/07 23:56:48  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:40  aepage
 * Initial Checkins
 *
 */
