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

package org.netsnmp.framework;

import org.netsnmp.OID;
import org.netsnmp.PDU;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public interface GetElement {
	
	/**
	 * Signals to the object that the object has been 'reset' and should
	 * re-query any necessary information.  By convention a reset should be delivered before
	 * the first query.
	 */
	public void reset() ;
	
	/**
	 * called to configure/reconfigure the component
	 */
	public void configure() ;
	
	/**
	 * shutdown the operations of this element 
	 */
	public void shutdown() ;
	
	/**
	 * Get Query is starting
	 */
	public void start() ;
	
	/**
	 *  Called if an error occurs during processing
	 * @param pdu PDU object that returned the error
	 */
	public void handleError(PDU pdu) ;
	
	/**
	 *  @return an array of oids to be fetched from the remote agent
	 */
	public OID [] oids() ;
	
	/**
	 * @return true if the oid list has changed since the last fetch.
	 * returning true will force the the framework to rebuild the PDU
	 * for this particular element each time
	 */
	public boolean hasChanged() ;
	
	/**
	 *  apply values returned from a remote agent.  NOTE:  it is not gaurenteed that all the 
	 * elements requested by the 'oids' method will be returned here.  The framework will take
	 * leave to 'span' oids across multiple PDUs as it sees fit.  It should also be expected
	 * that entries in this PDU may not have been asked for by the oids() call.  
	 */
	public void setPDU(PDU values) throws Throwable ;
	
	/**
	 * @return the number of milliseconds to repeat this query.  If value is < 0 element will not
	 * be resubmitted.  
	 */
	public long repeat() ;
	
	/**
	 * Called when a query of this element timesout
	 */
	public void timeout() ;
}
