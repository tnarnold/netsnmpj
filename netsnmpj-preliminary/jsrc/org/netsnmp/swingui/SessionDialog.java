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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.netsnmp.NetSNMPSession;
import org.netsnmp.OID;
import org.netsnmp.SNMPVersion;
import org.netsnmp.SecurityLevel;
import org.netsnmp.SessionProvider;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 *  Presents the user with a dialog for configuring a NetSNMPSession.
 *
 * Unless the hostLister property has been modified, setting the property "org.netsnmp.hosts" to
 * a comma separated list of hostnames will populate the list of hosts.  Example:
 * -Dorg.netsnmp.hosts="locahost,localrouter,mailgatway"
 *
 */
public class SessionDialog extends JDialog implements SessionProvider {
	
	private SessionPanel sessionPanel ;
	private JButton okayBtn = new JButton("Okay"), cancelBtn = new JButton("Cancel") ;
	private JPanel auxPanel = new JPanel() ;
	
	public boolean good = false ;
	
	private class btnHandler implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if( e.getSource() == okayBtn )
				good = true ;
			else
				good = false ;
				
			setVisible(false) ;			
		}
		 
	}
	
	/**
	 * @return the container in the 'North' portion of the dialog
	 */
	public Container getAuxPanel() {
		return auxPanel ;
	}
	
	public SessionDialog() {
		sessionPanel = new SessionPanel(this) ;
		Container contentPane = getContentPane();
		JPanel controls = new JPanel() ;
		ActionListener actionListener = new btnHandler() ;
		
		contentPane.setLayout(new BorderLayout()) ;
		contentPane.add(sessionPanel, BorderLayout.CENTER) ;
		
		okayBtn.addActionListener(actionListener) ;
		cancelBtn.addActionListener(actionListener) ;
		controls.add(okayBtn) ;
		controls.add(cancelBtn) ;
		
		contentPane.add(controls, BorderLayout.SOUTH) ;
		
		contentPane.add(auxPanel, BorderLayout.NORTH) ;
		
		setModal(true) ;
		
		
	}

	public static void main(String[] args) {
		JDialog d = new SessionDialog() ;
		
		d.pack() ;
		d.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		d.show() ;
		System.out.println("done");
	}
	/**
	 * @return
	 */
	public NetSNMPSession createSession() {
		return sessionPanel.createSession();
	}

	/**
	 * @param open if true, session will be opened before returning
	 * @return a session configured to the choices made in the dialog
	 */
	public NetSNMPSession createSession(boolean open) {
		return sessionPanel.createSession(open);
	}

	/**
	 * @return
	 */
	public String getAuthPassword() {
		return sessionPanel.getAuthPassword();
	}

	/**
	 * @return
	 */
	public OID getAuthProtocol() {
		return sessionPanel.getAuthProtocol();
	}

	/**
	 * @return
	 */
	public String getCommunity() {
		return sessionPanel.getCommunity();
	}

	/**
	 * @return
	 */
	public String getContext() {
		return sessionPanel.getContext();
	}

	/**
	 * @return
	 */
	public String getPeerName() {
		return sessionPanel.getPeerName();
	}

	/**
	 * @return
	 */
	public String getPrivPassword() {
		return sessionPanel.getPrivPassword();
	}

	/**
	 * @return
	 */
	public OID getPrivProtocol() {
		return sessionPanel.getPrivProtocol();
	}

	/**
	 * @return
	 */
	public SecurityLevel getSecurityLevel() {
		return sessionPanel.getSecurityLevel();
	}

	/**
	 * @return
	 */
	public String getSecurityName() {
		return sessionPanel.getSecurityName();
	}

	/**
	 * @return
	 */
	public SNMPVersion getSNMPVersion() {
		return sessionPanel.getSNMPVersion();
	}

}

/*
 * $Log: SessionDialog.java,v $
 * Revision 1.1  2003/04/23 14:25:46  aepage
 * initial checkin
 *
 */
