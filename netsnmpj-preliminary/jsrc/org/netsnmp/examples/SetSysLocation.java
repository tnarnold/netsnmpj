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

import org.netsnmp.* ;
import org.netsnmp.ASN.OCTET_STR ;

/**
 * A simple example that performs a 'SET' operation on the location of the localhost system.
 * 
 * NOTE:  
 *    In order for this example to work, the 'community' parameter must have Write access to 
 * the SNMPv2-MIB::sysLocation.0 object, if it does not this example will not work.  Please ensure
 * that you have a community name with that access before you proceed.  If you are not sure of this
 * consult the documentation for the SNMP agent that you are addressing or your network administrator.
 * 
 * IT IS RECOMMENDED THAT YOU DO NOT RUN THIS EXAMPLE WITH AN AGENT THAT YOU ARE NOT RESPONSIBLE FOR.
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class SetSysLocation {
	/**
	 * Class that will receive and process the response from the remote agent.
	 */
	public static class MyListener implements NetSNMPAction {
		
		boolean success = true ;
		
		/**
		 * @see org.netsnmp.NetSNMPAction#actionPerformed(int, NetSNMPSession, PDU, Object)
		 */
		public synchronized boolean actionPerformed(int result, NetSNMPSession session, PDU pdu, Object o) throws Throwable {
			if( result == NetSNMP.STAT_TIMEOUT ) {
				System.err.println("Operation timed out.  Ensure that your remote agent is running and that the community parameter is correct") ;
				success = false ;
				this.notify() ; // notify that the operation has completed
				return true ; // keep calling other registerred listeners, if any
			} // if
			
			if( pdu.errStatus != 0 ) {
				System.err.println("Set operation failed.  Ensure that the security info you specified provides write access to the sysLocation Object") ;
				success = false ;
				this.notify() ; // notify that the operation has completed
				return true ;
			}	
			
			this.notify() ; // notify that the operation has completed
			return true ;
		}

	}
	
	/**
	 *  Issue a helpful usage message
	 */
	private static void usage() {
		System.out.println("usage: org.netsnmp.examples.SetSysLocation <hostname> <community> <new system location>") ;	
	}

	public static void main(String[] args) {
		NetSNMPSession session ;
		MyListener listener ;
		PDU pdu ;
		OID sysLocationOID ;
		
		if( args.length < 3 ) {
			usage() ;
			System.exit(1)  ;	
		}
		String host, community, newLocation ;
		
		host = args[0] ;
		community = args[1] ;
		newLocation = args[2] ;
		
		/*
		 *  Open a session to the remote host
		 */
		session = new NetSNMPSession(host, community) ;
		
		/*
		 * Create a PDU that will perform a 'SET' operation
		 */
		pdu = new PDU(NetSNMP.MSG_SET) ;
		
		/*
		 * Create an OID for the systemLocation 
		 */
		try {
		 	sysLocationOID = new DefaultOID("SNMPv2-MIB::sysLocation.0") ;
		}
		catch( MIBItemNotFound e ) {
		 	System.err.println("The sysLocation Object was not found.") ; 
		 	System.err.println("Please ensure that the MIBDIRS and MIBS") ;
		 	System.err.println("environmental variables are set properly");
		 	System.exit(2) ;
		 	return ; // NOT REACHED
		}
		/*
		 * Add an OCTET_STR value to the PDU that will contain the new
		 * system location.
		 */
		pdu.addEntry(sysLocationOID, new OCTET_STR(newLocation)) ;
		
		/*
		 * create a new listener instance and add it to the listeners for the session
		 */
		 listener = new MyListener() ;
		 session.addListener(listener) ;
		 
		/*
		 * The actionPerformed method will be called on a different thread.   Synchronize
		 * this thread with the thread that it will be called upon with a 'synchronized' block.
		 */
		 
		synchronized ( listener ) {
			try {
				/*
				 *  Send the pdu to the remote agent
				 */
				session.send(pdu, null) ;
				
				
				/*
				 * wait for the listener actionPerformed method to notify that
				 * operation has been completed
				 */
				listener.wait() ;
				
				if( !listener.success ) {
					System.err.println("operation failed") ;
					System.exit(2) ;
				}
			} // try
			catch (NetSNMPSendError e) {
				System.err.println("An error occurred sending the pdu") ;
			} 
			catch (InterruptedException e) {
				System.err.println("the wait operation was interrupted") ;
			}
			
		} // synchronized
	}
}
