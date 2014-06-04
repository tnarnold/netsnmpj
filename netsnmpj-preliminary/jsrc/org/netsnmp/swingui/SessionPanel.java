/*
 * Created on Apr 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.netsnmp.swingui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.* ;
import javax.swing.border.TitledBorder;

import org.netsnmp.NetSNMP;
import org.netsnmp.NetSNMPSession;
import org.netsnmp.OID;
import org.netsnmp.SNMPVersion;
import org.netsnmp.SecurityLevel;
import org.netsnmp.SessionProvider;
import org.netsnmp.util.HostLister;

/**
 * @author aepage
 *
 *   A JPanel component that will present the user with a dialog to configure a
 * NetSNMPSession for SNMPv1, SNMPv2c or SNMPv3.
 * 
 * Unless the hostLister property has been modified, setting the property "org.netsnmp.hosts" to
 * a comma separated list of hostnames will populate the list of hosts.  Example:
 * -Dorg.netsnmp.hosts="locahost,localrouter,mailgatway"
 */
public class SessionPanel extends JPanel implements SessionProvider {

	protected HostLister hostList = NetSNMP.defaultHostList ;

	private JComboBox versionChoice = new JComboBox() ;
	private JComboBox hostBox = new JComboBox() ;
	private JComboBox securityLevel = new JComboBox() ;
	private JPasswordField communityField = new JPasswordField() ;
	private JPasswordField authPasswordField = new JPasswordField() ;
	private JPasswordField privPasswordField = new JPasswordField() ;
	
	private JTextField securityNameField = new JTextField() ;
	private JTextField contextField = new JTextField() ; // TBD combo box?
	
	private JComboBox authProtocolBox = new JComboBox() ;
	private JComboBox privProtocolBox = new JComboBox() ;
	
	
	public SNMPVersion getSNMPVersion() { return (SNMPVersion)versionChoice.getSelectedItem() ; }
	public SecurityLevel getSecurityLevel() { return (SecurityLevel)securityLevel.getSelectedItem() ; }
	public String getPeerName() { return (String)hostBox.getSelectedItem() ; }
	
	public String getCommunity() { return new String(communityField.getPassword()) ; }
	public String getContext() { return contextField.getText() ; }
	
	private java.awt.Window parentWin ;
	
	/**
	 * 
	 * @return authorization protocol selected
	 */
	public OID getAuthProtocol() {
		return ((SecurityOID)authProtocolBox.getSelectedItem()).oid ;
	}
	
	/**
	 * @return privacy protocol selected
	 */
	public OID getPrivProtocol() {
		return ((SecurityOID)privProtocolBox.getSelectedItem()).oid ;
	}
	
	public String getAuthPassword() {
		return new String(authPasswordField.getPassword()) ;
	}
	
	public String getPrivPassword() {
		return new String(privPasswordField.getPassword()) ;
	}
	
	public String getSecurityName() {
		return securityNameField.getText() ;
	}
	
	/**
	 * Creates a new session based on 
	 * @param open
	 * @return session created
	 */
	public NetSNMPSession createSession(boolean open) {
			NetSNMPSession sess = new NetSNMPSession() ;
		
			sess.setSNMPVersion(getSNMPVersion()) ;
			sess.setPeerName(getPeerName()) ;
			sess.setCommunity(getCommunity()) ;
			sess.setContextName(getContext()) ;
			sess.setSecurityName(securityNameField.getText()) ;
			sess.setSecurityLevel(getSecurityLevel()) ;
			sess.setAuthenticationProtocol(getAuthProtocol()) ;
			sess.setPrivacyProtocol(getPrivProtocol()) ;
			sess.setAuthPassword(getAuthPassword()) ;
			sess.setPrivPassword(getPrivPassword()) ;
		
			if( open )
				sess.open() ;
			return sess ;
		}
	
		public NetSNMPSession createSession() {
			return createSession(false) ;
		}
	
	 /**
	 * Add the host and snmp version items to the top row
	 *
	 */	
	private void hostRow() {
		GridBagConstraints gbc = new GridBagConstraints() ;
		gbc.gridx = 0 ;
		gbc.gridy = 0 ;
		gbc.fill = GridBagConstraints.NONE ;

		add(versionChoice, gbc) ;
		
		gbc.gridx = 1 ;
		gbc.gridwidth = 2 ;
		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.weightx = 10.0 ;
		
		add(hostBox, gbc) ;

	}
	
	private void configureCommunity() {
		GridBagConstraints gbc = new GridBagConstraints() ;
		removeAll() ;
		hostRow() ;
		
		gbc.gridy = 1 ;
		gbc.gridwidth = 3 ;
		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.weightx = 1.0 ;
		
		add(communityField, gbc) ;
		
		if( parentWin != null )
			parentWin.pack() ;
	}
	
	private void configureSecurity() {
		GridBagConstraints gbc = new GridBagConstraints() ;
		int row = 1 ;
		
		removeAll() ;
		hostRow() ;
		
		gbc.gridy = row++ ;
		gbc.weightx = 1.0 ;
		gbc.fill = GridBagConstraints.HORIZONTAL ;
	
		gbc.gridx = 0 ;
		add(securityLevel, gbc) ;
	
		gbc.gridx = 1 ;
		add(privProtocolBox, gbc) ;
		
		gbc.gridx = 2 ;
		add(authProtocolBox, gbc) ;
				
		gbc.weightx = 1.0 ;
		gbc.gridwidth = 3 ;

		gbc.gridx = 0 ;
		gbc.gridy = row++ ;
		add(securityNameField, gbc) ;
		
		gbc.gridy = row++ ;
		
		add(contextField, gbc) ;
		
		gbc.gridy = row++ ;
		
		add(authPasswordField, gbc) ;
		
		gbc.gridy = row++ ;
		
		add(privPasswordField, gbc) ;
		
		
		if( parentWin != null )
			parentWin.pack() ;
		
	}
	
	private class versionChanger implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			
			SNMPVersion vers = getSNMPVersion() ;
			
			if( vers == NetSNMP.SNMPv2c || vers == NetSNMP.SNMPv1 ) {			
				configureCommunity() ;
				updateUI() ;
				return ;
			}
			
			if( vers == NetSNMP.SNMPv3 ) {
				configureSecurity() ;
				updateUI() ;
				return ;
			}
			
		}
		
	}
	
	private static class SecurityOID {
		final OID oid ;
		final String name ;
		public String toString() {
			return name ;
		}
		SecurityOID(OID oid, String name) {
			this.oid = oid ;
			this.name = name ;
		}
	}
	
	
	public SessionPanel(java.awt.Window parentWin) {
		this(parentWin, NetSNMP.SNMPv2c) ;
	}
	
	public SessionPanel(java.awt.Window parentWin, SNMPVersion v) {
		String [] hosts ;
		int i ;
		
		this.parentWin = parentWin ;
		versionChanger versChanger = new versionChanger() ;
		
		setLayout(new GridBagLayout()) ;
		
		
		/*
		 * Combobox for the choice of which SNMP Version to use
		 */
		versionChoice.addItem(NetSNMP.SNMPv1) ;
		versionChoice.addItem(NetSNMP.SNMPv2c) ;
		versionChoice.addItem(NetSNMP.SNMPv3) ;
		
		versionChoice.setBorder(new TitledBorder("Vers:")) ;
		versionChoice.setSelectedItem(v) ;
		
		versionChoice.addActionListener(versChanger) ;
		versChanger.actionPerformed(null) ;
		
		/*
		 * Combobox for the choice of host
		 */
		 
		hostBox.setToolTipText("<html><font size=+1>protocol:hostname:port</font><p><h2>Examples:<p>localhost (defaults to UDP on port 161)<p>localhost:4444<p>tcp:localhost:4444</html>") ;
		hostBox.setEditable(true) ;
		hosts = hostList.hosts() ;
		
		for (i = 0 ; i < hosts.length ; i++) {
			hostBox.addItem(hosts[i]) ;
		}
	
		
		hostBox.setBorder(new TitledBorder("Host:")) ;
		
		/*
		 * Combobox for security level
		 */
		 
		securityLevel.addItem(NetSNMP.authNoPriv) ;
		securityLevel.addItem(NetSNMP.authPriv) ;
		securityLevel.addItem(NetSNMP.noAuth) ;
		securityLevel.setBorder(new TitledBorder("Sec Lvl:")) ;
		
		/*
		 * Authentication protocol combo
		 */
		authProtocolBox.addItem(new SecurityOID(NetSNMP.usmNoAuthOID, "none")) ;
		authProtocolBox.addItem(new SecurityOID(NetSNMP.usmMD5AuthOID, "MD5")) ;
		authProtocolBox.addItem(new SecurityOID(NetSNMP.usmSHAAuthOID, "SHA")) ;
		authProtocolBox.setBorder(new TitledBorder("Authentication:")) ;
		
		/*
		 * Privacy Protocol combo
		 */
		privProtocolBox.addItem(new SecurityOID(NetSNMP.usmNoPrivOID, "none")) ;
		privProtocolBox.addItem(new SecurityOID(NetSNMP.usmDESPrivOID, "DES")) ;
		privProtocolBox.setBorder(new TitledBorder("Privacy:")) ;
		/*
		 * Other Fields
		 */
		
		communityField.setBorder(new TitledBorder("Community:")) ;
		communityField.setText(System.getProperty("org.netsnmp.community", "")) ; // set default community
		
		securityNameField.setBorder(new TitledBorder("Security Name:  (snmptget -u)")) ;
		securityNameField.setToolTipText("<html>security username<p>corresponds to the -u option for snmpget</html>") ;
		
		contextField.setBorder(new TitledBorder("Context:  (snmpget -n)")) ;
		contextField.setToolTipText("<html>Destination context<p>corresponds to the -n option for snmpget</html>") ;
		
		authPasswordField.setBorder(new TitledBorder("Auth Password:  (snmpget -A)")) ;
		authPasswordField.setToolTipText("<html>Sets the authentication password<p>corresponds to the -A option for snmpget</html>") ;
		
		privPasswordField.setBorder(new TitledBorder("Priv Password:  (snmpget -X)")) ;
		privPasswordField.setToolTipText("<html>Sets the privacy authentication password<p>corresponds to the -X option for snmpget</html>") ;
		
		
	}
	
	/**
	 * Convenience function that will configure a session with the settings in the panel
	 * 
	 * @param s
	 */
	
	public void configureSession(NetSNMPSession s) {
		
		s.setCommunity(new String(communityField.getPassword())) ;
		s.setPeerName((String)hostBox.getSelectedItem()) ;
		s.setSNMPVersion((SNMPVersion)versionChoice.getSelectedItem()) ;
		s.setPrivacyProtocol(getPrivProtocol()) ;
		s.setAuthenticationProtocol(getAuthProtocol()) ;
		
	}

	public static void main(String[] args) {
		JFrame frm = new JFrame() ;
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		
		frm.getContentPane().setLayout(new java.awt.BorderLayout()) ;
		
		frm.getContentPane().add(new SessionPanel(frm)) ;
		
		frm.pack() ;
		
		frm.setVisible(true) ;
		
	}
}
