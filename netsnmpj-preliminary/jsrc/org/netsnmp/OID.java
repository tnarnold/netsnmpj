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
 * OID.java
 *
 *
 * Created on December 23, 2002, 10:47 AM
 */

package org.netsnmp;

/**
 * Object Identifier.  Identifier for MIB variables and instances, consisting
 * of a series of non-negative integers.  Constructors are available for building
 * identifiers from integers or text equivielents.
 *
 *  For example...
 *  <pre>
 *  The descriptor for the system that a remote agent resides on may be
 *  identified as either
 *
 *  1.3.6.1.2.1.1.1.0
 *
 * or
 *
 * iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0
 * </pre>
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public interface OID extends Comparable {
	public int[] oids() ;
	public int length() ;
	public String toText() ;
	public OID append(OID oid) ;
	public ASN_TYPE getASNType() ;
	
	/**
    * Compares up to n places in this oid
    *
    * @param oid to compare this to
    * @param n compare to n places
    * @return a negative integer, zero or a positive integer
    * if this object is less than, equal to or greaten than the OID
* it is being compared to to n places.
    *
    */
    public int compareTo(OID oid, int n) ;
}

/*
 * $Log: OID.java,v $
 * Revision 1.3  2003/06/01 15:22:01  aepage
 * pre merge checkins
 *
 * Revision 1.2  2003/03/30 22:59:44  aepage
 * removal of unneeded imports
 *
 * Revision 1.1.1.1  2003/02/07 23:56:50  aepage
 * Migration Import
 *
 * Revision 1.5  2003/02/07 13:55:13  aepage
 * Removed the toString() method.  Including it here, does not compell
 * the implementation in implementing classes since the method exists in
 * java.lang.Object.  Is this a bug in the language?  It was inclduded
 * here to compel the implentation of a specific toString Method.
 *
 * Revision 1.4  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.3  2003/02/06 01:52:37  aepage
 * Added append method
 *
 * Revision 1.2  2003/02/05 15:15:59  aepage
 * change apparently added by CodeGuide IDE
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */


