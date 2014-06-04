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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.netsnmp.* ;


/**
 * A dialog that presents a mib browser
 * 
 * <pre>
 *   MIBDialog dlg = new MIBDialog(parent, true) ;
 * 	dlg.addDefaultMIBs() ; // System, Interfaces, UCD, Net-SNMP
 * 
 *  dlg.show() ; // when modal this will not return until dialog is dismissed
 * 
 *  if( !dlg.good ) // user pressed cancel
 *     return ;
 *  
 *  System.out.println("got oid = " + dlg.getOID().toText()) ;
 * 
 * </pre>
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class MIBDialog extends JDialog implements SessionProvider {
	
	private OID baseOid ; // from the mib browser
	public boolean good = false ;
	
	private SessionPanel sessionControls ;
	private JPanel sessionPanel = new JPanel() ;
	
	private JPanel auxControls = new JPanel() ; // controls added by the caller
	private String hostName ;
	private String community ;
	
	/**
	 * @return the currently selected OID in the dialog
	 */
	public OID getOID() { 
		int inst ;
		
		MIBJPanel frontPanel = (MIBJPanel)tabbedPane.getSelectedComponent() ;
		baseOid = frontPanel.getSelectedOID() ;
		
		if( baseOid == null )
			return null ;
			
		if( instanceField.getText().length() == 0 ) 
			return baseOid ;
		
		try {
			inst = Integer.parseInt(instanceField.getText()) ;
			
		}
		catch( NumberFormatException e ) {
			JOptionPane.showMessageDialog(this, e) ;
			instanceField.setText("") ;
			return baseOid ;
			
		}
		
		return new InstanceOID(baseOid, inst) ; 
	}
	
	public JPanel getAuxPanel() {
		return auxControls ;
	}
	
	private JLabel oidLabel = new JLabel() ;
	private JTextField instanceField = new JTextField() ;
	private MIBJPanel mibPanel ;
	private JButton okayButton = new JButton("OK"), cancelButton = new JButton("Cancel") ;
	
	private JTabbedPane tabbedPane = new JTabbedPane() ;


	/**
	 * Adds a new MIBJPanel to the tabbed pane
	 * 
	 * @param name name of the new panel
	 * @param oid OID of the panels root
	 */
	public void addMIBPanel(String name, OID oid) {
		
		MIBJPanel panel = new MIBJPanel(oid) ;
		
		panel.addMIBJPanelEvent(mibHandler) ;
		
		tabbedPane.add(name, panel) ;
	}
	
	/**
	 * Convenience method that adds some of the more popular
	 * MIBS to the panel.  
	 */
	public void addDefaultMIBs() {
		try {
			addMIBPanel("mib-2", new DefaultOID("SNMPv2-SMI::mib-2")) ;
			addMIBPanel("System", new DefaultOID("1.3.6.1.2.1.1")) ;
			addMIBPanel("Interfaces", new DefaultOID("IF-MIB::interfaces.ifTable.ifEntry")) ;
			addMIBPanel("UCD", new DefaultOID("UCD-SNMP-MIB::ucdavis")) ;
			addMIBPanel("Net-SNMP", new DefaultOID("NET-SNMP-MIB::netSnmp")) ;
		}
		catch( MIBItemNotFound e ) {
			JOptionPane.showMessageDialog(this, "mibs not installed properly") ;	
			return ;
		}
	}
	
	public MIBDialog(java.awt.Frame parent, boolean modal) {
		this(parent, modal, null) ;
	}
	
	public MIBDialog(java.awt.Frame parent, boolean modal, OID startOID) {
		super(parent, modal) ;
		
		if( startOID == null )
			mibPanel = new MIBJPanel() ;
		else
			mibPanel = new MIBJPanel(startOID) ;
		mibPanel.addMIBJPanelEvent(new objectHandler()) ;
		mibPanel.setPreferredSize(new Dimension(500, 300)) ;
		
		tabbedPane.add("Full", mibPanel) ;
		
		Container contentPane = getContentPane() ;
		GridBagConstraints gbc ;
		JPanel northPanel = new JPanel(), southPanel = new JPanel() ;
		
		sessionControls = new SessionPanel(parent) ;
		
		sessionPanel.setLayout(new BorderLayout()) ;
		
		northPanel.setLayout(new GridBagLayout()) ;
		
		gbc = new GridBagConstraints() ;
		gbc.gridx = 0 ;
		gbc.gridy = 0 ;
		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.gridwidth = 2 ;
		gbc.weightx = 1.0 ;
		northPanel.add(auxControls, gbc) ;
		
		
		gbc = new GridBagConstraints() ;
		gbc.gridx = 0 ;
		gbc.gridy = 1 ;
		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.gridwidth = 2 ;
		gbc.weightx = 1.0 ;
		northPanel.add(sessionPanel, gbc) ;
		
		
		gbc = new GridBagConstraints() ;
		gbc.gridx = 0 ;
		gbc.gridy = 2 ;
		gbc.weightx = 1.0 ;
		gbc.fill = GridBagConstraints.HORIZONTAL ;
		
		northPanel.add(oidLabel, gbc) ;
		
		gbc.gridx = 1 ;
		gbc.weightx = 0.0 ;
		gbc.gridwidth = 1 ;
		instanceField.setPreferredSize(new Dimension(60,40)) ;
		instanceField.setText("0") ;
		instanceField.setColumns(4) ;
		instanceField.setBorder(new TitledBorder("Inst:")) ;
		northPanel.add(instanceField, gbc) ;
		
		contentPane.setLayout(new BorderLayout()) ;
		contentPane.add(northPanel, BorderLayout.NORTH) ;
		
		contentPane.add(tabbedPane, BorderLayout.CENTER) ;
		
		southPanel.add(okayButton) ;
		southPanel.add(cancelButton) ;
		contentPane.add(southPanel, BorderLayout.SOUTH) ;
		
		okayButton.addActionListener(new buttonHandler()) ;
		cancelButton.addActionListener(new buttonHandler()) ;
		
		instanceField.addCaretListener(new CaretListener() {

      public void caretUpdate(CaretEvent e) {
				OID oid ;
				
				oid = getOID() ;
				if( oid == null )
					return ;
				oidLabel.setText("<html><font size=+2>" + oid.toText()) ;
				oidLabel.updateUI() ;     
      }
			
		}) ;

		pack() ;
		setSize(500, 800) ;
	}
	
	
	public void showSessionPanel() {
		sessionPanel.add(sessionControls, BorderLayout.CENTER) ;
	}
	
	public void hideSessionPanel() {
		sessionPanel.remove(sessionControls) ;
	}

	private class objectHandler implements MIBJPanelListener {
		/**
		 * @see org.netsnmp.swingui.MIBJPanelListener#objectSelected(MIBEvent)
		 */
		public void objectSelected(MIBEvent evt) {
			OID oid ;
			
			baseOid = evt.oid ;
			
			oid = getOID() ;
			
			oidLabel.setText("<html><font size=+1>" + oid.toText()) ;
			oidLabel.updateUI() ;
			
		}
	}
	
	private objectHandler mibHandler = new objectHandler() ;

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	
	private class buttonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			good = false ;
			if( e.getSource() == okayButton ) {
				if( baseOid == null || baseOid.length() == 0 )
					good = false ;
				else
					good = true ;
					
				hostName = sessionControls.getPeerName() ;
				community = sessionControls.getCommunity() ;
			}
			setVisible(false) ;
		}
	}

	public static void main(String[] args) throws MIBItemNotFound {
		
		MIBDialog d = new MIBDialog(new JFrame(), true, new DefaultOID()) ;
		
		d.addDefaultMIBs() ;
		
		d.setTitle("Select OID") ;
		
		d.showSessionPanel() ;
		d.setVisible(true) ;
		
		if( d.good )
			System.out.println(d.getOID().toText() + " => " + d.getOID()) ;
		
		
		System.exit(0) ;
	}

	/**
	 * Returns the community.
	 * @return String
	 */
	public String getCommunity() {
		return community;
	}

	/**
	 * Returns the hostName.
	 * @return String
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @return
	 */
	public NetSNMPSession createSession() {
		return sessionControls.createSession();
	}

	/**
	 * @param open
	 * @return
	 */
	public NetSNMPSession createSession(boolean open) {
		return sessionControls.createSession(open);
	}

}

/*
 * $Log$
 */
