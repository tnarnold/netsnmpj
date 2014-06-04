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

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.* ;
import javax.swing.border.TitledBorder;

import org.netsnmp.* ;
import org.netsnmp.ASN.* ;

import org.netsnmp.framework.* ;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 */
public class ValueMeter extends JProgressBar implements GetElement, UpdatePeriodAccessor {

	private long updatePeriod ;
	private String label ;
	private OID oids[] = new OID[1] ;
	protected boolean timedoutFlag = false ;
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject() ;
			reset() ;
		}
	
	public void setOID(OID o) {
		oids[0] = o ;	
	}
	
	public void setLabel(String l) {
		
		setBorder(new TitledBorder(l)) ;
	}
	
	public ValueMeter(OID oid, int min, int max) {
		this.oids[0] = oid ;
		
		setMinimum(min) ;
		setMaximum(max) ;
	}
	
	/**
	 * Constructs a Value meter to monitor an OID
	 * that monitors a percentage value.  For example
	 * UCD-SNMP-MIB::dskPercent.1 which would get the
	 * percentage of disk space on a disk monitored by
	 * snmpd.  
	 */
	public ValueMeter(OID oid) {
		this(oid, 0, 100) ;	
	}

	/**
	 * @see org.netsnmp.framework.GetElement#handleError(Object)
	 */
	public void handleError(PDU pdu) {
	}

	/**
	 * @see org.netsnmp.framework.GetElement#hasChanged()
	 */
	public boolean hasChanged() {
		return false;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#oids()
	 */
	public OID[] oids() {
		
		return oids ;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#repeat()
	 */
	public long repeat() {
		return updatePeriod;
	}

	/**
	 * @see org.netsnmp.framework.GetElement#setPDU(PDU)
	 */
	public void setPDU(PDU values) throws Throwable {
		final int value ;
		ASNValue v = values.findValue(oids[0]) ;
		
		// System.out.println("setting => " + values);
		
		Object o = v.toJavaObject() ;
		
		if( Number.class.isInstance(o) ) {
				value = ((Number)o).intValue() ;
		} 
		else {
			return ;	
		}
		
		Runnable r = new Runnable() {
			public void run() {
				if( timedoutFlag ) {
					setString(null) ; // set it back to the default
					timedoutFlag = false ;
				}
					
				setValue(value) ;
				updateUI() ;
			}
		} ;
		
		SwingUtilities.invokeLater(r) ;
		
	}

	/**
	 * @see org.netsnmp.framework.GetElement#start()
	 */
	public void start() {
	}

	/**
	 * Returns the updatePeriod.
	 * @return long
	 */
	public long getUpdatePeriod() {
		return updatePeriod;
	}

	/**
	 * Sets the updatePeriod.
	 * @param updatePeriod The updatePeriod to set
	 */
	public void setUpdatePeriod(long updatePeriod) {
		this.updatePeriod = updatePeriod;
	}



	public static void main(String[] args) throws MIBItemNotFound {
		JFrame frm = new JFrame() ;
		Container contentPane = frm.getContentPane() ;
	  SessionManager mgr = new SessionManager("vivacia", "abecrombe") ;

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		
		contentPane.setLayout(new GridLayout(0, 1)) ;
		
		frm.setSize(400, 200) ;
		
		
		ValueMeter mtr = new ValueMeter(new DefaultOID("UCD-SNMP-MIB::dskPercent.1")) ;
		ValueMeter mtr2 = new ValueMeter(new DefaultOID("UCD-SNMP-MIB::dskPercent.2")) ;
		
		mtr.setUpdatePeriod(5000) ;
		mtr.setStringPainted(true) ;
		mtr.setBorderPainted(true) ;
		
		mtr2.setUpdatePeriod(5000) ;
		mtr2.setStringPainted(true) ;
		mtr2.setBorderPainted(true) ;
		
		contentPane.add(mtr) ;
		contentPane.add(mtr2) ;
		
		mgr.submit(mtr, 2000) ;
		mgr.submit(mtr2, 3000) ;
		
		frm.show() ;
		
	}

	public String toString() {
		return "VMtr for " + oids[0].toText() ;
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
	 * @see org.netsnmp.framework.GetElement#showdown()
	 */
	public void shutdown() {
	}

	/**
	 * @see org.netsnmp.framework.GetElement#timeout()
	 */
	public void timeout() {
		timedoutFlag = true ;
		
		Runnable r = new Runnable() {
			public void run() {
				setValue(0) ;
				setString("timed out") ;
			}
		};

		SwingUtilities.invokeLater(r);
	}

}
