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

import java.awt.GridLayout;
import javax.swing.JOptionPane;

import org.netsnmp.* ;
import org.netsnmp.util.HostLister;
/**
 *
 * @author  aepage
 */
public class ValueMeterConfigDialog extends javax.swing.JDialog {
    
    private ValueMeter theMeter ;
    public String hostName, community ;
    
    public ValueMeterConfigDialog(java.awt.Frame parent, boolean modal, ValueMeter mtr) {
    	this(parent, modal, mtr, NetSNMP.defaultHostList) ;
    }
    
    /** Creates new form ValueMeterConfigDialog */
    public ValueMeterConfigDialog(java.awt.Frame parent, boolean modal, ValueMeter mtr, HostLister hostLister) {
        super(parent, modal);
        initComponents();
     
     
     theMeter = mtr ;   
		/*
	   * Add the list of hosts
	   */
	   

			String[] hosts = hostLister.hosts();
			int i;
			for (i = 0; i < hosts.length; i++) {
				hostComboBox.addItem(hosts[i]) ;
			}
         
      
         
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        hostComboBox = new javax.swing.JComboBox();
        communityField = new javax.swing.JPasswordField();
        jPanel1 = new javax.swing.JPanel();
        oidField = new javax.swing.JTextField();
        mibBtn = new javax.swing.JButton();
        minField = new javax.swing.JTextField();
        maxField = new javax.swing.JTextField();
        labelField = new javax.swing.JTextField();
        okayBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        updateField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Configure Meter");
        setBackground(java.awt.Color.white);
        setForeground(java.awt.Color.white);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        hostComboBox.setEditable(true);
        hostComboBox.setBorder(new javax.swing.border.TitledBorder("Host"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(hostComboBox, gridBagConstraints);

        communityField.setBorder(new javax.swing.border.TitledBorder("Community"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(communityField, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        oidField.setText("UCD-SNMP-MIB::dskPercent.1");
        oidField.setBorder(new javax.swing.border.TitledBorder("OID"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(oidField, gridBagConstraints);

        mibBtn.setText("MIB...");
        mibBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mibBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(mibBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        minField.setText("0");
        minField.setBorder(new javax.swing.border.TitledBorder("min"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(minField, gridBagConstraints);

        maxField.setText("100");
        maxField.setBorder(new javax.swing.border.TitledBorder("max"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(maxField, gridBagConstraints);

        labelField.setBorder(new javax.swing.border.TitledBorder("Label"));
        labelField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(labelField, gridBagConstraints);

        okayBtn.setText("Okay");
        okayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okayBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        getContentPane().add(okayBtn, gridBagConstraints);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        getContentPane().add(cancelBtn, gridBagConstraints);

        updateField.setToolTipText("Time between updates");
        updateField.setBorder(new javax.swing.border.TitledBorder("Update Period (seconds)"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(updateField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }//GEN-END:initComponents

    private void okayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okayBtnActionPerformed
        // Add your handling code here:
              
        OID oid ;
        long updatePeriod ;
        int min, max ;
        
        try {
					oid = new DefaultOID(oidField.getText()) ;
					updatePeriod = Long.parseLong(updateField.getText()) ;
					min = Integer.parseInt(minField.getText()) ;
					max = Integer.parseInt(maxField.getText()) ;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "" + e) ;
					return ;
				}
				
				if( theMeter == null )
        	return ;
        
        hostName = (String)hostComboBox.getSelectedItem() ;
        community = new String(communityField.getPassword()) ;
        
        theMeter.setOID(oid) ;
       	theMeter.setUpdatePeriod(updatePeriod) ;
       
       	theMeter.setLabel(labelField.getText()) ;
       	theMeter.setMinimum(min) ;
       	theMeter.setMaximum(max) ;
        
        
        
    }//GEN-LAST:event_okayBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // Add your handling code here:
        
      	setVisible(false) ;
        
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void mibBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mibBtnActionPerformed
        // Add your handling code here:
        javax.swing.JDialog mibDialog = new javax.swing.JDialog() ;
        javax.swing.JPanel controlPanel = new javax.swing.JPanel() ;
      
        MIBJPanel mibPanel ;
        
        mibPanel = new MIBJPanel() ;
        
        mibDialog.setModal(true) ;
        
        mibDialog.getContentPane().setLayout(new java.awt.BorderLayout()) ;
        mibDialog.getContentPane().add(mibPanel, java.awt.BorderLayout.CENTER) ;
        
       	controlPanel.setLayout(new GridLayout(1, 2)) ;
        
        mibDialog.show() ;
        
    }//GEN-LAST:event_mibBtnActionPerformed

    private void labelFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_labelFieldActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new ValueMeterConfigDialog(new javax.swing.JFrame(), true, null).show();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField maxField;
    private javax.swing.JTextField labelField;
    private javax.swing.JPasswordField communityField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton mibBtn;
    private javax.swing.JTextField minField;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JButton okayBtn;
    private javax.swing.JTextField updateField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField oidField;
    private javax.swing.JButton cancelBtn;
    // End of variables declaration//GEN-END:variables
    
}
