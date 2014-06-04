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

/**
 * SNMPVersion.java
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

package org.netsnmp;

import java.io.Serializable;

/**
 * Type self enum pattern used to specify which version of SNMP
 * is in use with sessions
 */
final public class SNMPVersion implements Serializable {
	private SNMPVersion(int version, String nm) { this.version = version ; name = nm ;}
	public final int version ;
	public final String name ;
	public String toString() { return name ; }
	static final SNMPVersion v1 = new SNMPVersion(0, "SNMPv1") ; // from snmp.h
	static final SNMPVersion v2c = new SNMPVersion(1, "SNMPv2c") ; // from snmp.h
	static final SNMPVersion v3  = new SNMPVersion(3, "SNMPv3") ; // from snmp.h
	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if( obj == null ) return false ;
		return ((SNMPVersion)obj).version == version ;
	}

}

