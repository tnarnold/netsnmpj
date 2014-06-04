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

import org.netsnmp.DefaultOID;
import org.netsnmp.MIBItemNotFound;
import org.netsnmp.NetSNMP;
import org.netsnmp.NetSNMPAction;
import org.netsnmp.NetSNMPSendError;
import org.netsnmp.NetSNMPSession;
import org.netsnmp.OID;
import org.netsnmp.PDU;
import org.netsnmp.ASN.ASNValue;
import org.netsnmp.ASN.END_OF_MIBVIEW;

/**
 * An example of how to 'walk' via snmp
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class snmpwalk {

	static OID startOID ;

	private static class walkListener implements NetSNMPAction {

    /* (non-Javadoc)
     * @see org.netsnmp.NetSNMPAction#actionPerformed(int, org.netsnmp.NetSNMPSession, org.netsnmp.PDU, java.lang.Object)
     */
    public synchronized boolean actionPerformed(int result, NetSNMPSession session, PDU pdu, Object o) throws Throwable {
      
      if( result  == NetSNMP.STAT_TIMEOUT ) {
      	System.err.println("timeout");
      	notify() ;
      	return false ;
      }

			
      OID oid = pdu.entries[0].oid ;
      ASNValue value = pdu.entries[0].value ;
      
      if( END_OF_MIBVIEW.isEndOfMib(value)) {
      	System.out.println("end of mib");
      	notify() ;
      	return false ;
      }
      
      if( startOID.compareTo(oid, startOID.length()) != 0 ) {
      	// reached the end of the tree we were checking
      	notify() ;
      	return false ;
      }
      
      System.out.println("" + pdu);
      
			PDU nextPDU = new PDU(NetSNMP.MSG_GETNEXT) ;
			nextPDU.addNullEntry(oid) ;
			
			session.send(nextPDU, null) ;
      
      return false;
    }
		
	}

  public static void main(String[] args) throws MIBItemNotFound, InterruptedException, NetSNMPSendError, IllegalStateException {
  	if( args.length < 3 ) {
  		System.err.println("usage: snmpwalk <host> <community> <tree>") ;
  		System.exit(2) ;
  	}
  	
  	NetSNMPAction listener = new walkListener() ;
  	
  	String host = args[0] ;
  	String community = args[1] ;
  	String oidStr = args[2] ;
  	
  	NetSNMPSession s = new NetSNMPSession() ;
  	s.setPeerName(host) ;
  	s.setCommunity(community) ;
  	s.setSNMPVersion(NetSNMP.SNMPv2c) ;
  	s.open() ;
  	
  	s.addListener(listener) ;
  	
  	startOID = new DefaultOID(oidStr) ;
  	
  	PDU startPDU = new PDU(NetSNMP.MSG_GETNEXT) ;
  	startPDU.addNullEntry(startOID) ;
  	
  	synchronized( listener ) {
  		s.send(startPDU, null) ;
  		listener.wait() ;
  	}
  	
  	System.out.println("done") ;
  }
}
