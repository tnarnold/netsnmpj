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
 *
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>@users.sourceforge.net
 */

package org.netsnmp;

import java.io.Serializable;

public class DefaultOID extends NativeOID implements Serializable {
    /**
     * Get the path elements of the OID
     * @return the individual elements of the OID as an array of integers
     */
	public int[] oids() { return oids ; }
    /**
     * length of the OID
     *
     *  @returns the number of individual elements
     */
	public int length() { return oids.length ; }
 
    protected final int oids[] ;
    
    
    /** Creates a new instance of OID
     *
     *  Creates an OID from a text descriptor.
     *  The identifier is a '.' separated list of mib leaves.  The identifier
     *  may also consist of the integer equivalents for any given leaf.
     *
     *  <pre>
     *  Example:
     *
     *             oid = new OID("iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0") ;
     *
     *   or
     *
     *             oid = new OID("1.3.6.1.2.1.1.1.0") ;
     *
     * </pre>
     *
     * @throws MIBItemNotFound if the Item was not found in the
     * currently loaded mibs.  NOTE:   Finding an item within the
     * locally installed MIBs does not guarantee that it will be found
     * on a remote agent.
     */
    public DefaultOID(String descriptor) throws MIBItemNotFound {
        this(MIB.readObjID(descriptor)) ;
    }
    
    
    
	/**
	 * Creates an Empty OID
	 */
	public DefaultOID() {
		oids = new int[0] ;
	}
	
	/**
	 * Creates a new DefaultOID from an existing one
	 *
	 *
	 * @param srcOid
	 */
	public DefaultOID(OID srcOid) {
		oids = srcOid.oids() ;
	}
	
    /**
     * Creates a new oid from a text descriptor and a integer instance.
     * Useful for creating instances of separate entries in tables:
     *
     *  Example:
     * <pre>
     *  String ifSpeedDescr = "iso.org.dod.internet.mgmt.mib-2.interfaces.ifTable.ifEntry.ifSpeed" ;
     *  OID ifSpeeds[2] = new OID[2] ;
     *
     *  ifSpeeds[0] = new OID(ifSpeedDescr, 1) ;
     *  ifSpeeds[1] = new OID(ifSpeedDescr, 2) ;
     *  </pre>
     *
     * @param descriptor OID of Object
     * @param instance specific instance
     */
    public DefaultOID(String descriptor, int instance) throws MIBItemNotFound  {
        int tmp_array[] = MIB.readObjID(descriptor).oids() ;
        oids = new int[tmp_array.length + 1] ;
        
        System.arraycopy(tmp_array, 0, oids, 0, tmp_array.length) ;
        
        oids[tmp_array.length] = instance ;
    }
    
    /**
     * Creates an OID from another OID and a specific instance
     *
     * @param oid Object Identifier
     * @param inst Specific instance
     */
    public DefaultOID(OID oid, int inst) {
       
        oids = new int[oid.oids().length + 1] ;
        
        System.arraycopy(oid.oids(), 0, oids, 0, oid.oids().length) ;
        
        oids[oid.oids().length] = inst ;
    }
   
    /**
     * Creates and OID from an array of integer identifiers
     *
     * <pre>
     * Example:
     *
     * int sysDescrInts[] = { 1,3,6,1,2,1,1,1,0 } ;
     * OID oid = new OID(sysDescrInts) ;
     *
     *  equivalent to:
     *
     *  oid = new OID("iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0") ;
     *  </pre>
     */
    public DefaultOID(int[] descriptor) {
        oids = descriptor ;
    }
}


/*
 * $Log: DefaultOID.java,v $
 * Revision 1.3  2003/04/18 01:29:20  aepage
 * serialization support
 *
 * Revision 1.2  2003/02/09 22:35:45  aepage
 * make instance oid a subclass of DefaultOID
 *
 * Revision 1.1.1.1  2003/02/07 23:56:48  aepage
 * Migration Import
 *
 * Revision 1.6  2003/02/07 22:01:52  aepage
 * java doc comments
 *
 * Revision 1.5  2003/02/07 13:45:57  aepage
 * movement of methods to NativeOID
 *
 * Revision 1.4  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.3  2003/02/06 18:11:47  aepage
 * Moved native calls to the NativeOID abstract class
 *
 * Revision 1.2  2003/02/06 01:52:21  aepage
 * Added new constructors and the append method
 *
 * Revision 1.1  2003/02/04 23:17:40  aepage
 * Initial Checkins
 *
 */


