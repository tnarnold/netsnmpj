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
 * A class to contain various warnings and messages.  It is hoped that by encapsulating
 * all such messages that data associated with the class will only be loaded when necessary,
 * and that if they aren't needed they won't be contributing to the memory footprint.  
 *
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class Warnings {
    
    /** never meant to be instantiated*/
    private Warnings() {
    }
    
    public static void loadNativeLibraryOrProvideWarning(String libName) {
        try {
                System.loadLibrary(libName) ;
            }
            catch( UnsatisfiedLinkError e ) {
	       System.err.println(e) ;
               if ( e.toString().indexOf("libnetsnmp.so") != -1  || e.toString().indexOf("libsnmp.dll") != -1 ) {
                    System.err.println("##");
                    System.err.println("## could not find libnetsnmp.so.  Please checked your LD_LIBRARY_PATH setting(unix/linux");
                    System.err.println("## or your PATH setting under MS-Windows.   It may also be that ");
                    System.err.println("## net-snmp does not appear to be installed.  If so, please install net-snmp 5.0.7 or higher") ;
                    System.err.println("## before proceeding further.  See http://net-snmp.sourceforge.net");
                    System.err.println("##");
                    System.exit(2) ;
                }
               else if ( e.toString().indexOf("libnetsnmpmibs.so") != -1  || e.toString().indexOf("netsnmpmib.dll") != -1 ) {
                    System.err.println("##");
                    System.err.println("## could not find libnetsnmpmibs.so.  Please checked your LD_LIBRARY_PATH setting(unix/linux");
                    System.err.println("## or your PATH setting under MS-Windows.   It may also be that ");
                    System.err.println("## net-snmp does not appear to be installed.  If so, please install net-snmp 5.0.7 or higher") ;
                    System.err.println("## before proceeding further.  See http://net-snmp.sourceforge.net");
                    System.err.println("##");
                    System.exit(2) ;
                }
               else if ( e.toString().indexOf("libnetsnmphelpers.so") != -1  || e.toString().indexOf("netsnmphelpers.dll") != -1 ) {
                    System.err.println("##");
                    System.err.println("## could not find libnetsnmphelpers.so.  Please checked your LD_LIBRARY_PATH setting(unix/linux");
                    System.err.println("## or your PATH setting under MS-Windows.   It may also be that ");
                    System.err.println("## net-snmp does not appear to be installed.  If so, please install net-snmp 5.0.7 or higher") ;
                    System.err.println("## before proceeding further.  See http://net-snmp.sourceforge.net");
                    System.err.println("##");
                    System.exit(2) ;
                }
                else if ( e.toString().indexOf("libnetsnmpagent.so") != -1  || e.toString().indexOf("netsnmpagent.dll") != -1 ) {
                    System.err.println("##");
                    System.err.println("## could not find libnetsnmpagent.so.  Please checked your LD_LIBRARY_PATH setting(unix/linux");
                    System.err.println("## or your PATH setting under MS-Windows.   It may also be that ");
                    System.err.println("## net-snmp does not appear to be installed.  If so, please install net-snmp 5.0.7 or higher") ;
                    System.err.println("## before proceeding further.  See http://net-snmp.sourceforge.net");
                    System.err.println("##");
                    System.exit(2) ;
                }
                else if( e.toString().indexOf("libcrypto.so") != -1 ) {
                    System.err.println("##") ;
                    System.err.println("## could not find required openssl libraries.  Please check your LD_LIBRARY_PATH(unix/linux) or PATH(windows) setting") ;
                    System.err.println("## a typical location for the ssl library intallation is /usr/local/ssl/lib") ;
                    System.err.println("## See http://www.openssl.org") ;
                    System.err.println("##");
                }
                else if( e.toString().indexOf("libstdc++.so") != -1 ) {
                    System.err.println("##") ;
                    System.err.println("## could not find required stdc++ library.  Please check your LD_LIBRARY_PATH(unix/linux) or PATH(windows) setting") ;
		    System.err.println("## please check your linux vendor for this rpm/package") ;
                    System.err.println("##");
                }
                else if( e.toString().indexOf("dependent libraries") != -1 ) {
                	System.err.println("##") ;
                	System.err.println("## A support library for netsnmpj was not found.") ;
                	System.err.println("## please check to make sure that the libsnmp.dll library");
                	System.err.println("## is within the PATH(windows) or LD_LIBRARY_PATH(unix)") ;
                	System.err.println("##");
                }
                else if( e.toString().indexOf(libName) != -1 ) {
                    System.err.println("##");
                    System.err.println("## Could not find the " + libName + " library.  ") ;                    System.err.println("## Please check your java.library.path setting.") ;
                    System.err.println("## Currently java.library.path is set to:  ") ;
                    System.err.println("## "  + System.getProperty("java.library.path"));
                    System.err.println("##");
                    System.exit(2) ;
                }
                else {
                    System.err.println("" + e);
                    e.printStackTrace() ;
                }
            }
    }
    
    
    
}
