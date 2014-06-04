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


/**
 * Interface implemented by objects that will process the result of
 * an SNMP operation.
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public interface NetSNMPAction extends java.util.EventListener {
    /**
     *   Called when a session receives a pdu result
     *
     * @param result   SNMP Supporting library specific code
     * @param session  Session PDU was received on
     * @param pdu     The pdu received
     * @param o       Object supplied by caller to {@link org.netsnmp.NetSNMPSession#send send} method
     * @return   TRUE if other listeners are to be called,
     *           FALSE if not
     * @throws Throwable  Exception to be caught by the internal thread
     */
    public boolean actionPerformed(int result, NetSNMPSession session, PDU pdu, Object o) throws Throwable ;
}

/*
 * $Log: NetSNMPAction.java,v $
 * Revision 1.3  2003/04/30 21:06:58  aepage
 * doc fixes
 *
 * Revision 1.2  2003/02/27 17:34:21  aepage
 * comment fix
 *
 * Revision 1.1.1.1  2003/02/07 23:56:49  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 14:05:53  aepage
 * Refactored NetSNMPPDU to PDU
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:40  aepage
 * Initial Checkins
 *
 */
