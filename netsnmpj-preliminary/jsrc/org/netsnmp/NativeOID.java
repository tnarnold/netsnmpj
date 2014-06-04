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
 * NativeOID.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.netsnmp;

/**
 * OID class that supplies access to native OID services
 */
public abstract class NativeOID implements OID
{
    /**
     * Converts an oid to a dotted integer representation
     */
    public String toString() {
        StringBuffer sb = new StringBuffer() ;
        int i ;
		int oids[] = oids() ;
        for( i = 0 ; i < oids.length ; i++ )
            sb.append("." + oids[i]) ;
        return sb.toString() ;
    }

    /**
     * Compare to another object.  Used for implementing
     * the 'Comparable' interface for seaching and sorting
     *
     * @param obj Object to compare this OID to
     * @return a negative integer, zero or a positive integer
     * if this object is less than, equal to or greaten than the object
     * it is being compared to.
     *
     */
    public int compareTo(Object obj) {
        // start from the back, since that's where there is more differentiation
        
        OID oid = (OID)obj ;
		int [] localoids, objOids ;
        
        int diff = length() - oid.length() ;
        if( diff != 0 )
            return diff ;
       
		localoids = oids() ;
		objOids = ((OID)obj).oids() ;
		
        int i = localoids.length ;
        while( i != 0 ) {
            diff = localoids[--i] - objOids[i] ;
            if( diff != 0 ) return diff ;
        }
        return 0 ;
    }
	
   /**
    * Compares up to n places in this oid
    *
    * @param oid to compare this to
    * @param n compare to n places
    * @return a negative integer, zero or a positive integer
    * if this object is less than, equal to or greaten than the OID
* it is being compared to to n places.
    *
    */
    public int compareTo(OID oid, int n) {
        int i, diff ;
        if( n > oid.oids().length )
            n = oid.oids().length ;
		
        if( n > length() )
            n = length() ;
		
		int [] localoids, objOids ;
		localoids = oids() ;
		objOids = oid.oids() ;
        
        for( i = 0 ; i < n ; i++ ) {
            diff = localoids[i] - objOids[i] ;
            if( diff != 0 )
                return diff ;
        }
        return 0 ;
    }
		
	/**
	 * Creates a new OID object appending by appending oid to this one.  
	 * If this oid or the oid being appended is of zero length then the
	 * non-zero length OID will be returned, unless force copy is true
	 *
	 * @param oid OID object append
	 * @param forceCopy if true, oid will be appended even if one of the oids has no members
	 * @return new OID that is the concatenation of this OID and the oid parameter
	 */

	public OID append(OID oid, boolean forceCopy) {
		int [] oids ;
		
		if( (oid.length() == 0 || length() == 0) && !forceCopy ) {
			if( oid.length() == 0 )
				return this ;
			return oid ;
		}
		
		oids = new int[oid.length() + length()] ;
		
		System.arraycopy(oids(), 0, oids, 0, length()) ;
		System.arraycopy(oid.oids(), 0, oids, length(), oid.length()) ;
		
		return new DefaultOID(oids) ;
	}
	
	/**
	 * Creates a new OID object appending by appending oid to this one.  
	 * A new OID object is created even if this oid or the OID object to be
	 * appended is of zero-length.
	 */
	public OID append(OID oid) {
		return append(oid, true) ;
	}

	
	/**
	 * Converts an OID to a text representation.  Esentially calls snprint_objid
	 * and returns the result as a java.lang.String
	 * @return a text representation of the OID based on the currently
	 * loaded MIBs
	 *
	 * @see man(3) mib_api
	 */
	public native String toText() ;
	
	/**
	 * 
	 * @return the type of this OID
	 */
	public native ASN_TYPE getASNType() ;

	 static {
            Warnings.loadNativeLibraryOrProvideWarning("netsnmpj") ;
        }
        
}

/*
 * $Log: NativeOID.java,v $
 * Revision 1.6  2003/06/07 15:59:19  aepage
 * modifications to support the option to copy or not copy an OID when appending
 * a zero length oid.
 *
 * Revision 1.5  2003/06/01 16:02:57  aepage
 * post release-0.2.1 checkin
 *
<<<<<<< variant A
 * Revision 1.4  2003/06/01 15:22:01  aepage
 * pre merge checkins
>>>>>>> variant B
 * Revision 1.3.2.1  2003/05/21 00:47:26  aepage
 * fixes to absorb MIB functions into the main library in order to
 * support RH7.3 and perhaps other flavors as well.
======= end
 *
 * Revision 1.3  2003/03/19 15:49:22  aepage
 * library loading done through Warnings class.  This allows the output a
 * common tutorial message to correct common library issues.
 *
 * Revision 1.2  2003/02/27 17:36:52  aepage
 * Moved loadLibrary call to bottom of class definition.  This corrects
 * several problems related to class intitialization order in relation to
 * when the libraries are loaded.
 *
 * Revision 1.1.1.1  2003/02/07 23:56:49  aepage
 * Migration Import
 *
 * Revision 1.5  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
