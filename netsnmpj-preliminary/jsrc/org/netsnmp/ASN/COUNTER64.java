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
 * Used to represent a COUNTER64 type.  Note:  Java has no 'unsigned' type
 * to represent an unsigned 64 bit variable.  
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class COUNTER64 extends COUNTER {
	
	public static final ASN_TYPE type = ASN_TYPE.ASN_COUNTER64 ;
	
	public int asn_type() { return type.toByte() ; }
    
	public ASN_TYPE type() {
			return type ;
	}
	
	public COUNTER64(long data) {
		super(data) ;
	}
}

/*
 * $Log: COUNTER64.java,v $
 * Revision 1.2  2003/06/07 16:01:08  aepage
 * support for 64bit counters
 *
 * Revision 1.1  2003/06/02 14:37:01  aepage
 * Initial Checkin
 * 
 */