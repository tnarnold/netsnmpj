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
/*
 * snmpgetRunner.java
 *
 * Created on January 6, 2003, 6:44 PM
 */

package org.netsnmp.util;
import org.netsnmp.* ;
import java.util.* ;
import java.io.* ;


/**
 * This class runs 'snmpget' in a separate process and extracts its result.
 * THIS IS <B>NOT</B> the means by which the netsnmpj library acquires data.
 * This is a utility class used in testing to confirm results acquired by
 * the library.
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class snmpgetRunner {
    
	static public void main(String args[]) {
        snmpgetRunner runner = new snmpgetRunner("localhost", "abecrombe") ;
        String result = "failed";
        OID oidResult = null ;
        int intResult = -1 ;
        
        try {
            result = runner.getString(new DefaultOID("1.3.6.1.2.1.system.sysDescr.0")) ;
            
            intResult = runner.getInt(new DefaultOID("1.3.6.1.2.1.system.sysUpTime.0")) ;
            
            oidResult = runner.getOID(new DefaultOID("1.3.6.1.2.1.system.sysObjectID.0")) ;
        }
        catch(Exception e) {
            System.out.println("exception " + e) ;
        }
        System.out.println("got result = \"" + result + "\"") ;
        System.out.println("got int result = \"" + intResult + "\"") ;
        System.out.println("got oid result = " + oidResult.toString()) ;
    }
    
    static String commandName = "snmpget" ;
    static String options[] = {"-Oqvnt", "-v" , "2c" } ;
    
    String hostname, v2Community ;
	private ArrayList localOptions = new ArrayList() ;
    
    /** Creates a new instance of snmpgetRunner */
    public snmpgetRunner(String hostname, String v2Community) {
        this.hostname = hostname ;
        this.v2Community = v2Community ;
    }
	
	public void addOption(String opt) {
		localOptions.add(opt) ;
		
	}
	
	public void addOptions(String opts[]) {
		int i ;
		for( i = 0 ; i < opts.length ; i++ )
			addOption(opts[i]) ;
	}
	
    
    private String BuildAndRunCommand(OID oid) throws Exception {
        String line ;
        StringBuffer result = new StringBuffer() ;
        int i, err ;
        Process proc ;
        ArrayList args = new ArrayList() ;
        BufferedReader rd, rdErr ;
        
        args.add(commandName) ;
        for( i = 0 ; i < options.length ; i++ )
            args.add(options[i]) ;
		
		args.addAll(localOptions) ;
        
        args.add("-c") ;
        args.add(v2Community) ;
        args.add(hostname) ;
        args.add(oid.toString()) ;

        try {
            proc = Runtime.getRuntime().exec((String[])args.toArray(new String[0])) ;
            
            rd = new BufferedReader(new InputStreamReader(proc.getInputStream())) ;
            rdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream())) ;
            proc.waitFor() ;
            err = proc.exitValue() ;
            if( err != 0 ) {
                line = rdErr.readLine() ;
		if( line.indexOf("Timeout") != -1 || line.indexOf("No Response") != -1 )
		    throw new java.io.IOException("timed out") ;
                throw new Exception(line) ;
            }
            
            while( (line = rd.readLine()) != null ) {
                result.append(line) ;
            }
        }
		catch( InterruptedException e ) {
            System.out.println("got Interrupted " + e) ;
        }
        
        return result.toString() ;
    }
    
    public String getString(OID oid) throws Exception {
        String result = BuildAndRunCommand(oid) ;
		
		return result ;
    }
	
	public String getStringOrAbort(OID oid) {
		try {
			return getString(oid) ;
		}
		catch (Throwable e) {
			e.printStackTrace(System.err) ;
			System.err.println("getting string " + oid.toText() + " failed");
			System.exit(2) ;			
		}
		return null ; // NOT REACHED
	}
    
    
    public OID getOID(OID oid) throws Exception {
        String result = BuildAndRunCommand(oid) ;
        
        // extract the String data and convert to an oid
        return new DefaultOID(result) ;
    }
	
	public OID getOIDOrAbort(OID oid) {
		try {
			return getOID(oid) ;
		}
		catch (Throwable e) {
			e.printStackTrace(System.err) ;
			System.err.println("getting OID " + oid.toText() + " failed");
			System.exit(2) ;
		}
		return null ; // NOT REACHED
	}
    
    public int getInt(OID oid) throws Exception {
        String result = BuildAndRunCommand(oid) ;
        
        return Integer.parseInt(result) ;
    }
	
	public int getIntOrAbort(OID oid) {
		try {
			return getInt(oid) ;
		}
		catch (Throwable e) {
			e.printStackTrace(System.err) ;
			System.err.println("getting int " + oid.toText() + " failed");
			System.exit(2) ;
		}
		
		return -1 ; // NOT REACHED
	}
}

/*
 * $Log: snmpgetRunner.java,v $
 * Revision 1.5  2003/03/31 00:43:06  aepage
 * removal of unuzed variables.
 *
 * Revision 1.4  2003/02/27 23:34:38  aepage
 * fix for finally block
 *
 * Revision 1.3  2003/02/27 18:34:28  aepage
 * added xOrAbort methods to facilitate testing.
 *
 * Revision 1.2  2003/02/25 21:11:01  aepage
 * fix that corrects test failures on win32
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
