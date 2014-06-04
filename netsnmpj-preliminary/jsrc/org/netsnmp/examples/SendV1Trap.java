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
import org.netsnmp.NetSNMPSendError;
import org.netsnmp.NetSNMPSession;
import org.netsnmp.PDU;
import org.netsnmp.ASN.INTEGER;

/**
 * Send a Link Up
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */

// snmptrap  -v  1  -c  public  manager enterprises.netSnmp test-hub 3 0 '' interfaces.iftable.ifentry.ifindex.1 i 1

public class SendV1Trap {

  public static void main(String[] args) throws MIBItemNotFound, NetSNMPSendError {
  	NetSNMPSession s = new NetSNMPSession() ;
  	PDU pdu ;
  	
  	s.setSNMPVersion(NetSNMP.SNMPv1) ;
  	s.setPeerName("localhost:162") ;
  	s.setCommunity("public") ;
  	s.open() ;
  	
  	pdu = new PDU(NetSNMP.MSG_TRAP) ; // SNMPv1 Trap
  	pdu.enterprise = new DefaultOID("NET-SNMP-MIB::netSnmp") ;
  	pdu.trap_type = 3 ;
  	pdu.specific_type = 0 ;
  	
  	pdu.addEntry("IF-MIB::ifEntry.ifIndex.1", new INTEGER(1)) ;
  	
  	s.send(pdu, null) ; // NOTE there will be no response
  }
}
