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
 * NO_SUCH_OBJECT.java
 *
 * Created on January 6, 2003, 8:37 AM
 */

package org.netsnmp.ASN;

import org.netsnmp.*;

/**
 *  Class used to substitute for an object when the object is not
 * found in an agent.
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class NO_SUCH_OBJECT extends ASNValue {
    
    public static final NO_SUCH_OBJECT NO_SUCH_OBJECT = new NO_SUCH_OBJECT() ;
    public static final ASN_TYPE type = ASN_TYPE.ASN_NULL ;
    
    /** Creates a new instance of NO_SUCH_OBJECT */
    private NO_SUCH_OBJECT() {
    }
    
    public int asn_type() { return type.toByte() ; }
    
    public ASN_TYPE type() {
        return type ;
    }
    
    public Object toJavaObject() {
        return NO_SUCH_OBJECT ;
    }
}

/*
 * $Log: NO_SUCH_OBJECT.java,v $
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
