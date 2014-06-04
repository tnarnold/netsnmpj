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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.netsnmp.NetSNMP;
import org.netsnmp.PDU;
import org.netsnmp.framework.GetElement;
import org.netsnmp.framework.SessionManager;
import org.netsnmp.framework.util.ifBandwidthElement;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ifBandwidthMeter extends JPanel {
	
	private JProgressBar inBar = new JProgressBar() ;
	private JProgressBar outBar = new JProgressBar() ;
	
	private TitledBorder inBorder = new TitledBorder("") ;
	private TitledBorder outBorder = new TitledBorder("") ;
	private JLabel errorLabel ;
	private boolean errLabelSet = false ;
	
	private myBandwidthElem bandwidthElem ;

	private GridLayout barLayout;
	private BorderLayout errLayout ;
	
	private DateFormat updateTimeFmt = new SimpleDateFormat("HH:mm:ss") ;
	
	private class myBandwidthElem extends ifBandwidthElement {
	
		myBandwidthElem(int instance, long period) {
			super(instance, period) ;
			
		}
		/**
		 * @see org.netsnmp.framework.util.ifBandwidthElement#setBandwidth(int, long, int, long)
		 */
		protected void setBandwidth(
			final int inPercent,
			final long inBps,
			final int outPercent,
			final long outBps, final int dt) {
				
				Runnable r = new Runnable() {
					public void run() {
						
						clearErrorLabel() ;
						
						inBar.setValue(inPercent)	;
						outBar.setValue(outPercent) ;
						inBorder.setTitle(getName() + " In " + inBps + " bits/sec") ;
						outBorder.setTitle("Out " + outBps + " bits/sec") ;
						
						inBar.updateUI() ;
						outBar.updateUI() ;
						
						ifBandwidthMeter.this.updateUI() ;
						setLastUpdated("( dt =" + dt/1000 + " secs)") ;
					}
				};

				SwingUtilities.invokeLater(r);
				
		} // setBandwidth
		

		/**
		 * @see org.netsnmp.framework.GetElement#timeout()
		 */
		public void timeout() {
			super.timeout() ;
			
			Runnable r = new Runnable() {
				public void run() {
					setLastUpdated("(timed out)") ;
					setErrorLabel("time out") ;
				}
			};

			SwingUtilities.invokeLater(r);
		}
		
		public void setLastUpdated(String suffix) {
		
			setToolTipText("Last Updated: " + updateTimeFmt.format(new Date()) +  " " + suffix) ;	
			
		}
		
		public void setLastUpdated() {
			setLastUpdated("") ;	
		}
		
		public void clearErrorLabel() {
				if( !errLabelSet )
					return ;
					
				remove(errorLabel) ;
				setLayout(barLayout) ;
				
				add(inBar) ;
				add(outBar) ;
					
				errLabelSet = false ;
		}
		
		public void setErrorLabel(String errStr) {
			
			if( errorLabel == null ) {
				errorLabel = new JLabel() ;
				errLayout = new BorderLayout() ;	
				errorLabel.setHorizontalAlignment(JLabel.CENTER) ;
			}
			
			if( !errLabelSet ) {
				remove(inBar) ;
				remove(outBar) ;
				setLayout(errLayout) ;
				add(errorLabel, BorderLayout.CENTER) ;
				errLabelSet = true ;
			}
			
			errorLabel.setText(errStr) ;
			
			
		}

		/**
		 * @see org.netsnmp.framework.GetElement#handleError(PDU)
		 */
		public synchronized void handleError(PDU pdu) {
			setErrorLabel("Err in PDU " + pdu.errStatus + "/" + pdu.errIndex) ;
		}

	} // myBandwidthElem
	
	public GetElement getElement() {
		return bandwidthElem ;	
	}
	
	public ifBandwidthMeter(int instance, long period) {
		
		inBar.setStringPainted(true) ;
		outBar.setStringPainted(true) ;
		
		outBar.setBorder(outBorder) ;
		inBar.setBorder(inBorder) ;
		
		barLayout = new GridLayout(2,1) ;
		
		setLayout(barLayout) ;
		add(inBar) ;
		add(outBar) ;
		
		bandwidthElem = new myBandwidthElem(instance, period) ;
	}
	
	
	public static void main(String args[]) {
		int i ;
		TitledBorder b ;
		SessionManager mgr = null ;
		ifBandwidthMeter mtr = null ;
		
		if( args.length < 3 || (args.length % 3) != 0 ) {
			System.err.println("usage:  ifBandwidthMeter (<host> <community> <ifIndex>) ... ");	
			System.exit(2) ;
		}
		
		JFrame frm = new JFrame() ;
		JScrollPane scrollPanel = new JScrollPane() ;
		JPanel viewPort = new JPanel() ;
		
		frm.setTitle("ifBandwidth Meter") ;
		
		Container contentPane = frm.getContentPane() ;
	  

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		
		contentPane.setLayout(new BorderLayout()) ;
		contentPane.add(scrollPanel, BorderLayout.CENTER) ;
		
		frm.setSize(400, 400) ;
		viewPort.setLayout(new GridLayout(args.length/3, 1, 0, 30)) ;
		for( i = 0 ; i < args.length ; i += 3 ) {
			mgr = new SessionManager(args[i], args[i+1], NetSNMP.SNMPv1) ;
			
			mtr = new ifBandwidthMeter(Integer.parseInt(args[i+2]), 5000) ;
			
			mtr.setMinimumSize(new Dimension(0, 20)) ;
			
			b = new TitledBorder(args[i]) ;
			
			b.setBorder(new LineBorder(Color.BLACK, 3)) ;
			
			mtr.setBorder(b) ;
			
			mgr.submit(mtr.getElement()) ;
			
			viewPort.add(mtr) ;
		}
		scrollPanel.setViewportView(viewPort) ;
		scrollPanel.updateUI() ;
		
		
		frm.setVisible(true) ;
	}

}
