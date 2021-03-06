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
 * MIBJPanelEvent.java
 *
 * Created on February 5, 2003, 8:42 PM
 */

package org.netsnmp.swingui;

/**
 * Interface for passing events from the MIBJPanel to an application
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

import org.netsnmp.* ;

public interface MIBJPanelListener extends java.util.EventListener {
    void objectSelected(MIBEvent evt) ;
}

/*
 * $Log: MIBJPanelListener.java,v $
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
