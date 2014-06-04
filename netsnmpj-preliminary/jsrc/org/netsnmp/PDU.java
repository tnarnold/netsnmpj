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

import java.io.Serializable;

/**
 * Primary Data Unit
 *
 *  A message that sent to or received from a remote SNMP agent.
 *
 *  <pre>
 *
 *  import org.netsnmp.* ;
 *
 *  class mySysRequest {
 *
 *    {@link org.netsnmp.NetSNMPSession NetSNMPSession} sess = new org.netsnmp.NetSNMPSession("remotehost", "community") ;
 *    {@link org.netsnmp.PDU PDU} pdu = new PDU(NetSNMP.MSG_SET) ;
 *    final {@link org.netsnmp.OID OID} oid = new DefaultOID("iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0") ;
 *
 *    {@link org.netsnmp.NetSNMPAction NetSNMPAction}  l = new NetSNMPAction {
 *       public boolean actionPerformed(int result, {@link org.netsnmp.NetSNMPSession NetSNMPSession} sess,  {@link org.netsnmp.PDU PDU} receivedPDU, Object o) {
 *
 *       }
 *     }
 *   }
 *   
 *   
 *   sess.addListener(l) ;
 *   pdu.addNullEntry(oid) ;
 *
 *   sess.send(pdu, null) ;
 *
 *  </pre>
 */
public class PDU implements Serializable {
    
    /**
     * Entry of an individual data item.
     */
    public static class entry implements Comparable {
      public OID oid ;
      public org.netsnmp.ASN.ASNValue value ;
      
      public entry(OID o, org.netsnmp.ASN.ASNValue v) { oid = o ; value = v ; }
	  
	  // removal pending
	  /*
      public entry(int[] oid_descr, org.netsnmp.ASN.ASNValue v) {
          oid = new OID(oid_descr) ;
          value = v ;
      }
	   */
       public int compareTo(Object obj) {
           return oid.compareTo(((entry)obj).oid) ;
       }
       
       public String toString() {
           StringBuffer sb = new StringBuffer() ;
           
           sb.append(oid.toText()) ;
           sb.append(" ") ;
           sb.append(value.toString()) ;
           return sb.toString() ;
       }
    }
    
    /**
     *  Typesafe enum for specific PDU_COMMANDS
     */
    public static class PDU_COMMAND implements Serializable {
        public final int id ;
		public final String label ;
        private PDU_COMMAND(int id, String label) { this.id = id ; this.label = label ; }
		public String toString() { return label ; }
    }
		public byte agent_addr[] ; // for v1 traps ONLY, will be set if null
		public OID enterprise ;
  
    public entry entries[] = new entry[0] ;
    public int errStatus, errIndex ; // set upon return
		public int maxRepetitions = 1 ; // support for getbulk
		public int nonRepeaters = 1 ;
    protected int requestID ;
    private entry sortedEntries[] = null ;
		public int specific_type ;
		public int time = 0 ;
		
		/*
		 * Trap fields
		 */
		public int trap_type ;
 
    public PDU_COMMAND type ;
    public static int ASN_CONSTRUCTOR = 0x20 ;
   
    public static int ASN_CONTEXT = 0x80 ;

    /**
     * Command type to request values from a remote agent
     */
    public static final PDU_COMMAND SNMP_MSG_GET = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x00, "SNMP_MSG_GET") ;
	
		/**
		 * Command type to get 'bulk' repsonses from remote agents
		 */
		public static final PDU_COMMAND SNMP_MSG_GETBULK = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x05, "SNMP_MSG_GETBULK") ;
    
    /**
     * Command type to request the next object from a remote agent
     */
    public static final PDU_COMMAND SNMP_MSG_GETNEXT = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x01, "SNMP_MSG_GETNEXT") ;
		public static final PDU_COMMAND SNMP_MSG_INFORM = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x06, "SNMP_MSG_INFORM") ;
		public static final PDU_COMMAND SNMP_MSG_REPORT= new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x08, "SNMP_MSG_REPORT") ;
		
		
		public static final PDU_COMMAND SNMP_MSG_RESPONSE = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x02, "SNMP_MSG_RESPONSE") ;
    
    /**
     * Command type to set values from a remote agent
     */
    public static final PDU_COMMAND SNMP_MSG_SET = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x03, "SNMP_MSG_SET") ;
		public static final PDU_COMMAND SNMP_MSG_TRAP = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x04, "SNMP_MSG_TRAP") ;
		public static final PDU_COMMAND SNMP_MSG_TRAP2 = new PDU_COMMAND(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x07, "SNMP_MSG_TRAP2") ;
    public PDU() { this(SNMP_MSG_GET) ; }
    public PDU(PDU_COMMAND t) { type = t ; }
	
    
    public void addEntry(int [] oidDescr, org.netsnmp.ASN.ASNValue v) {
        addEntry(new DefaultOID(oidDescr), v) ;
    }
    
    /**
     * @param o  OID of the object being added
     * @param v  the value associated with the oid
     */
    
    public void addEntry(OID o, org.netsnmp.ASN.ASNValue v) {
    /*
     * Note, a choice has been made not use to a collections
     *  class or utility since a PDU is expected to be built once,
     *  and then continuously reused.  Striving for efficiency,
     *  a low-level array is used as the container for the values
     */
       
      entry [] new_entries = new entry[entries.length + 1] ;
     
			System.arraycopy(entries, 0, new_entries, 0, entries.length) ;
		
      new_entries[entries.length] = new entry(o, v) ;
      entries = new_entries ;
	  	sortedEntries = null ; // reset the array
    }
    
    
    /**
     * Add an entry into the PDU
     *
     * @param desc object ID descriptor
     * @return OID of the object added
     */
    public OID addEntry(String desc, org.netsnmp.ASN.ASNValue v) throws MIBItemNotFound {
        OID o = new DefaultOID(desc) ;
        addEntry(o, v) ;
        return o ;
    }
	
	
	/**
	 * Allows you to add a large ammount of oids all at once
	 * rather than one at a time
	 */
	public void addNullEntries(OID oids[]) {
		entry [] nullEntries = new entry[oids.length] ;
		entry [] newEntries = new entry[oids.length + entries.length] ;
		int i ;
		
		sortedEntries = null ; // reset the array
		for( i = 0 ; i < nullEntries.length ; i++ )
			nullEntries[i] = new entry(oids[i], org.netsnmp.ASN.NULL.NULL_VALUE) ;
		
		System.arraycopy(entries, 0, newEntries, 0, entries.length) ;
		System.arraycopy(nullEntries, 0, newEntries, entries.length, nullEntries.length) ;
		
		entries = newEntries ;
		
	}
	
	/**
	 * Add a null entry to the PDU
	 * @param desc OID of the entry
	 */
    public void addNullEntry(OID desc) {
        addEntry(desc, org.netsnmp.ASN.NULL.NULL_VALUE) ;
    }
    
    /**
     * Add a null entry to the PDU
     * @param desc descriptor of the object
     * @param instance instance of the object
     */
    public void addNullEntry(OID desc, int instance) {
        addNullEntry(new InstanceOID(desc, instance)) ;
    }
    
    /**
     * Adds a new null entry to the PDU
     * @param desc symbolic descriptor of the object
     * @return OID of the added object
     * @throws MIBItemNotFound if the Item was not found in the currently loaded MIBs
     */
    public OID addNullEntry(String desc) throws MIBItemNotFound {
        OID o = new DefaultOID(desc) ;
        addEntry(o, org.netsnmp.ASN.NULL.NULL_VALUE)  ;
        return o ;
    }
    
    /**
     * Adds a new null entry to the PDU
     * @param desc symbolc descriptor of the object
     * @param instance instance of the object
     * @throws MIBItemNotFound if the item was not found in the currently loaded MIBs
     */
    public void addNullEntry(String desc, int instance)  throws MIBItemNotFound {
        addNullEntry(new DefaultOID(desc, instance)) ;
    }
    
    /**
     * Append the entries from pdu to this one.
     * @param pdu PDU to append
     */
    public void append(PDU pdu) {
        int len = entries.length, i, j ;
        entry newEntries[] = new entry[len + pdu.entries.length] ;
        
        for( i = 0 ; i < entries.length ; i++ ) {
            newEntries[i] = entries[i] ;
        }
        
        for( j = 0 ; j < pdu.entries.length ; j++, i++) {
            newEntries[i] = pdu.entries[j] ;
        }
    }
    
    /**
     * @return error status of the PDU represented as a string
     */
    public native String errString() ; 
    
    /**
     * Retrieve a value from the pdu corresponding to the oid
     *
     * @param oid corresponding to the value within the PDU
     * @return value correpsonding to oid, null if no such entry
     */
    public org.netsnmp.ASN.ASNValue findValue(OID oid) {
        int i ;
       	if( sortedEntries == null )
       		setSortedEntries() ;
       
        i = java.util.Arrays.binarySearch(sortedEntries, new entry(oid, null)) ;
        if( i < 0 ) return null ;
        return sortedEntries[i].value ;
    }
    
    /**
     * Retrieives a value from the pdu corresponding to the oid and the instance
     * 
     * @param oid specifier for the object
     * @param inst particular instance of the object
     * @return value correpsonding to oid and instance, null if no such entry
     */
		public org.netsnmp.ASN.ASNValue findValue(OID oid, int inst) {
			return findValue(new InstanceOID(oid, inst)) ;
		}
    
    /**
     * @return number of values in this PDU
     */
    public int nValues() { return entries.length ; }
    
    public int requestID() { return requestID ; }
    
    /**
     * 
     * @param new_entries entries to replace the current entries with
     */
    public void setEntries(entry [] new_entries) {
        entries = new_entries ;
    }
    
    /**
     * @param oids OIDs of the entries to replace the current entries with
     * @param values values of the entries to replace the current entries with
     */
    public void setEntries(OID [] oids, org.netsnmp.ASN.ASNValue[] values)  {
        int i ;
        
        if( oids.length != values.length )
        	throw new IllegalStateException("number of oids not equal to the number of values") ;
        
        entries = new entry[oids.length] ;
        for( i = 0 ; i < oids.length ; i++ )
            entries[i] = new entry(oids[i], values[i]) ;
    }
    
    /**
     * Takes the entries and sorts them by the OID in preparation
     * for searching through them.  
     */
    private void setSortedEntries() {
    	sortedEntries = new entry[entries.length] ;
    	System.arraycopy(entries, 0, sortedEntries, 0, entries.length) ;
    	java.util.Arrays.sort(sortedEntries) ;
    }
    
    /**
     * Replaces the existing value corresponding to oid in the pdu.
     * if the entry doesn't exist it is added to the end
     *
     * @param oid object identifier for the value
     * @param value to set
     */
    public void setValue(OID oid, org.netsnmp.ASN.ASNValue value) {
        int i ;
        
        for( i = 0 ; i < entries.length ; i++ ) {
        	if( !entries[i].oid.equals(oid) )
        		continue ;
        	entries[i].value = value ; // replace the value
        	return ;
        }
				addEntry(oid, value) ;
       
    }
    /*
     * returns the current size of the pdu packet
     */
    public native int size() ;
    
    /**
     * @return string representation of the PDU to a string detailing its contents
     */
    public native String toString() ;

    /*
     * Note: when trying to access static fields, such as SNMP_MSG_GET
     * in the native library, this static {} block loadLibrary call
     * must come AFTER any static initialization statements.
     */
	
    
       static {
            Warnings.loadNativeLibraryOrProvideWarning("netsnmpj") ;
        }
}

/*
 * $Log: PDU.java,v $
 * Revision 1.15  2003/04/30 14:28:57  aepage
 * doc updates
 *
 * Revision 1.14  2003/04/30 14:21:40  aepage
 * doc fixes and member sorting
 *
 * Revision 1.13  2003/04/30 00:48:18  aepage
 * added errString method
 *
 * Revision 1.12  2003/04/29 17:04:12  aepage
 * trap support
 *
 * Revision 1.11  2003/04/22 17:54:14  aepage
 * sorted entries now in a separate array
 *
 * Revision 1.10  2003/04/18 01:30:12  aepage
 * serialization support
 *
 * Revision 1.9  2003/03/30 22:59:44  aepage
 * removal of unneeded imports
 *
 * Revision 1.8  2003/03/24 21:24:48  aepage
 * Correction of error in doc comment.
 *
 * Revision 1.7  2003/03/20 22:37:55  aepage
 * Uses warning class
 *
 * Revision 1.6  2003/02/28 20:19:10  aepage
 * added label and toString methods to PDU_COMMAND
 *
 * Revision 1.5  2003/02/27 17:35:31  aepage
 * New pdu commands in preparation for agent sessions.
 *
 * Revision 1.4  2003/02/23 21:32:43  aepage
 * Fix for really anonying class loading problem.
 *
 * Revision 1.3  2003/02/19 19:07:11  aepage
 * Support for GETBULK operations
 *
 * Revision 1.2  2003/02/15 18:08:06  aepage
 * dead code removal, and tuneup to use an instance oid.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:50  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/07 22:03:03  aepage
 * java doc comments
 *
 * Revision 1.1  2003/02/07 14:05:53  aepage
 * Refactored NetSNMPPDU to PDU
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */
