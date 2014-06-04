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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * A session with a remote agent.  Data, as a {@link org.netsnmp.PDU PDU} is sent to
 * the remote agent.  The remote agent will respond with a {@link org.netsnmp.PDU PDU}
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class NetSNMPSession implements Serializable {

		/**
     * Internal exception thrown when a read has been signalled
     * to end prematturely
     * 
     * @deprecated
     */
    public static class readInterrupt extends Throwable {
        protected readInterrupt(String msg) {
        }
    }
	
		public static interface serialEncryption {
			public byte[] decrypt(byte[] s) throws GeneralSecurityException ;
			public byte[] encrypt(byte[] s) throws GeneralSecurityException ;
		}
		
		private transient String authPassword ; // corresponds to the snmpcmd -A option
		
		
		private transient String community ;
		
		private byte[] contextEngineID = null ;
		
		private String contextName ;
		
		private int hashCode ;
		private boolean isOpen = false ;
    
    
    
    private NetSNMPAction [] listeners ;
		
		private transient String peerName ;
		private transient String privPassword ; // corresponds to the snmpcmd -X option
		
		private long rcvMsgMaxSize ;
		private int retries ;
		private transient byte [] securityAuthKey ; // derived from securityAuthPassword
		
		private OID securityAuthOID = NetSNMP.usmMD5AuthOID ;
		
		private byte[] securityEngineID = null ;
		SecurityLevel securityLevel = NetSNMP.authNoPriv ;
		 
		private int securityModel = -1 ;
		private String securityName ;
		private transient byte [] securityPrivKey ;
		
		
		private OID securityPrivOID = NetSNMP.usmDESPrivOID ;
		private transient serialEncryption serialEncryptor ; // set before a serialize operation to encipher secuirity info(or not)
		private boolean serializedEncrypt ; // true if password data was encrypted before being written
		private transient Exception serializeException = null ;
		
		private transient long sessionHandle ; // handle to internal session struct
		private long sndMsgMaxSize ;
		private long timeout ; 
	
		private SNMPVersion version = NetSNMP.SNMPv2c ;
		
		
	
		private static serialEncryption defaultEncryptor = null ;
    private static Object interruptAcknowledge = new Object() ; 

    private static Object readLock = new Object() ; 
		

    /** 
     * Creates a new instance of Session
     */
    public NetSNMPSession() {
        listeners = new NetSNMPAction[0] ;
        nativeInit() ;
				setSecurityAuthOID(NetSNMP.usmMD5AuthOID) ;
				setSecurityPrivOID(NetSNMP.usmDESPrivOID) ;
    }
    
    /**
     * Creates and opens an SNMPv2c session to a remote host
     *
     * @param peerName host name of the remote agent, may be in any format acceptable to 
     * net-snmp utilties.  Typically in the format of<br><br>
     *   <center>transport:hostname:portNumber</center><br>
     *   <center>hostname:portNumber  <i>(defaults to UDP transport)</i></center><br>
     *   <center>hostname  <i>(defaults to UDP transport and port 161)</i></center><br>
     * @param community community of the remote agent
     */
    public NetSNMPSession(String peerName, String community) throws IllegalStateException {
        this() ;
        setSNMPVersion(NetSNMP.SNMPv2c) ;
        setPeerName(peerName) ;
        setCommunity(community) ;
        open() ;
    }
    
    /**
     * Adds a listener that responds to PDUs from the remote agents.
     * When a pdu is received by this session the {@link
     * org.netsnmp.NetSNMPAction#actionPerformed} method is called
     *
     * @param listener object that implements the {@link org.netsnmp.NetSNMPAction#actionPerformed}
     *  method to process the information returned from the agent
     */
    public void addListener(NetSNMPAction listener) {
        int i, n = listeners.length ;
        
        NetSNMPAction newListeners[] = new NetSNMPAction[n+1] ;
        
        for( i = 0 ; i < n ; i++ ) {
           newListeners[i] = listeners[i] ;
        }
        newListeners[i] = listener ;
        listeners = newListeners ;
    }
    
    /**
     * Delete the listenen from the list of listeners.
     *
     * @param listener   listener to be deleted
     * @return  the listener deleted, null if not found.  If not found
     * listener list remains unchanged.
     */
    public NetSNMPAction deleteListener(NetSNMPAction listener) {
        int i, n = listeners.length ;
        NetSNMPAction tmp = null ;
        
        if( n == 0 )
            return null ;
		 		NetSNMPAction newListeners[] = new NetSNMPAction[n-1] ;
        
        for( i = 0 ; i < n-1 ; i++ ) {
            if( listener == listeners[i] ) {
                tmp = listeners[i] ;
                listeners[i] = listeners[n-1] ; // swap with one at the end
            newListeners[i] = listeners[i] ;
            }
        }
        if( tmp != null ) {
            listeners = newListeners ;
            return tmp ;
        }
        return null ;
    }
    
    protected void finalize() throws Throwable {
    	
    	synchronized( finalizingQueue ) {
    		finalizingQueue.addFirst(new Long(sessionHandle)) ;
    	}
    	
    	sessionHandle = 0 ;
    	
    	super.finalize() ;
    }

		/**
		 * @return the authentication password in use
		 */
		public String getAuthPassword() {
			return authPassword;
		}
    
    /**
     * @return the community
     */
    
    public String getCommunity() { return community ; }

		/**
		 * @return the context engine ID
		 */
		public byte[] getContextEngineID() {
			return contextEngineID;
		}

		/**
		 * @return context name in use
		 */
		public String getContextName() {
			return contextName;
		}

    /**
     * @return the peer name.  This is the name of the remote agent
     */
    public String getPeerName() { return peerName ; }

		/**
		 * @return the privacy password in use
		 */
		public String getPrivPassword() {
			return privPassword;
		}

		/**
		 * @return
		 */
		public long getRcvMsgMaxSize() {
			return rcvMsgMaxSize;
		}

		/**
		 * @return
		 */
		public int getRetries() {
			return retries;
		}

		/**
		 * @return
		 */
		public byte[] getSecurityAuthKey() {
			return securityAuthKey;
		}

		/**
		 * @return
		 */
		public OID getSecurityAuthOID() {
			return securityAuthOID;
		}

		/**
		 * @return
		 */
		public byte[] getSecurityEngineID() {
			return securityEngineID;
		}

		/**
		 * @return
		 */
		public SecurityLevel getSecurityLevel() {
			return securityLevel;
		}

		/**
		 * @return
		 */
		public int getSecurityModel() {
			return securityModel;
		}

		/**
		 * @return
		 */
		public String getSecurityName() {
			return securityName;
		}

		/**
		 * @return
		 */
		public byte[] getSecurityPrivKey() {
			return securityPrivKey;
		}

		/**
		 * @return
		 */
		public OID getSecurityPrivOID() {
			return securityPrivOID;
		}


		/**
		 * @return exception generated the underlying session during serialization
		 */
		public Exception getSerializeException() {
			return serializeException ;
		}

		/**
		 * The session handle is the native pointer to data specific to this
		 * session.  For debugging purposes it may be prudent to have access to it.
		 * @return the session handle
		 */
		public long getSessionHandle() {
			return sessionHandle;
		}
		
		/**
		 * @return
		 */
		public long getSndMsgMaxSize() {
			return sndMsgMaxSize;
		}
    
    /** Getter for property snmpVersion.
     * @return Value of property snmpVersion.
     * 
     * @see org.netsnmp.NetSNMP#SNMPv1
		 * @see org.netsnmp.NetSNMP#SNMPv2c
		 * @see org.netsnmp.NetSNMP#SNMPv3
     */
    public SNMPVersion getSnmpVersion() { return version ; }

		/**
		 * @return the curreent timeout in microseconds
		 */
		public long getTimeout() {
			return timeout;
		}

		/**
		 * @return the SNMP protocol version in use
		 * 
		 * 
		 */
		public SNMPVersion getVersion() {
			return version;
		}
		
		
		/**
		 * @return a hash code that can be used for storing object in 
		 * hashtable
		 */
		public int hashCode() {
			return hashCode ;
		}
    
    
    /**
     * @return true if the session is open
     */
    public boolean isOpen() {
    	
    	return isOpen ;
    }
    
    private static native void nativeFinalize(long sessionHandle) ;
    
    private native void nativeInit() ;
    
    /**
     * Opens the session given the current set of properties
     *
     * @throws IllegalStateException if the peername or community name
     * have not been set, or the lookup on the peer fails
	 	 *
     */
    public native void open() throws IllegalStateException ;

		private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
			nativeInit() ;
			serialEncryption cr = serialEncryptor != null ? serialEncryptor : defaultEncryptor ;
			
			in.defaultReadObject() ;
			if( serializedEncrypt ) {
				
				short len ;
				
				try {
					byte[] peerArray, communityArray, authArray, privArray ;
					
					len = in.readShort() ;
					peerArray = new byte[len] ;
					in.read(peerArray) ;
					
					len = in.readShort() ;
					communityArray = new byte[len] ;
					in.read(communityArray) ;
					
					len = in.readShort() ;
					authArray = new byte[len] ;
					in.read(authArray) ;				
						
					len = in.readShort() ;
					privArray = new byte[len] ;
					in.read(privArray) ;

					// wait until we've read all the data before we start to decrypt
					// so the file stream will remain in sync
					
					peerName = new String(cr.decrypt(peerArray)) ;
					community = new String(cr.decrypt(communityArray)) ;
					authPassword = new String(cr.decrypt(authArray)) ;
					privPassword = new String(cr.decrypt(privArray)) ;
				}
				catch (GeneralSecurityException e) {
					serializeException = e ;
					return ;
				}
			}
			else {
				peerName = (String)in.readObject() ;
				community = (String)in.readObject() ;
				authPassword = (String)in.readObject() ;
				privPassword = (String)in.readObject() ;
			}
			
			/*
			 * If the session was open when it was saved to the stream and
			 * it is not in need of decryption reopen the session
			 */
			if( isOpen  )
				open() ;
		}
    
    /**
     * Sends the pdu to the peer
     *
     * @param thePdu pdu to send
     * @param o object to be passed to the NetSNMPAction when a response
     * to this pdu is received.
     * @throws NetSNMPSendError if an error occurs in sending the pdu
	   * @throws IllegalStateException if the session is not open
     */
    public native void send(PDU thePdu, Object o) throws NetSNMPSendError, IllegalStateException ;
    
    /** Setter for authentication protocol
     * corresponds to the -a option for snmpcmd(1)
     * 
     * @see org.netsnmp.NetSNMP#usmNoAuthOID
     * @see org.netsnmp.NetSNMP#usmMD5AuthOID
     * @see org.netsnmp.NetSNMP#usmSHAAuthOID
     * 
     * @param oid Identifier for the authentication protocol to use 
     */
    public void setAuthenticationProtocol(OID oid) {
    	securityAuthOID = oid  ;
			updateHashCode() ;
    	updateSession() ;
		}

		/**
		 * @param string authorization password
		 */
		public void setAuthPassword(String string) {
			authPassword = string;
			updateHashCode() ;
			updateSession() ;
		}
		

		/**
		 * @param string the V1/V2c community
		 */
		public void setCommunity(String string) {
			community = string;
			updateSession() ;
		}

		/**
		 * @param bs
		 */
		public void setContextEngineID(byte[] bs) {
			contextEngineID = bs;
			updateHashCode() ;
			updateSession() ;
		}

		/**
		 * @param string
		 */
		public void setContextName(String string) {
			contextName = string;
			updateHashCode() ;
			updateSession() ;
		}

		/**
		 * Format of the hostname can be as follows:<br>
		 * <pre>
		 * <center>transport:hostname:portNumber</center><br>
     * <center>hostname:portNumber  <i>(defaults to UDP transport)</i></center><br>
     * <center>hostname  <i>(defaults to UDP transport and port 161)</i></center><br>
		 * </pre>
		 * 
		 * @param hostname name of the remote agent to connect to
		 * 
		 */
		public void setPeerName(String hostname) {
			if( hostname == null )
				throw new NullPointerException("null peername") ;
			peerName = hostname;
			updateHashCode() ;
			updateSession() ;
		}
    
    /**
     * Setter for privacy protocol
     * corresponds to the -x DES option for snmpcmd(1)
     * @see org.netsnmp.NetSNMP#usmNoPrivOID
     * @see org.netsnmp.NetSNMP#usmDESPrivOID
     * @param oid
     */
    public  void setPrivacyProtocol(OID oid) { 
    	securityPrivOID = oid ;
		updateSession() ; 
    }

		/**
		 * @param string
		 */
		public void setPrivPassword(String string) {
			privPassword = string;
			updateHashCode() ;
			updateSession() ;
		}

		/**
		 * @param l
		 */
		public void setRcvMsgMaxSize(long l) {
			rcvMsgMaxSize = l;
			updateHashCode() ;
			updateSession() ;
		}

		/**
		 * @param i
		 */
		public void setRetries(int i) {
			retries = i;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param bs
		 */
		public void setSecurityAuthKey(byte[] bs) {
			securityAuthKey = bs;
			
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param oid
		 */
		public void setSecurityAuthOID(OID oid) {
			securityAuthOID = oid;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param bs
		 */
		public void setSecurityEngineID(byte[] bs) {
			securityEngineID = bs;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param Security level to use
		 * 
		 * @see org.netsnmp.NetSNMP#noAuth
		 * @see org.netsnmp.NetSNMP#authNoPriv
		 * @see org.netsnmp.NetSNMP#authPriv
		 */
		public void setSecurityLevel(SecurityLevel level) {
			securityLevel = level;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param i
		 */
		public void setSecurityModel(int i) {
			securityModel = i;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param string security name corresponds to the snmpget -u <i>user</i>
		 * option
		 */
		public void setSecurityName(String string) {
			securityName = string;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param key encryption key to use for V3 privacy correpsonds to the
		 * snmpget -X key option
		 */
		public void setSecurityPrivKey(byte[] key) {
			securityPrivKey = key;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param oid identifier of the privacy protocol to use
		 * @see org.netsnmp.NetSNMP#usmDESPrivOID
		 */
		public void setSecurityPrivOID(OID oid) {
			securityPrivOID = oid;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * Sets the encryption object to use to encrypt/decrypt v1/v2 community
		 * names and v3 auth/priv passwords during serialization/deserialization
		 * 
		 * @param encryption to use
		 */
		public void setSerialEncryptor(serialEncryption encryption) {
			serialEncryptor = encryption;
		}

		/**
		 * @param size largest message to send
		 */
		public void setSndMsgMaxSize(long size) {
			sndMsgMaxSize = size ;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param version sets the SNMPVersion to be used.
		 * 
		 * @see org.netsnmp.NetSNMP#SNMPv1
		 * @see org.netsnmp.NetSNMP#SNMPv2c
		 * @see org.netsnmp.NetSNMP#SNMPv3
		 */
		public void setSNMPVersion(SNMPVersion version) {
			this.version = version;
			updateSession() ;
			updateHashCode() ;
		}

		/**
		 * @param t timeout in microseconds before the first timeout
		 */
		public void setTimeout(long t) {
			timeout = t ;
			updateSession() ;
			updateHashCode() ;
		}
		private void updateHashCode() {
			StringBuffer sb = new StringBuffer() ;
			sb.append(peerName) ;
			sb.append(community) ;
			sb.append(contextName) ;
			sb.append(securityName) ;
			sb.append(privPassword) ;
			sb.append(authPassword) ;
			hashCode = sb.toString().hashCode() ;
		}
    
    private native void updateSession() ; // used when fields change on opened session
		
		private void writeObject(ObjectOutputStream out) throws java.io.IOException {
			
			if( serialEncryptor == null && defaultEncryptor == null ) {
				serializedEncrypt = false ;
				out.defaultWriteObject() ;
				
				out.writeObject(peerName) ;
				out.writeObject(community) ;
				out.writeObject(authPassword) ;
				out.writeObject(privPassword) ;
				
				return ;
			}
			
			serialEncryption cr = serialEncryptor != null ? serialEncryptor : defaultEncryptor ;
			serializedEncrypt = true ;
			
			try {
			 		byte [] array ;
					out.defaultWriteObject() ;
					
					array = cr.encrypt(peerName.getBytes()) ;
					out.writeShort((short)array.length) ;
					out.write(array) ;
					
					array = cr.encrypt(community.getBytes()) ;
					out.writeShort((short)array.length) ;
					out.write(array) ;
					
					array = cr.encrypt(authPassword.getBytes()) ;
					out.writeShort((short)array.length) ;
					out.write(array) ;
					
					array = cr.encrypt(privPassword.getBytes()) ;
					out.writeShort((short)array.length) ;
					out.write(array) ;					
			 }
			 catch( IOException e ) {
			 	throw e ;
			 } 
			 catch (GeneralSecurityException e) {
				serializeException = e ;
				return ;
			}
		}
		
    
    private static LinkedList processingQueue = new LinkedList() ;
    
    private static class processingRec {
    	NetSNMPSession s ;
    	PDU pdu ;
    	int result ;
    	Object o ;
    }
    
    private ArrayList processingThreads = new ArrayList() ;
    private static int availableThreads = 0 ;
    private static final int maxThreads ;
    private static int waitingForProcessing = 0 ;
    
    private static class ProcessingThread extends Thread {
      public void run() {
       	processingRec rec ;
       	
       	try {
       		while( true ) {
		       	synchronized( processingQueue ) {
		       		while( processingQueue.size() == 0 ) {
		       			availableThreads += 1 ;
		       			
		       			if( waitingForProcessing > 0 )
		       				processingQueue.notify() ;
		       			
		       			processingQueue.wait() ;
		       			availableThreads -= 1 ;
		       		}
		       		
		       		rec = (processingRec)processingQueue.removeLast() ;
		       		if( rec == null ) 
		       			return ; // signalling we're done
		       	} // sync
		       	
		       
		       	try {
		       		int i ;
							boolean flag = true ;
              for ( i = 0 ; i < rec.s.listeners.length && flag ; i++ ) {
                flag = rec.s.listeners[i].actionPerformed(rec.result, rec.s, rec.pdu, rec.o) ;
              }
            } // try
            catch (Throwable e1) {
              System.err.println("Exception in NetSNMP.actionPerformed: " + e1);
              e1.printStackTrace(System.err);
              continue ;
            }
       		} // while
       	} // try
       	catch( InterruptedException e ) {
       		System.out.println("processing thread interrupted exiting") ;
       		return ;
       	} 	
      } // run
		}
		
		private void submitPDU(int result, PDU pdu, Object o) {
			processingRec rec = new processingRec() ;
			
			
			rec.result = result ;
			rec.pdu = pdu ;
			rec.s = this ;
			rec.o = o ;
			
			try {
        synchronized( processingQueue ) {
        	if( availableThreads == 0 && processingThreads.size() < maxThreads ) {
        		// launch new thread
        		ProcessingThread thr = new ProcessingThread() ;
        		
        		/*
        		 * The priority of the processing threads is lowered
        		 * so that the main receiving thread will have a slightly
        		 * higher priority to make sure that PDUs are received as 
        		 * soon as possible and that they are not 'dropped' as the result
        		 * of the receiving buffers becoming full.  
        		 */
        		thr.setPriority(Thread.NORM_PRIORITY-1) ;
        		
        		thr.start() ;
        		
        	}
        	while( availableThreads == 0  ) {
						waitingForProcessing += 1 ;
        		processingQueue.wait() ;
						waitingForProcessing -= 1 ;
        	}
        	
        	processingQueue.addFirst(rec) ;
        	
        	
        }
      }
      catch (InterruptedException e) {
        System.err.println("FATAL Error:  interrupted during PDU sumission") ;        
      }
		}
    
		static {
			Warnings.loadNativeLibraryOrProvideWarning("netsnmpj");
			
			/*
			 * Launch our processing threads
			 */
			maxThreads = Integer.parseInt(System.getProperty("org.netsnmp.nProcessingThreads", "4")) ;
			
		} // static
		
		private static LinkedList finalizingQueue = new LinkedList() ;
		
		/**
		 * Called by the native thread to close sessions
		 *
		 */
		private static void finalizeNatives() {
			
			synchronized( finalizingQueue ) {
				while( finalizingQueue.size() > 0 ) {
					nativeFinalize(((Long)finalizingQueue.removeLast()).longValue()) ; 
				}
			}
		}
    
    /**
     * Interupt the reading thread
     * @deprecated
     */
    public static native void interrupt() ;
    
    /**
     * Reads on all pending SNMP operations on all currently open
     * sessions
     * @param timeout time in seconds to wait for an event(micro second)
     * effective granularity
     * @throws an IOException if an error occurs
     *
     * @return whenever a read operation has been completed on at least 1
     * session
     * 
     * @deprecated operation has been moved to an internal thread.   Further
     * use is now an error
     */
    public static void read(double timeout) throws java.io.IOException, readInterrupt {
    	throw new Error("use of NetSNMPSession read has been deprecated beyond version 0.1.0") ;
    }
		
		/**
		 * Set a new default encryptor
		 * @param cryptor new default encyprtor
		 * @return previous encryptor
		 */
		public static serialEncryption setDefaultEncryptor(serialEncryption cryptor) {
			serialEncryption prev = defaultEncryptor ;
			defaultEncryptor = cryptor ;
			return prev ;
			
		}

} // class

/*
 * $Log: NetSNMPSession.java,v $
 * Revision 1.21  2003/06/08 01:15:29  aepage
 * new scheme for finalizing session handles
 *
 * Revision 1.20  2003/06/07 15:59:40  aepage
 * AgentX fixes
 *
 * Revision 1.19  2003/06/01 15:22:01  aepage
 * pre merge checkins
 *
 * Revision 1.18  2003/04/30 14:20:35  aepage
 * doc fixes and member sorting
 *
 * Revision 1.17  2003/04/28 22:43:08  aepage
 * serialize support fixes.
 *
 * Revision 1.16  2003/04/27 11:45:50  aepage
 * added 'isClosing' field
 *
 * Revision 1.15  2003/04/25 23:10:11  aepage
 * added getSessionHandle
 *
 * Revision 1.14  2003/04/23 17:28:39  aepage
 * Fix to 'close' operation that tightens up the locking operations of
 * the nativeThread.  This corrects some 'bad file' errors on select for
 * Solaris that were not occurring under Linux
 *
 * Revision 1.13  2003/04/22 18:00:43  aepage
 * encryption capability for security data
 *
 * Revision 1.12  2003/04/18 01:29:20  aepage
 * serialization support
 *
 * Revision 1.11  2003/04/15 19:18:37  aepage
 * added properties to support SNMPv3 and deprecated the 'interrupt' method
 *
 * Revision 1.10  2003/04/11 19:28:52  aepage
 * property access of peerName, community and version.
 *
 * Revision 1.9  2003/03/31 00:43:08  aepage
 * removal of unuzed variables.
 *
 * Revision 1.8  2003/03/30 22:59:44  aepage
 * removal of unneeded imports
 *
 * Revision 1.7  2003/03/28 23:55:57  aepage
 * the read method is now deprecated.  All reading is now done on a
 * native thread that will make its callbacks into java.
 *
 * Revision 1.6  2003/03/21 21:45:33  aepage
 * readLock object
 *
 * Revision 1.5  2003/03/19 15:49:22  aepage
 * library loading done through Warnings class.  This allows the output a
 * common tutorial message to correct common library issues.
 *
 * Revision 1.4  2003/02/27 17:36:51  aepage
 * Moved loadLibrary call to bottom of class definition.  This corrects
 * several problems related to class intitialization order in relation to
 * when the libraries are loaded.
 *
 * Revision 1.3  2003/02/15 18:02:55  aepage
 * support for use of a typesafe enum to specify the version of snmp to use
 *
 * Revision 1.2  2003/02/09 22:37:18  aepage
 * clarified comments on timeout property
 *
 * Revision 1.1.1.1  2003/02/07 23:56:50  aepage
 * Migration Import
 *
 * Revision 1.4  2003/02/07 22:03:03  aepage
 * java doc comments
 *
 * Revision 1.3  2003/02/07 14:05:53  aepage
 * Refactored NetSNMPPDU to PDU
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/04 23:17:39  aepage
 * Initial Checkins
 *
 */

