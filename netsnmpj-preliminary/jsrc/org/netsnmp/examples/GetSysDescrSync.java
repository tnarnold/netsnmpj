
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


package org.netsnmp.examples ;

import org.netsnmp.* ;
import org.netsnmp.ASN.* ;

/**
 * A simple example.  Performs a query against the localhost
 * for the sysDescr object.   This example uses the 'NetSNMPSyncSession'
 * object.
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class GetSysDescrSync {

	public static void main(String[] args) {
		
		if( args.length < 2 ) {
			System.err.println("usage: org.netsnmp.GetSysDescrSync host community") ;	
			System.exit(2) ;
		}
		
		NetSNMPSyncSession sess = new NetSNMPSyncSession(args[0], args[1]) ;
		
		ASNValue response = null ;
		try {
			response = sess.get("SNMPv2-MIB::sysDescr.0");
		}
		catch (MIBItemNotFound e) {
			System.err.println("The requested OID was not found in the local MIB") ;
		}
		catch (Exception e) {
			e.printStackTrace();
			return ;
		}	
		System.out.println(response);
	}
}

/*
 * $Log: GetSysDescrSync.java,v $
 * Revision 1.2  2003/04/29 15:35:54  aepage
 * periodic checkin
 *
 * Revision 1.1  2003/03/29 00:00:42  aepage
 * initial checkin
 *
 */
