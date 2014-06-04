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

package org.netsnmp.junittests;

import java.util.Random;

import org.netsnmp.ASN_TYPE;
import org.netsnmp.InstanceOID;
import org.netsnmp.MIB;
import org.netsnmp.NetSNMP;
import org.netsnmp.NetSNMPBadValue;
import org.netsnmp.NetSNMPSyncSession;
import org.netsnmp.OID;
import org.netsnmp.ASN.ASNValue;
import org.netsnmp.ASN.INTEGER;
import org.netsnmp.agentx.AgentX;
import org.netsnmp.util.snmpgetRunner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class AgentXTests extends TestCase {
	
	static Process proc ; // if using a local agent
	static boolean setupFailed ;
	
	
	private static final int Nagents = 250 ;
	private static int testValues[] = new int[Nagents] ;
	
	static {
		int i ;
		Random rnd = new Random(System.currentTimeMillis()) ;
		for( i = 0 ; i < Nagents ; i++ )
			testValues[i] = rnd.nextInt(Nagents) ;
	}
	
	private static snmpgetRunner runner = new snmpgetRunner(TestProperties.agentXMaster, TestProperties.agentXCommunity) ;
	
	static OID agentOID = MIB.readObjIDOrAbort("UCD-SNMP-MIB::unknown") ;

	protected void finalize() throws Throwable {
		if( proc != null )
			proc.destroy() ;
	}
	
	public static class callCheckAgent1 implements AgentX, AgentX.GET, 
	        																				AgentX.GETBULK, AgentX.GETNEXT, 
	        																				AgentX.SET_ACTION, AgentX.SET_COMMIT,
	        																				AgentX.SET_FREE, AgentX.SET_RESERVE1,
	        																				AgentX.SET_RESERVE2 {
	        																					
																					
	  
	  boolean receivedGET = false ;
	  boolean receivedGETBULK = false ;
		boolean receivedGETNEXT = false ;
		boolean receivedSET_ACTION = false ;
		boolean receivedSET_COMMIT = false ;
		boolean receivedSET_FREE = false ;
		boolean receivedSET_RESERVE1 = false ;
		boolean receivedSET_RESERVE2 = false ;	    			
					
		int value = 1 ;
	  																		
	  /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.GET#GET(org.netsnmp.OID)
     */
    public ASNValue GET(OID oid) {
      receivedGET = true ;
      
      return new INTEGER(value) ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.GETBULK#GETBULK(org.netsnmp.OID)
     */
    public ASNValue GETBULK(OID oid) {
      receivedGETBULK = true ;
      return new INTEGER(value) ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.GETNEXT#GETNEXT(org.netsnmp.OID)
     */
    public ASNValue GETNEXT(OID oid) {
      receivedGETNEXT = true ;
      
      return new INTEGER(value) ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.SET_ACTION#SET_ACTION(org.netsnmp.OID, org.netsnmp.ASN.ASNValue)
     */
    public boolean SET_ACTION(OID oid, ASNValue value) {
      receivedSET_ACTION = true ;
      
      try {
        this.value = value.toInt() ;
      }
      catch (NetSNMPBadValue e) {
        // TODO Auto-generated catch block
        return false ;
      }
      return true ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.SET_COMMIT#SET_COMMIT(org.netsnmp.OID)
     */
    public boolean SET_COMMIT(OID oid) {
      receivedSET_COMMIT = true ;
      return true ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.SET_FREE#SET_FREE(org.netsnmp.OID)
     */
    public boolean SET_FREE(OID oid) {
      receivedSET_FREE = true ;
      return true ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.SET_RESERVE1#SET_RESERVE1(org.netsnmp.OID, org.netsnmp.ASN_TYPE)
     */
    public boolean SET_RESERVE1(OID oid, ASN_TYPE type) {
      receivedSET_RESERVE1 = true ;
      return true ;
    }

    /* (non-Javadoc)
     * @see org.netsnmp.agentx.AgentX.SET_RESERVE2#SET_RESERVE2(org.netsnmp.OID)
     */
    public boolean SET_RESERVE2(OID oid) {
      receivedSET_RESERVE2 = true ;
      return true ;
    }
	}
	        																				
	
	public static class testAgent1 implements AgentX, AgentX.GET {
			public boolean testDone = false ;
			/**
			 * @see org.netsnmp.agentx.AgentX.GET#GET(org.netsnmp.OID)
			 */
			public synchronized ASNValue GET(OID oid) {
				int idx = oid.oids()[oid.length()-1] ;
				
				testDone = true ; 
				
				return new INTEGER(testValues[idx]) ;
			}
		}
	
	static {
		NetSNMP.setAgentXSocket(TestProperties.agentXSocket) ;
	}
	
	public AgentXTests() {
		/*
		 * The org.netsnmp.junittests.agentXSocket property needs to
		 * to be set in the netsnmpjTest.properties file
		 * 
		 */
		if( TestProperties.agentXSocket == null ) {
			System.err.println("## ERROR:   org.netsnmp.junittests.agentXSocket property needs to be set in netsnmpjTest.properties file");
			fail() ;
		}
			
	}
	
	public void testOperations() throws Throwable {
		NetSNMPSyncSession s = new NetSNMPSyncSession() ;
		callCheckAgent1 agent = new callCheckAgent1() ;
		int result ;
		
		try {
			NetSNMP.registerAgentX(agentOID, agent, AgentX.CAN_GETANDGETNEXT | 
			                                        AgentX.CAN_GETBULK |
			                                        AgentX.RWRITE) ;
			                                        
			s.setSNMPVersion(NetSNMP.SNMPv2c) ;
			s.setPeerName(TestProperties.agentXMaster) ;
			s.setCommunity(TestProperties.agentXCommunity) ;
			s.open() ;
	
	
			/* do the get operation */
			
			result = s.get(agentOID).toInt() ;
			
			if( result != agent.value )
				fail() ;
			if( !agent.receivedGET )
				fail() ;
		}
		finally {
			NetSNMP.unregisterAgentX(agentOID) ;
		}
		
		
	}
	
	/**
	 * tests the basic ability to add and remove multiple agent
	 */
	public void testAgentX() throws Throwable {
		NetSNMPSyncSession s = new NetSNMPSyncSession() ;
		AgentX testAgent = new testAgent1() ;
		// Add the agent
		OID testOIDs[] = new OID[Nagents] ;
		int i, val ;
		
		s.setSNMPVersion(NetSNMP.SNMPv2c) ;
		s.setPeerName(TestProperties.agentXMaster) ;
		s.setCommunity(TestProperties.agentXCommunity) ;
		s.open() ;
		
		for( i = 0; i < Nagents ; i++ ) {
			testOIDs[i] = new InstanceOID(agentOID, i) ;
			
			NetSNMP.registerAgentX(testOIDs[i], testAgent, AgentX.RONLY) ;
		}
		
		// check its response 
		
		for( i = 0 ; i < Nagents ; i++ ) {
			val = s.get(testOIDs[i]).toInt() ;
			if( testValues[i] != val  )
				fail() ;
		}
		
		// remove the agents
		
		for( i = 0 ; i < Nagents ; i++ ) {
			NetSNMP.unregisterAgentX(testOIDs[i]) ;
		}
	}

	public static Test suite() {
		return new TestSuite(AgentXTests.class) ;
	}
	
	public static void main(String args[]) throws Exception {
		junit.textui.TestRunner.run(AgentXTests.suite()) ;
		
		System.exit(0) ;
	}
}
