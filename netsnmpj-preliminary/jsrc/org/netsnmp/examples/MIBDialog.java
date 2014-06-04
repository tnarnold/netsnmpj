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

package org.netsnmp.examples;

/**
 * Example of how to display a mib browser
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class MIBDialog {

  public static void main(String[] args) {
  	org.netsnmp.swingui.MIBDialog dlg = new org.netsnmp.swingui.MIBDialog(null, true) ;
  	
  	/*
  	 * Add some of the most popular mibs
  	 */
  	dlg.addDefaultMIBs() ;
  	
  	/*
  	 * Show the Dialog until the user presses OK or Cancel
  	 */
  	dlg.show() ;
  	
  	if( dlg.good )
  		System.out.println(dlg.getOID().toText());
  }
}
