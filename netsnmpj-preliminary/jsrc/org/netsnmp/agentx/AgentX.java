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



/**
 * Interfaces to be implemented to provide an AgentX subagent.    Any class
 * that is to act as an agent should add the AgentX interface to its list of
 * implements and for each operation that you would need the agent to perform add
 * the appropriate GET, GETNEXT, GETBULK, SET_RESERVE1, SET_RESERVE2, SET_ACTION,
 * SET_UNDO, SET_COMMIT, SET_FREE interface.
 * 
 * @see org.netsnmp.NetSNMP#registerAgentX
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */


package org.netsnmp.agentx;

import org.netsnmp.* ;
import org.netsnmp.ASN.* ;

public interface AgentX {

	// values correspond to those found in agent_handler.h
	public final int CAN_GETANDGETNEXT = 0x01 ;
	public final int CAN_SET  = 0x02 ;
	public final int CAN_GETBULK = 0x04 ;
	public final int RONLY = CAN_GETANDGETNEXT ;
	public final int RWRITE = (CAN_GETANDGETNEXT | CAN_SET) ;
	
	public interface GET {
		/**
		 * @param oid object being fetched
		 * @return value of the target object
		 */
		public ASNValue GET(OID oid) ;	
	}
	
	public interface GETNEXT {
		/**
		 * @param oid object being fetched
		 * @return value of the target object
		 */
		public ASNValue GETNEXT(OID oid) ;
	}
	
	public interface GETBULK {
		/**
		 * @param oid object being fetched
		 * @return value of the target object
		 */
		public ASNValue GETBULK(OID oid) ;
	}
	
	public interface SET_RESERVE1 {
		/**
		 * @param oid identifier of the object being set
		 * @param type data type to be set
		 * @return true if successful
		 */
		public boolean SET_RESERVE1(OID oid, ASN_TYPE type) ;
	}
	
	public interface SET_RESERVE2 {
		/**
		 * @param oid identifier of object being set
		 * @return true if successful
		 */
		public boolean SET_RESERVE2(OID oid) ;
	}
	
	public interface SET_ACTION {
		/**
		 * 
		 * @param oid identifier of the object being set
		 * @param value value to set the target OID to
		 * @return true if successful
		 */
		public boolean SET_ACTION(OID oid, ASNValue value) ;
	}
	
	public interface SET_UNDO {
		/**
		 * 
		 * @param oid identifier of the object being undone
		 * @return true if successful
		 */
		public boolean SET_UNDO(OID oid) ;
	}
	
	public interface SET_COMMIT {
		/**
		 * 
		 * @param oid identifier of the object being set
		 * @return true if successful
		 */
		public boolean SET_COMMIT(OID oid) ;
	}
	
	public interface SET_FREE {
		/**
		 * 
		 * @param oid identifier of the object being set
		 * @return true if successful
		 */
		public boolean SET_FREE(OID oid) ;
	}
	
}

