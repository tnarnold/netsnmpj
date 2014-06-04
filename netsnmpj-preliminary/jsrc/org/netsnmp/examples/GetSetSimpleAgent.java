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

import java.io.IOException;

import org.netsnmp.DefaultOID;
import org.netsnmp.MIBItemNotFound;
import org.netsnmp.NetSNMP;
import org.netsnmp.NetSNMPBadValue;
import org.netsnmp.OID;
import org.netsnmp.ASN.ASNValue;
import org.netsnmp.ASN.INTEGER;
import org.netsnmp.agentx.AgentX;

/**
 * A simple example of creating a agentX sub agent. 
 * <br>
 * NOTE:   This example requires that you have a master agent running,
 * and that its AgentX support is enabled and its AgentX port is accessible.
 * <br>
 * <pre>
 *    snmpd -x agentXmasterHost:port
 * </pre>
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class GetSetSimpleAgent implements AgentX, AgentX.GET, AgentX.SET_ACTION {

	private int value = 0 ;

	/**
   * @see org.netsnmp.agentx.AgentX.GET#GET(org.netsnmp.OID)
   */
  public ASNValue GET(OID oid) {
    
    return new INTEGER(value);
  }

  public boolean SET_ACTION(OID oid, ASNValue newValue) {
   
    	try {
        value = newValue.toInt() ;
      }
      catch (NetSNMPBadValue e) {
        return false ;
      }
    	
		return true ;    
  }
  
  public static void main(String args[]) throws MIBItemNotFound, IOException {
  	OID oid ;
  	if( args.length < 1 ) {
  		System.err.println("usage:  SetGetSimpleAgent agentXmasterHost:port [OID]");
  		System.exit(2) ;
  	}
  	
  	if( args.length < 2 )
  		oid = new DefaultOID("UCD-SNMP-MIB::unknown.0") ;
  	else
			oid = new DefaultOID(args[1]) ;
  	
  	AgentX agent = new GetSetSimpleAgent() ;
  	
		NetSNMP.setAgentXSocket(args[0]) ;
  	NetSNMP.registerAgentX(oid, agent, AgentX.RWRITE) ;
  	
  	System.out.println("Agent Running:") ;
  	System.out.println("Get:");
  	System.out.println("  snmpget -c community agentXmaster " + oid.toText()) ;
  	System.out.println("Set:");
  	System.out.println("  snmpset -c community agentXmaster " + oid.toText() + " i value") ;
  	System.out.println("") ;
  	System.out.println("Press any key to terminate") ;
  	System.in.read() ;
		System.exit(0) ;
  }
}
