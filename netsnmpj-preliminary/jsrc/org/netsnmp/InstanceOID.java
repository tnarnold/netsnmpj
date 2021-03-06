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

package org.netsnmp ;

/**
 * A Convenience class that could save memory if used judiciously.
 *
 *  Represents a specific instance of a given object
 *  @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class InstanceOID extends DefaultOID {
	public final int instance ;
	
	public InstanceOID(OID object, int instance) {
		super(object) ;
		this.instance = instance ;
	}
	
	
	public int[] oids()
	{
		int [] returnOids = new int[oids.length+1] ;
		
		System.arraycopy(oids, 0, returnOids, 0, oids.length) ;
		returnOids[oids.length] = instance ;
		
		return returnOids ;
	}
	
	public int length()
	{
		return oids.length + 1 ;
	}
	
}

/*
 * $Log: InstanceOID.java,v $
 * Revision 1.2  2003/02/09 22:35:45  aepage
 * make instance oid a subclass of DefaultOID
 *
 * Revision 1.1.1.1  2003/02/07 23:56:48  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
