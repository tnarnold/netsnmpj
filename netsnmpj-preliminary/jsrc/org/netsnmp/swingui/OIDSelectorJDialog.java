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

import javax.swing.* ;
import java.awt.event.* ;
import java.awt.* ;
import org.netsnmp.* ;

/**
 * A basic swing dialog for selecting OIDS from the current mibs
 *
 *   <pre>
 *      OIDSelectorJDialog dlg = new OIDSelectorJDialog() ;
 *
 *       dlg.setModal(true) ;
 *       dlg.show() ;
 *
 *       if( dlg.good() ) { // user pressed Ok
 *             System.out.println("User Selected: "  + dlg.selectedOID);
 *        }
 *   <pre>
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class OIDSelectorJDialog extends JDialog implements MIBJPanelListener {
	
	/**
	 * Set to the oid that has been selected
	 */
	public OID selectedOID = null ;
	public boolean good = false ;
	
	private MIBJPanel mibPanel ;
	private JTextField instanceField ;
	private JDialog dlg ;
	
	private ActionListener okHandler = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			int value ;
			good = true ;
			String instStr = instanceField.getText() ;
			
			try {
				if( instStr.length() != 0 ) {
					value = Integer.parseInt(instStr) ;
					if( selectedOID != null )
						selectedOID = new InstanceOID(selectedOID, value) ;
				}
				
				dispose() ;
			}
			catch ( Exception e ) {
				JOptionPane.showMessageDialog(dlg, "Error:  " + e) ;
				return ;
			}
		}
	} ;
	
	private ActionListener cancelHandler = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			good = false ;
			dispose() ;
		}
	} ;
	
	
	public void objectSelected(MIBEvent evt) {
		selectedOID = evt.oid ;
	}
	
	
	OIDSelectorJDialog() {
		dlg = this ;
		GridBagConstraints c = new GridBagConstraints() ;
		JPanel instancePanel = new JPanel(), controlPanel = new JPanel() ;
		
		instanceField = new JTextField("0") ;
		
		setSize(300,400) ;
		
		getContentPane().setLayout(new BorderLayout()) ;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE) ;
		
		try {
			mibPanel = new MIBJPanel() ;
			
			mibPanel.addMIBJPanelEvent(this) ;
		}
		catch (Exception e) {
			System.out.println("FATAL Error:  MIBJPanel Failed " + e);
		}
		
		instancePanel.setLayout(new GridBagLayout()) ;
		controlPanel.setLayout(new FlowLayout()) ;
		
		c.gridx = 0 ;
		c.gridy = 0 ;
		c.weightx = 0.0 ;
		c.weighty = 0.0 ;
		
		instancePanel.add(new JLabel("inst:"), c) ;
		
		c.gridx = 1 ;
		c.weightx = 1.0 ;
		c.fill = GridBagConstraints.HORIZONTAL ;
		
		instancePanel.add(instanceField, c) ;
		
		JButton okButton = new JButton("Ok"), cancelButton = new JButton("Cancel") ;
		
		okButton.addActionListener(okHandler) ;
		cancelButton.addActionListener(cancelHandler) ;
		
		controlPanel.add(okButton) ;
		controlPanel.add(cancelButton) ;
		
		getContentPane().add(mibPanel, BorderLayout.CENTER) ;
		getContentPane().add(instancePanel, BorderLayout.NORTH) ;
		getContentPane().add(controlPanel, BorderLayout.SOUTH) ;
	}
	
	public static void main(String args[]) {
		OIDSelectorJDialog d = new OIDSelectorJDialog() ;
		
		d.setModal(true) ;
		
		d.show() ;
		
		System.err.println("done " + d.selectedOID.toText());
		System.exit(0) ;
	}
	
}

