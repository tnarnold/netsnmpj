
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

package org.netsnmp.util;

import java.util.ArrayList;

import org.netsnmp.DefaultOID;
import org.netsnmp.NetSNMP;
import org.netsnmp.NetSNMPAction;
import org.netsnmp.NetSNMPSendError;
import org.netsnmp.NetSNMPSession;
import org.netsnmp.OID;
import org.netsnmp.PDU;
import org.netsnmp.ASN.ASNValue;
import org.netsnmp.ASN.INTEGER;

/**
 * Utility class, based on the GET_NEXT request, to collect values from a column of objects.  
 * For example:
 * 
 * <pre>
 * ValueCollector c = new ValueCollector(String host, String community, new DefaultOID("IF-MIB::ifIndex")) ;
 * ints [] ifIndicies = c.ints() ;
 * </pre>
 * 
 * This would collect all of the indexes for installed interfaces.  
 * 
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class ValueCollector implements NetSNMPAction {

	private NetSNMPSession s;
	OID baseOID ;
	
	private boolean done ;
	Error error ;
	ArrayList values =  new ArrayList() ;
	
	private void startQuery(OID baseOID) throws NetSNMPSendError {
		PDU initialPdu ;
		
		this.baseOID = baseOID ;
		
		initialPdu = new PDU(NetSNMP.MSG_GETNEXT) ;
		
		initialPdu.addNullEntry(baseOID) ;
		
		s.addListener(this) ;
		s.send(initialPdu, null) ;
	}

	/**
	 * Create a new instance of a ValueCollector
	 * 
	 * @param host host to query
	 * @param community community to use
	 * @param baseOID OID to begin query
	 */
	public ValueCollector(String host, String community, OID baseOID) throws NetSNMPSendError {
		
		s = new NetSNMPSession(host, community);		
		
		startQuery(baseOID) ;
	}
	
	/**
	 *  Create a new instance of a ValueCollector using an exisiting session's
	 *  peer, community and version.
	 */
	public ValueCollector(NetSNMPSession copySession, OID baseOID) throws NetSNMPSendError {
		
		if( !copySession.isOpen() )
			throw new IllegalStateException("cannot create value collector on unopened session") ;
		
		s = new NetSNMPSession() ;
		s.setPeerName(copySession.getPeerName()) ;
		s.setCommunity(copySession.getCommunity()) ;
		s.setSNMPVersion(copySession.getSnmpVersion()) ;
		s.open() ;
		
		
		startQuery(baseOID) ;
	}
	
	/**
	 * blocks until the requested info has completed
	 */
	
	private synchronized void complete() throws RuntimeException {
		
		try {
			while( !done ) {
				wait() ;
				if( error != null )
					throw error ;
			}
		} catch (InterruptedException e) {
				throw new RuntimeException("interrupted waiting for results") ;
		}
	}
	
	public synchronized String [] strings() throws RuntimeException, Error {
		int i ;
		String [] returnArray ;
		
		complete() ;
		
		returnArray = new String[values.size()] ;
		
		for( i = 0 ; i < returnArray.length ; i++ )
			returnArray[i] = values.get(i).toString() ;
			
		return returnArray ;
	}
	
	public synchronized ASNValue [] values() {
		
		complete() ;
		
		return (ASNValue[])values.toArray(new ASNValue[0]) ;	
	}
	
	/**
	 *  wait for the results, and convert them to integers
	 */
	public synchronized int [] ints() throws RuntimeException, Error {
		int resultInts[] ;
		int i ;
		
		complete() ;
		
		resultInts = new int[values.size()] ;
		
		for( i = 0 ; i < resultInts.length ; i++ )
			resultInts[i] = ((INTEGER)values.get(i)).toInt() ;
		
		return resultInts ;
	}
	
	public String toString() {
		int i ;
		int [] indices = ints() ; // waits for completion
		
		StringBuffer sb = new StringBuffer() ;
		
		for( i = 0 ; i < indices.length ; i++ ) {
			sb.append(indices[i]) ;
			sb.append(" ") ;
		}
		
		return sb.toString() ;	
		
	}
	
	/**
	 * @return whether or not the indexes have been collected.   Can be used to poll
	 * the status of this object rather than having to wait for it.  
	 */
	public boolean isReady() { return done ; } 


	/**
	 *  routine called when query has been completed.  Base class routine is essentially
	 * a no-op.  Override to provide an asynchronous notification of complettion
	 * <pre>
	 * Example:
	 * 
	 * class myValueCollector extends ValueCollector {
	 * protected void onComplete() {
	 *    // completion action
	 * }
	 */
	protected void onComplete() {
	}
	
	/**
	 * @see org.netsnmp.NetSNMPAction#actionPerformed(int, NetSNMPSession, PDU, Object)
	 */
	public synchronized boolean actionPerformed(
		int result,
		NetSNMPSession session,
		PDU pdu,
		Object o)
		throws Throwable {
			
		if( result != 1 ) {
				error = new Error("fetch failed  err in pdu ==> " + pdu.errStatus + "/" + pdu.errIndex) ;
				done = true ;
				notify() ;
				return false ;	
			}
			
			/*
			 *  Check to see if we've compleded the query
			 */
			if( baseOID.compareTo(pdu.entries[0].oid, baseOID.length()) != 0 ) {
				done = true ; // set that we're done
				notify() ;	// notify anyone waiting
				onComplete() ;
				return false ;
			}
			try { 
				PDU nextPDU = new PDU(NetSNMP.MSG_GETNEXT) ;
				int i ;
				/*
				 *  NOTE:  by building a newPDU with null entries
				 * we reduce the traffic by not sending back data
				 * that will never be read anyway
				 */
				for (i = 0 ; i < pdu.entries.length ; i++ ) {
					values.add(pdu.entries[i].value) ;
					nextPDU.addNullEntry(pdu.entries[i].oid) ;
				}
				
				
				s.send(nextPDU, null) ;
			} 
			catch( Throwable e ) {
				error = new Error(e.toString()) ;
				done = true ;
				notify() ;
				return false ;	
			}
				
			
		return false;
	}
	
	public static void main(String[] args) throws Throwable {
		ValueCollector idxC = new ValueCollector("localhost", "abecrombe", new DefaultOID("IF-MIB::ifIndex")) ;
		ValueCollector speeds = new ValueCollector("paragon", "abecrombe", new DefaultOID("IF-MIB::ifSpeed")) ;
		
		System.out.println(idxC);
		System.out.println(speeds) ;
		
	}
}

/*
 * $Log: ValueCollector.java,v $
 * Revision 1.4  2003/06/01 15:23:57  aepage
 * organized imports
 *
 * Revision 1.3  2003/04/29 21:43:04  aepage
 * periodic checkin
 *
 * Revision 1.2  2003/04/18 01:46:20  aepage
 * refactoring
 *
 * Revision 1.1  2003/04/11 20:02:07  aepage
 * initial checkin
 *
 */
