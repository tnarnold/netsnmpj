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

/**
 *  Handling class for loading MIB modules and translating
 * text descriptions into OIDs.  Methods here roughly correspond
 * to those found in the netsnmp mib_api.  (see man(3) mib_api)
 *
 * By default the following modules are read at startup:<br>
 * <pre>
 * IP-MIB, IF-MIB, TCP-MIB, UDP-MIB, SNMPv2-MIB, RFC1213-MIB, UCD-SNMP-MIB
 * </pre>
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class MIB {
	
	private static class intToLeafCompare implements java.util.Comparator {
		public int compare(Object a, Object b) {
			return ((leaf)a).id - ((Integer)b).intValue() ;
		}
	}
 
    public static class leaf implements Comparable {
        public final boolean hasChildren ;
        public final int id ;
        public final String label ;

        protected leaf(int id, String label, boolean hasChildren) {
            this.id = id ;
            this.label = label ;
            this.hasChildren = hasChildren ;
        }
		
		

        public int compareTo(Object obj) {
            return this.id - ((leaf)obj).id ;
        }
        public String toString() { return label + '(' + id + ')' ; }

    }

    private MIB() {
        
    }
    /**
     * add a directory to search for mib modules.  NOTE:  does not load any
     * files or modules.
     *
     * @return number of files found in directory
     */
    public synchronized static native int addMIBDir(String dir) ;
	
	public static leaf findLeaf(leaf[] leaves, int id) throws MIBItemNotFound {
		int idx ;
		
		idx = java.util.Arrays.binarySearch(leaves, new Integer(id), new intToLeafCompare()) ;
		if( idx < 0 )
			throw new MIBItemNotFound() ;
		
		return leaves[idx] ;
	}
    public synchronized native static leaf[] findLeaves(OID oid) throws MIBItemNotFound ;
    
    /**
     * Reads and parses a new MIB file, making the definitions within
     * available for lookup.
     * 
     * @param fname path name of the file read 
	 	 *
     */
    public synchronized static native void readMIB(String fname) throws java.io.FileNotFoundException ;
    
    /**
     *  Reads and parses a MIB module
     *
     * @param module of the module to be parsed
     */
    public synchronized static native void readModule(String module) ;
    
    /**
     * looks up and translates the  text description
     */
    public synchronized static native OID readObjID(String descriptor) throws MIBItemNotFound ;
	
	/**
	 * looks up a MIB or aborts.  Recommended use is for static initializers.
	 */
	
	public static OID readObjIDOrAbort(String descriptor) {
		
		try{
			return readObjID(descriptor) ;
		}
		catch (MIBItemNotFound e) {
			System.err.println("could not find OID " + e.descr);
			
		}
		
		System.exit(2) ;
		return null ;
	}
    
    public synchronized native static leaf[] treeHead() ;
    
    
	static {
			Warnings.loadNativeLibraryOrProvideWarning("netsnmpj") ;
	}
   

}

/*
 * $Log: MIB.java,v $
 * Revision 1.10  2003/06/01 16:03:49  aepage
 * merget from release-0.2.1
 *
 * Revision 1.9.2.1  2003/05/21 00:47:26  aepage
 * fixes to absorb MIB functions into the main library in order to
 * support RH7.3 and perhaps other flavors as well.
 *
 * Revision 1.9  2003/04/30 14:14:33  aepage
 * doc updates and member sort.
 *
 * Revision 1.8  2003/04/29 15:48:12  aepage
 * comments
 *
 * Revision 1.7  2003/04/28 22:41:45  aepage
 * renamed one of the routines for better grammatical consistency and
 * worked up some better doc comments for tools like Eclipse.
 *
 * Revision 1.6  2003/04/27 16:30:39  aepage
 * Synchronized methods for thread safety.
 *
 * Revision 1.5  2003/03/31 00:43:08  aepage
 * removal of unuzed variables.
 *
 * Revision 1.4  2003/03/19 15:49:22  aepage
 * library loading done through Warnings class.  This allows the output a
 * common tutorial message to correct common library issues.
 *
 * Revision 1.3  2003/02/27 17:36:52  aepage
 * Moved loadLibrary call to bottom of class definition.  This corrects
 * several problems related to class intitialization order in relation to
 * when the libraries are loaded.
 *
 * Revision 1.2  2003/02/15 18:07:03  aepage
 * added a convenience function to allow the lookup of an Object without
 * an exception.  If lookup fails the process will exit.  The intended use
 *
 * Revision 1.1.1.1  2003/02/07 23:56:48  aepage
 * Migration Import
 *
 * Revision 1.5  2003/02/07 22:01:52  aepage
 * java doc comments
 *
 * Revision 1.4  2003/02/07 13:47:13  aepage
 * doc comments on MIB functions and new native functions.
 *
 * Revision 1.3  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.2  2003/02/06 01:53:51  aepage
 * refined the 'find' method for finding leaves and made the findLeaves method
 * take an OID as an arguments instead of an int [].
 *
 * Revision 1.1  2003/02/04 23:17:40  aepage
 * Initial Checkins
 *
 */

