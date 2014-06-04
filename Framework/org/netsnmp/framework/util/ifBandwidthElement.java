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

package org.netsnmp.framework.util;

import java.io.ObjectInputStream;
import java.io.Serializable;

import org.netsnmp.InstanceOID;
import org.netsnmp.NetSNMP;
import org.netsnmp.OID;
import org.netsnmp.PDU;
import org.netsnmp.framework.GetElement;
import org.netsnmp.framework.SessionManager;

/**
 * @author aepage
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class ifBandwidthElement implements GetElement, Serializable {
	
	public static final OID ifSpeed = org.netsnmp.MIB.readObjIDOrAbort("IF-MIB::ifSpeed") ;
	public static final OID ifInOctets = org.netsnmp.MIB.readObjIDOrAbort("IF-MIB::ifInOctets") ;
	public static final OID ifOutOctets = org.netsnmp.MIB.readObjIDOrAbort("IF-MIB::ifOutOctets") ;
	public static final OID ifDescr = org.netsnmp.MIB.readObjIDOrAbort("IF-MIB::ifDescr") ;

	private static final int IF_SPEED = 0 ;
	private static final int IF_DESCR = 1 ;
	
	private static final int IF_IN = 0 ;
	private static final int IF_OUT = 1 ;

	private boolean initialized ;
	private long updatePeriod ;
	
	protected int speed ;
	private long ins, outs ;
	private long lastUpdated ;
	
	private Long insLong, outsLong ;
	private Integer speedInt ;
	
	private String name ;

	private OID initOids[] = new OID[2] ;
	private OID runningOids[] = new OID[2];

	protected ifBandwidthElement(int instance, long period) {
		
		initOids[IF_SPEED] = new InstanceOID(ifSpeed, instance) ;
		initOids[IF_DESCR] = new InstanceOID(ifDescr, instance) ;
		
		runningOids[IF_IN] = new InstanceOID(ifInOctets, instance) ;
		runningOids[IF_OUT] = new InstanceOID(ifOutOctets, instance) ;
		
		updatePeriod = period ;
	}
	
	/**
	 * retrieves the name of the interface
	 * 
	 * @return name of the interface or null if it has not yet been acquired
	 */
	public String getName() {
		return name ;
	}
	
	private synchronized void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject() ;
		reset() ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#reset()
	 */
	public void reset() {
		initialized = false ;
		insLong = null ;
		outsLong = null ;
		speedInt = null ;
		name = null ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#configure()
	 */
	public void configure() {
	}

	/**
	 * @see org.netsnmp.framework.GetElement#shutdown()
	 */
	public void shutdown() {
		updatePeriod = -1 ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#start()
	 */
	public void start() {
		insLong = null ;
		outsLong = null ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#handleError(Object)
	 */
	public void handleError(PDU pdu) {
	}

	/**
	 * @see org.netsnmp.framework.GetElement#oids()
	 */
	public synchronized OID[] oids() {
		if( !initialized ) {
			OID fullOIDs[] = new OID[initOids.length + runningOids.length] ;
			System.arraycopy(initOids, 0, fullOIDs, 0, initOids.length) ;
			System.arraycopy(runningOids, 0, fullOIDs, initOids.length, runningOids.length) ;
			return fullOIDs ;				
		}
		
		return runningOids ;
	}
	
	public synchronized void timeout() {
		initialized = false ;
		name = null ;
		speedInt = null ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#hasChanged()
	 */
	public boolean hasChanged() {
		return true ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#setPDU(PDU)
	 */
	public synchronized void setPDU(PDU values) throws Throwable {
		long oldIns = ins, oldOuts = outs, dt ;
		long inBps, outBps ; // bits per second
		long updateTime ;
		int  inPercent, outPercent ;
		Integer I ;
		Long L ;
		
		/*
		 * Extract each value, taking care that they may be in another forthcoming
		 * pdu
		 */
		L = (Long)values.findValue(runningOids[IF_IN]).toJavaObject() ;
		if( L != null )
			insLong = L ;
			
		L = (Long)values.findValue(runningOids[IF_OUT]).toJavaObject() ;
		if( L != null )
			outsLong = L ;
			
		if( speedInt == null ) {
			I = (Integer)values.findValue(initOids[IF_SPEED]).toJavaObject() ;
			if( I != null )
				speedInt = I ;
		}
		
		if( name == null )
			name = (String)values.findValue(initOids[IF_DESCR]).toJavaObject() ;
		
		if( insLong == null || outsLong == null || speedInt == null || name == null )
			return ;
			
		updateTime = System.currentTimeMillis() ;
		dt = updateTime - lastUpdated ;
		lastUpdated = updateTime ;
		ins = insLong.longValue() ;
		outs = outsLong.longValue() ;
		speed = speedInt.intValue() ;
		
		if( !initialized ) {
			initialized = true ;
			return ;
		}
			
		inBps = ((ins - oldIns)*8000)/dt ;
		outBps = ((outs - oldOuts)*8000)/dt ;
		
		inPercent = (int) ((inBps * 100)/(long)speed) ;
		outPercent = (int) ((outBps * 100)/(long)speed) ;
		
		setBandwidth(inPercent, inBps, outPercent, outBps, (int)dt) ;
		
	}

	/**
	 * @see org.netsnmp.framework.GetElement#repeat()
	 */
	public long repeat() {
		return updatePeriod ;
	}
	

	/**
	 * called when object can update the calculation of bandwidth
	 * 
	 * @param inPercent  Percentage of intput bandwidth
	 * @param outPercent Percentout of output bandwidth
	 * @param intBps   input bits per second
	 * @param outBps   ouput bits per second
	 * @param dt       time in milliseconds since last update
	 */
	protected abstract void setBandwidth(int inPercent, long inBps, int outPercent, long outBps, int dt) ;
	
	public static void main(String args[]) {
		SessionManager s = new SessionManager("dslmodem", "public", NetSNMP.SNMPv1) ;	
		
		ifBandwidthElement bnd = new ifBandwidthElement(2, 5000) {
			protected void setBandwidth(int inPercent, long inBps, int outPercent, long outBps, int dt) {
				System.out.println(getName() + " in => " + inBps + "(" + inPercent + "%)  out => " 
										 + outBps + "(" + outPercent + "%)  speed = " + speed);
			}
			
			public void timeout() {
				System.out.println("timeout") ;	
			}
		} ;
		
		s.submit(bnd, 0) ;
	}
}
