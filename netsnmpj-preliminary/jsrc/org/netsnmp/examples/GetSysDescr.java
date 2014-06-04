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
 * A simple example.  Performs a query against a host for the sysDescr object.
 * 
 * <pre>
 * java -Djava.library.path=SYSTEM-ARCH org.netsnmp.examples.GetSysDescr host community
 * 
 * SYSTEM-ARCH may be one of:  linux-i386 solaris-sparc Win32-x86
 * 
 * Please refer to the INSTALL file for further installation information.  
 * </pre>
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class GetSysDescr {
     
    static OID sysDescrOID ;
      
    /*
     *  Class that receives and processes the returned data.  When the response from
     * the remote agent is received, the actionPerformed method is called
     */
    static class MyListener implements NetSNMPAction {
    	
    		/*
    		 * This method will be called from within the NetSNMP thread
    		 * that processes SNMP transactions.  
    		 * 
    		 * By synchronizing this method, and calling 'send' from within a 
    		 * synchronized {} block synchronized on this object we guarantee
    		 * that this method does not start until the main thread has sent
    		 * its pdu and is 'waiting' for the response to be processed.  
    		 */
        public synchronized boolean actionPerformed(int result, NetSNMPSession sess, PDU rcvPdu, Object o) {
            ASNValue val = rcvPdu.findValue(sysDescrOID) ;
            
            /*
             * notifies a thread calling the 'wait' method on this object
             * to return.  However, that wait call will not return until
             * this method returns, releasing the 'synchronized' lock.  
             */
            this.notify() ;
            
            /*
             * Check to make sure that we got a response.  
             */
            if( result == NetSNMP.STAT_TIMEOUT ) {
                System.err.println("Timeout no response") ;
                return false ; // don't call other registered listeners
            }
                      
            System.out.println("got sysDescr = " + val) ;                      
            return true ; // continue to call other registered listeners
        }
    }

    public static void usage() {
        System.out.println("usage:") ;
        System.out.println("java org.netsnmp.examples.GetSysDescr hostname community") ;
    }
    
    public static void main(String[] args) throws MIBItemNotFound, NetSNMPSendError, IllegalStateException, InterruptedException {
        NetSNMPSession sess ;
        NetSNMPAction myListener = new MyListener() ;
                 
        if( args.length < 2 ) {
            usage() ;
            System.exit(2) ;
        }
        String host, community ;
        
        host = args[0] ;
        community = args[1] ;  
               
        /*
         * Open a session to the agent  
         */
				sess = new NetSNMPSession(host, community) ;
				
				/*
				 * Add the listener to the session to process incoming pdus.  
				 */
				sess.addListener(myListener) ;
				
        /*
         * Get the Object Identifier(OID) of the System Descriptor instance.
         */
        sysDescrOID = new DefaultOID("SNMPv2-MIB::sysDescr.0") ;
                 
        /*
         * Create a PDU to request the System Descriptor with
         */
				PDU pdu = new PDU(NetSNMP.MSG_GET) ;
        
        /*
         * Add the Object Identifier of the System Descriptor to the pdu
         */
        pdu.addNullEntry(sysDescrOID) ;
        
        synchronized( myListener ) {
        	/*
        	 * send the pdu to the remote agent
        	 */
					sess.send(pdu, null) ;
					/*
					 * wait for the result.  Waits until the 'notify' method
					 * is called on the myListener object and the actionPerformed
					 * method returns.  
					 */
					myListener.wait() ;
				} // synchronized
        
    }
    
}

/*
 * $Log: GetSysDescr.java,v $
 * Revision 1.4  2003/05/07 00:50:33  aepage
 * more comments and clearer ordering of code.
 *
 * Revision 1.3  2003/04/29 15:35:54  aepage
 * periodic checkin
 *
 * Revision 1.2  2003/03/29 00:01:57  aepage
 * new thread architecture
 *
 * Revision 1.1  2003/02/10 00:58:23  aepage
 * initial checkin
 *
 * Revision 1.2  2003/02/10 00:11:57  aepage
 * prerelease checkin
 *
 * Revision 1.1  2003/02/09 11:00:46  aepage
 * Initial Checkin
 *
 * Revision 1.1.1.1  2003/02/07 23:56:53  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:31:55  aepage
 * minor fix to use NetSNMP class
 *
 * Revision 1.3  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 * Revision 1.2  2003/02/07 14:05:53  aepage
 * Refactored NetSNMPPDU to PDU
 *
 * Revision 1.1  2003/02/04 23:19:19  aepage
 * Initial Checkins
 *
 */

