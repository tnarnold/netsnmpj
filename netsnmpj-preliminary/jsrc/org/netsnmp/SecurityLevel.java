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

package org.netsnmp;

import java.io.Serializable;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 * Security level enum
 */
public class SecurityLevel implements Serializable {
	public final int level ;
	public final String name ;
	
	public String toString() { return name ; }
	
	
	
	private SecurityLevel(int lv, String nm) {
		name = nm ;
		level = lv ;
	}
	
	public static SecurityLevel noAuth     = new SecurityLevel(1, "noAuth") ; // from snmp.h
	public static SecurityLevel authNoPriv = new SecurityLevel(2, "authNoPriv") ; // from snmp.h
	public static SecurityLevel authPriv   = new SecurityLevel(3, "authPriv") ; 
	
	/* 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if( obj == null ) return false ;
		return ((SecurityLevel)obj).level == level ;
	}

}

/*
 * $Log: SecurityLevel.java,v $
 * Revision 1.3  2003/04/22 17:55:11  aepage
 * test for null in equals method
 *
 * Revision 1.2  2003/04/18 01:29:20  aepage
 * serialization support
 *
 * Revision 1.1  2003/04/15 19:21:55  aepage
 * initial checkin
 *
 */
