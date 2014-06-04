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

package org.netsnmp.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.* ;

import org.netsnmp.* ;

/**
 * Class to manage submission of objects for query on a repeating basis
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class SessionManager implements Serializable {

	/**
	 *  private inner class, implemented to hide details of the SessionManager.
	 */
	private class actionHandler implements NetSNMPAction, Serializable {
		/**
		 * @see org.netsnmp.NetSNMPAction#actionPerformed(int, NetSNMPSession, PDU, Object)
		 */
		public boolean actionPerformed(
			final int result,
			NetSNMPSession s,
			final PDU rpdu,
			Object o)
			throws Throwable {
				long updatePeriod ;
				final sessionEntry ent = (sessionEntry)o ;
				
				// object for running a successful query
				class qRunner extends qelement {
					public void run() {
							try {
								// System.out.println("setting result = " + result);
								ent.elem.setPDU(rpdu) ;
							} // try 
							catch (Throwable e) {
								synchronized( System.err ) {
									System.err.println("error occurred sending a setPduEvent") ;
									e.printStackTrace() ;
								} // sync
							} // catch( Throwable )
					}
				} ;
				
				// object for running a timeout event
				class errorRunner extends qelement {
					public void run() {
							try {
								ent.elem.handleError(rpdu) ;
							} // try
							catch (Throwable e) {
								synchronized( System.err ) {
									System.err.println("error occurred sending a timeout event") ;
									e.printStackTrace(System.err) ;
								} // sync
							} // catch( Throwable )
					}
				} ;
				
				/*
				 *  call of interface specified code
				 */
				if( result == NetSNMP.STAT_TIMEOUT ) {
					
					executeQelem(new timeoutRunner(ent.elem)) ;
					}
				else {
					if( rpdu.errStatus != 0 )
						executeQelem(new errorRunner()) ;
					else
						executeQelem(new qRunner()) ;
				}
			
			return true ; // continue calling other handlers
		}
		
	} // class actionHandler
 	
 	private static class eventRunner extends Thread {
 		boolean exitFlag = false ;
 		private Runnable r ;
 		boolean ready = false ;
 		public void run() {
 			
 				
			while( !exitFlag ) {
				synchronized( this ) {						
				ready = true ;
					notify() ; // notify anyone waiting for this object
					try {
						wait() ;   // wait until someone dispatches us
					} 
					catch (InterruptedException e) {
						if( exitFlag ) {
							nExecutors -= 1 ;
							continue ; // someone signalling us to quit
						}
						throw new IllegalStateException("FATAL error:  interrupted during submit") ;
					}
					if( r == null ) // probably shouldn't have to worry about this but
						throw new IllegalStateException("null runnable object") ;
					ready = false ;
					r.run() ;	
					r = null ;
				}
				synchronized( executors ) {
					executors.addFirst(this) ;
				} // synchronized	
			} // while
				
 		} // run()
 		
 		public synchronized void submit(Runnable r) {
 			while( !ready ) {
 				try {
					wait() ;
				} 
				catch (InterruptedException e) {
					if( !exitFlag )
						throw new IllegalStateException("FATAL error:  interrupted during submit") ;
				}
 			}
 			this.r = r ;
 			notify() ; // signal the thread that there is an object to be run
 		}
 	} // class eventRunner

	/**
	 *  internal thread for processing events
	 */
	private class managerThread implements Runnable {
		public void run() {
	 		long remaining = 0, t0 ;
	 		qelement qelem = null ;
	 		while( true ) {
	 			synchronized( eventQueue ) {
		 			
		 				while( eventQueue.size() == 0 ) {
		 					try {
		 						waitingAt = System.currentTimeMillis() ;	 						
		 						eventQueue.wait() ;
		 						
							} catch (InterruptedException e) {
								if( exitFlag )
									return ;
							}
		 					continue ;
		 				}
		 				
		 				qelem = (qelement)eventQueue.removeFirst() ;
		 				if( qelem.at <= System.currentTimeMillis() ) {
							executeQelem(qelem) ;
							continue ;
		 				}
							
		 				remaining = qelem.at  - System.currentTimeMillis() ;
		 				
						try {
							waitingAt = System.currentTimeMillis() ;
							if( remaining > 0 )
								eventQueue.wait(remaining) ;
						} 
						catch (InterruptedException e) {
							if( exitFlag )
								return ;
						}
						submitQelement(qelem) ;
	 			} // synchronized
	 		}
	 	} // run
	}
 	
 	private static abstract class qelement implements Runnable {
 		/**
 		 * System time to for firing the event
 		 * 
 		 */
 		long at ;
 		/**
 		 * action to perform
 		 */
 	}
 	
 	private class sessionEntry extends qelement {
 		GetElement elem ;
 		OID oids[] ;
 		PDU pdu ;	
 		
 		sessionEntry(GetElement e) {
 			this.elem = e ;	
 		}
 		
 		public void run() {
 			if( oids == null || elem.hasChanged() ) {
 				oids = elem.oids() ;
 				pdu = new PDU(NetSNMP.MSG_GET) ;
 				pdu.addNullEntries(oids) ;
 			}
 			elem.start() ; // signal that the query is starting
 			try {
				session.send(pdu, this) ;
			} 
			catch( NetSNMPErrTooBig e ) {
				// TBD add functionality to send more than one pdu	
			}
			catch (NetSNMPSendError e) {
				
			}
			finally {
				long updatePeriod = elem.repeat() ;
				if( updatePeriod <= 0 )
					return ;
				// resubmit
				at = System.currentTimeMillis() + updatePeriod ;
				submitQelement(this) ;
			}
 		} // run
 		
 	} // session Entry
	
//	object for running a timeout event
	 private class timeoutRunner extends qelement {
		 GetElement elem ;
		 public timeoutRunner(GetElement e) { 
			 elem = e ;
		 }
		 public void run() {
				 try {
					 elem.timeout() ;
				 } // try
				 catch (Throwable e) {
					 synchronized( System.err ) {
						 System.err.println("error occurred sending a timeout event") ;
						 e.printStackTrace(System.err) ;
					 } // sync
				 } // catch( Throwable )
		 }
		 public String toString() {  return "timeout on " + elem.toString() ; }
	 } 	private transient LinkedList eventQueue ;
	private boolean exitFlag = false ;
	private NetSNMPSession session ;
	
	/**
	 * Internal thread to manage submissions
	 */
	private transient Thread thr ;
 	
 	private long waitingAt ;
 	private static LinkedList executors = new LinkedList() ;

 	private static final int maxExecutors = 5 ;
 	private static int nExecutors = 0 ;
 	
 	public SessionManager(NetSNMPSession s) {
 		
		waitingAt = System.currentTimeMillis() ;
		eventQueue = new LinkedList() ;
		thr = new Thread(new managerThread(), "SessionMgr for " + s.getPeerName()) ;
		thr.start() ;	

		session = s ;
		session.addListener(new actionHandler()) ;
		session.open() ;
 	}
 
 	
 	public SessionManager(String host, String community) {
 		this(new NetSNMPSession(host, community)) ;
 	}
 	
 	public SessionManager(String host, String community, SNMPVersion v) {
		this(new NetSNMPSession(host, community)) ;
		session.setSNMPVersion(v) ;
		session.open() ; // reopen the session
 	}
 	
 	private void executeQelem(qelement qelem) {
 		eventRunner runner ;
 		
 		synchronized( executors ) {
			if( executors.size() == 0 ) {
		 		runner = new eventRunner() ;
		 		runner.setName("eventRunner" + executors.size()) ;
		 		runner.start() ;
		 		//executors.addFirst(runner) ;
	 		}
	 		else {
		 		runner = (eventRunner)executors.removeFirst() ;
	 		}
	 		
	 		
		} // synchronized
		runner.submit(qelem) ;
 	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return session.hashCode();
	}
 	
 	
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		
 		in.defaultReadObject() ;
 		thr = new Thread(new managerThread(), "SessionMgr for " + session.getPeerName()) ;
 		eventQueue = new LinkedList() ;
 		thr.start() ;
 			
 	}
 
 	
 	
 	public void submit(GetElement e) {
 		submit(e, 0) ;	
 	}
 	
 	/**
 	 * @param e element to submit
 	 * @param inMilliseconds time to wait before submitting
 	 */
 	public void submit(GetElement e, long inMilliseconds) {
 		sessionEntry entry = new sessionEntry(e) ;
 		if( inMilliseconds <= 0 ) {
 				entry.run() ;
 				return ;
 		}
 		entry.at = System.currentTimeMillis() + inMilliseconds ;
 		submitQelement(entry) ;
 	}
 	
 	private void submitQelement(qelement sElem) {	
 		synchronized( eventQueue ) {
 			qelement e  ;
 			ListIterator it = eventQueue.listIterator() ;
 			
 			while( it.hasNext() ) {
				e = (qelement)it.next() ;
				if( e.at < sElem.at )
					continue ;
				eventQueue.add(it.previousIndex(), sElem) ;
				eventQueue.notify() ;
				return ;
			} // while 
			// append event
			
			eventQueue.add(sElem) ;
			eventQueue.notify() ;
		} // synchronized
 		
 	}
 	
 	public static void main(String [] args) {
 		SessionManager mgr = new SessionManager(new NetSNMPSession("localhost", "public")) ;
 		final long timeStart = System.currentTimeMillis() ;
 		class X extends qelement {
 			public X(long t) { at = System.currentTimeMillis() + t ; }
 			public void run() {
 				System.out.println("t = " + (System.currentTimeMillis() - timeStart));
				
 			}
 		} ;
 		
 		
 		mgr.submitQelement(new X(1000)) ;
 		mgr.submitQelement(new X(5000)) ;
 		mgr.submitQelement(new X(9000)) ;
 		mgr.submitQelement(new X(1500)) ;
 		mgr.submitQelement(new X(1750)) ;
 		mgr.submitQelement(new X(8000)) ;

 		try {
			Thread.sleep(2000) ;
		} 
		catch (InterruptedException e) {
		}
		System.out.println("t = " + (System.currentTimeMillis() - timeStart) + " submitting 3k entry");
 		mgr.submitQelement(new X(1000)) ; // 3000
 		System.out.println("all submitted");
 	} // main

}
