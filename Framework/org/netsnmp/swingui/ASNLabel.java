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


package org.netsnmp.swingui;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.netsnmp.DefaultOID;
import org.netsnmp.MIBItemNotFound;
import org.netsnmp.NetSNMPBadValue;
import org.netsnmp.OID;
import org.netsnmp.PDU;
import org.netsnmp.ASN.ASNFormat;
import org.netsnmp.ASN.ASNValue;
import org.netsnmp.framework.GetElement;
import org.netsnmp.framework.SessionManager;


/**
 * A class that allows SNMP values to be displayed through a JLabel swing component.  It is intended
 * to be used for displaying only a small number of elements.
 * 
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

public class ASNLabel extends javax.swing.JLabel  implements GetElement, Serializable {

	private static class defaultASNFormatter implements ASNFormat, Serializable {
			public String format(ASNValue v) {
				return v.toJavaObject().toString() ;	
			}
	}	
	private long updatePeriod = -1 ;
	static defaultASNFormatter defaultFormatter = new defaultASNFormatter() ;
	
	private class entry implements Serializable  {
		OID oid ;
		ASNFormat fmt ;	
		ASNValue v ; // null if update is in progress or if oid is null
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject() ;
		reset() ;
	}
	ArrayList entries = new ArrayList(2) ;
	private boolean entriesChanged = true ;
	OID oids[] ;
	
	private int remainingOIDs ;
	private int nonNullOIDs = 0 ;
	private boolean inProgress = false ;
	
	/**
	 * Adds a new OID to the label
	 */
	synchronized void addOID(OID oid, ASNFormat fmt) {
		
		if( inProgress ) 
			throw new IllegalStateException("attempt to edit oids while query in progress") ;	
			
		entry ent = new entry() ;
		ent.oid = oid ;
		ent.fmt = fmt ;
		entries.add(ent) ;
		
		if( oid != null ) 
			nonNullOIDs += 1 ;
	}
	
	void addOID(OID oid) {
		addOID(oid, defaultFormatter) ;	
	}

	

	/**
	 * @see org.netsnmp.GetElement#oids()
	 */
	public OID [] oids() {
		if( !entriesChanged )
			return oids ;
		oids = new OID[entries.size()] ;
		int i, n ;
		n = entries.size() ;
		
		for( i = 0 ; i < n ; i++ ) {
			oids[i] =( (entry)entries.get(i)).oid ;
		}
		entriesChanged = false ;
		return oids ;
	}

	/**
	 * @see org.netsnmp.GetElement#setPDU(PDU)
	 */
	public void setPDU(PDU values) throws Throwable {
		int i, j ;
		
		for( i = 0 ; i < values.entries.length ; i++ ) {
			OID o = values.entries[i].oid ;
			
			for( j = 0 ; j < entries.size() ; j++ ) {
				entry ent = (entry) entries.get(j) ;
				if( ent.oid.compareTo(o) != 0 )
					continue ;
				ent.v = values.entries[i].value ;
				
				remainingOIDs -= 1 ;
			} // for j	
		} // for i 
		if( remainingOIDs == 0 )
			setLabel() ;
	}
	
	/**
	 *  called when all of the values have been returned
	 */
	private void setLabel() {
		entry ent ;
		int i ;
		
		StringBuffer sb = new StringBuffer() ;
		for( i = 0 ; i < entries.size() ; i++ ) {
			ent = (entry)entries.get(i) ;
			sb.append(ent.fmt.format(ent.v)) ;
		} // for i 
		
		final String s = sb.toString() ;
		Runnable r = new Runnable() {
			public void run() {
				setText(s) ;
				updateUI() ;
			}
		} ;
		SwingUtilities.invokeLater(r) ;
	}

	/**
	 * @see org.netsnmp.GetElement#start()
	 */
	public synchronized  void start() {
		inProgress = true ;
		remainingOIDs = nonNullOIDs ;
	}
	
	/**
	 * @see org.netsnmp.framework.GetElement#repeat()
	 */
	public long repeat() {
		return updatePeriod ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#handleError()
	 */
	public void handleError(PDU pdu) {
	}

	/**
	 * @see org.netsnmp.framework.GetElement#hasChanged()
	 */
	public boolean hasChanged() {
		return false;
	}
	
	private static class timeTicksFormat implements ASNFormat {

		/** (non-Javadoc)
		 * @see org.netsnmp.ASN.ASNFormat#format(org.netsnmp.ASN.ASNValue)
		 */
		public String format(ASNValue v) {
			long ticks ;
			
			try {
				 ticks = v.toInt64() ;
			} 
			catch (NetSNMPBadValue e) {
				return "cannot format " + v.getClass().toString() ;
			}
			StringBuffer sb = new StringBuffer() ;
			
			long days  = ticks/(360000 * 24) ;
			ticks -= days * (360000 * 24) ;
			
			long hours = ticks/360000 ;
			ticks -= hours * 360000 ;
			
			long minutes = ticks / 6000 ;
			ticks -= minutes * 6000 ;
			
			long seconds = ticks / 100 ;
			ticks -= seconds * 100 ;
			
			if( days > 0 ) {
				sb.append(days) ;
				sb.append(" days ") ;
			}
			
			if( days > 0 || hours > 0 ) {
				sb.append(hours) ;
				sb.append(" hrs ") ;
			}
			
			if( days > 0 || hours > 0 || minutes > 0 ) {
				sb.append(minutes) ;
				sb.append(" mins ") ;
			}
			
			sb.append(seconds) ;
			sb.append(".") ;
			sb.append(ticks) ;
			sb.append(" secs ") ;
			
			return sb.toString() ;
		}
		
		
	}

	public static void main(String args[]) throws MIBItemNotFound {
		JFrame frm = new JFrame() ;
		ASNLabel label, tickLabel ;
		SessionManager mgr = new SessionManager("localhost", "abecrombe") ;

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		
		label = new ASNLabel() ;
		
		tickLabel = new ASNLabel() ;
		tickLabel.addOID(new DefaultOID("SNMPv2-MIB::sysUpTime.0"), new timeTicksFormat()) ;
		
		if( args.length > 0 ) {
			label.addOID(new DefaultOID(args[0])) ;
		}
		else {
			label.addOID(new DefaultOID("SNMPv2-MIB::sysDescr.0")) ;
		}
		
		tickLabel.setUpdatePeriod(5000) ;
		tickLabel.setHorizontalAlignment(JLabel.CENTER) ;
		
		
		//frm.setSize(450, 50) ;
		frm.getContentPane().setLayout(new java.awt.GridLayout(0, 1, 0, 25)) ;
		frm.getContentPane().add(label) ;
		frm.getContentPane().add(tickLabel) ;
		
		mgr.submit(label, 2000) ;
		mgr.submit(tickLabel) ;
		
		frm.pack() ;
		frm.show() ;
	}

	/**
	 * not necessary for this object
	 * @see org.netsnmp.framework.GetElement#reset()
	 */
	public void reset() {
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
	}

	/**
	 * @see org.netsnmp.framework.GetElement#timeout()
	 */
	public void timeout() {
		
		Runnable r = new Runnable() {
			public void run() {
				setText("timedout") ;
			}
		};

		SwingUtilities.invokeLater(r);
	}

	/**
	 * Get the time in milliseconds between updates of this element
	 * @return
	 */
	public long getUpdatePeriod() {
		return updatePeriod;
	}

	/**
	 * @param millisecs new time in milliseconds between updates of this element
	 */
	public void setUpdatePeriod(long millisecs) {
		this.updatePeriod = millisecs ;
	}

}
