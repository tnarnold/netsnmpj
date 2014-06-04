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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.* ;
import javax.swing.border.TitledBorder;

import org.netsnmp.NetSNMPSession;
import org.netsnmp.framework.GetElement;
import org.netsnmp.framework.SessionManager;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class GetBox extends JFrame {
	
	private MIBDialog mibDialog ;
	/**
	 * record for serializable
	 * @author
	 *
	 */
	
	public static class panelEntry implements Serializable {
		String panelName ;
		JComponent comp ;
		panelEntry(String panelName, JComponent comp) {
			this.panelName = panelName ;
			this.comp = comp ;
		}
	}
			
	private static class sessionEntry implements Serializable {
		SessionManager mgr ; // key field
		
		private ArrayList compenents = new ArrayList() ;
		
		public int hashCode() { return mgr.hashCode() ; }
		
		public void addComponent(String panelName, JComponent comp) {
			compenents.add(new panelEntry(panelName, comp)) ;
		}
		
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		int i, n ;
		// write out the panel names
		
		getContentPane().remove(tabbedPane) ;
		
		out.defaultWriteObject() ;
		
		n = tabbedPane.getTabCount() ;
		out.writeInt(n) ;
		for( i = 0 ; i < n ; i++ ) {
			out.writeObject(tabbedPane.getTitleAt(i)) ;
		}
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER) ;
		
	}
	
	private Hashtable sessionsTable = new Hashtable() ;

	
	private boolean configurationChanged = false ;
	private java.io.File configFile = null ;
	
	private JMenuBar menuBar = new JMenuBar() ;
	private JMenu    fileMenu = new JMenu("File") ;
	private JMenu    panelMenu = new JMenu("Panels") ;
	private JTabbedPane tabbedPane = new JTabbedPane() ;
	
	private JMenuItem addNewMeterItem = new JMenuItem("Add New Meter...") ;


	private sessionEntry getSessionEntry(Object key) {
		sessionEntry ent ;
		
		ent = (sessionEntry)sessionsTable.get(key) ;
		if( ent == null ) {
			// create a new entry
			ent = new sessionEntry() ;
			if( NetSNMPSession.class.isInstance(key))
				ent.mgr = new SessionManager((NetSNMPSession)key) ;
			else if( SessionManager.class.isInstance(key) )
				ent.mgr = (SessionManager)key ;
			else
				throw new IllegalStateException("neither a SessionManager or a NetSNMPSession") ;
			sessionsTable.put(key, ent) ;
		}
		
		return ent ;
		
	}
	
	/**
	 * Return the panel with this name, or a new one.
	 * 
	 * @return
	 */
	private GetDisplayPanel addNewPanel(String name) {
		GetDisplayPanel panel ;
		JScrollPane scrollPane ;
		int i, n = tabbedPane.getTabCount() ;
		
		/*
		 * Note:  The number of panels is expected to be small(<25) so
		 * a hash or other more sophisticated collection isn't necessary
		 */
		
		for( i = 0 ; i < n ; i++ ) {
			if( !name.equals(tabbedPane.getTitleAt(i)))	
				continue ;
		
			scrollPane = (JScrollPane)tabbedPane.getComponentAt(i) ;
			return (GetDisplayPanel)scrollPane.getViewport().getComponent(0) ;
		}

		scrollPane = new JScrollPane() ;
		panel = new GetDisplayPanel() ;

		panel.setLayout(new GridLayout(0, 1)) ;

		scrollPane.setViewportView(panel) ;

		tabbedPane.add(name, scrollPane) ;
		configurationChanged = true ;
		menuLogic() ;

		return panel ;
		
	}
	
	private void initMibDialog() {
		if( mibDialog != null )
			return ;	
	
		mibDialog = new MIBDialog(GetBox.this, true) ;
		mibDialog.addDefaultMIBs() ;
		mibDialog.showSessionPanel() ;
	}

	private void menuLogic() {
		
		/*
		 *  if there are no panels we can't add any meters
		 */
		if( tabbedPane.getComponentCount() == 0 )
			addNewMeterItem.setEnabled(false) ;
		else
			addNewMeterItem.setEnabled(true) ;
		
	}
		
	private GetDisplayPanel getFrontPanel() {
		int idx = tabbedPane.getSelectedIndex() ;
		JScrollPane pane = (JScrollPane)tabbedPane.getComponentAt(idx) ;
		
		return (GetDisplayPanel)pane.getViewport().getComponent(0) ;
	}

	/**
	 * Adds a new element to the frontmost panel
	 * @param c New element to add
	 */
	private void addNewItem(GetElement c) {
		sessionEntry s = getSessionEntry(mibDialog.createSession()) ;
		GetDisplayPanel p = getFrontPanel() ;
		String panelName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()) ;

		s.addComponent(panelName, (JComponent)c) ;

		s.mgr.submit(c) ;

		p.add((JComponent)c) ;
		configurationChanged = true ;
	}

	private class quitHandler implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if( !configurationChanged )
				System.exit(0) ;
				 
			switch( JOptionPane.showConfirmDialog(GetBox.this, "Save Changes?") ) {
				case JOptionPane.OK_OPTION:
					break ;
				case JOptionPane.CANCEL_OPTION:
					return ;
				case JOptionPane.NO_OPTION:
					System.exit(0) ;
			} // switch 		
			
		} // acitonPerformed

	} // quitHandler
	
	
	private class addNewLabelHandler implements ActionListener {
		JTextField updateField = new JTextField() ;
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			float t ;
			initMibDialog() ;
			updateField.setBorder(new TitledBorder("Update Period(Seconds):")) ;
			updateField.setColumns(7) ;
			JPanel auxPanel = mibDialog.getAuxPanel() ;
			auxPanel.removeAll() ;
			

			auxPanel.setLayout(new BorderLayout()) ;
			auxPanel.add(updateField, BorderLayout.CENTER) ;			
			
			mibDialog.setVisible(true) ; // waits until we've completed
			if( !mibDialog.good )
				return ;
			
			try {
				t = Float.parseFloat(updateField.getText()) ;	
			}
			catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(GetBox.this, e1) ;
				return ;
			}
			
			ASNLabel lbl = new ASNLabel() ;
			lbl.addOID(mibDialog.getOID()) ;
			lbl.setUpdatePeriod((long)(t * 1000.0)) ;
			
			addNewItem(lbl) ;
		}
	}
	
	private class addNewMeterHandler implements ActionListener {
		JTextField updateField, minField, maxField ;
		
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			float t ;
			int min, max ;
			long periodMS ; 
			
			setupMibDialog() ;
			
			if( !mibDialog.good ) 
				return ; // user pressed cancel
				
			try {
				t = Float.parseFloat(updateField.getText()) ;	
				min = Integer.parseInt(minField.getText()) ;	
				max = Integer.parseInt(maxField.getText()) ;
			} // try
			catch (RuntimeException ex) {
				JOptionPane.showMessageDialog(GetBox.this, ex) ;
				return ;
			}
			
			periodMS = (long)(t * 1000.0) ;
			
			ValueMeter mtr = new ValueMeter(mibDialog.getOID(), min, max) ;
			
			mtr.setBorder(new TitledBorder(mibDialog.getOID().toText())) ;
			mtr.setStringPainted(true) ;
			mtr.setUpdatePeriod(periodMS) ;
			
			addNewItem(mtr) ;
		} // actionPerformed
		
		public void setupMibDialog() {
			initMibDialog() ;
			updateField = new JTextField() ;	
			updateField.setBorder(new TitledBorder("Update Period(Seconds):")) ;
			updateField.setColumns(7) ;

			minField = new JTextField() ;
			minField.setText("0") ;
			minField.setBorder(new TitledBorder("Min")) ;
			maxField = new JTextField() ;
			maxField.setText("100") ;
			maxField.setBorder(new TitledBorder("Max")) ;

			JPanel auxPanel = mibDialog.getAuxPanel() ;
			auxPanel.removeAll() ;

			auxPanel.setLayout(new GridBagLayout()) ;		
			GridBagConstraints gbc = new GridBagConstraints() ;
			gbc.weightx = 1.0 ;
			gbc.fill = GridBagConstraints.HORIZONTAL ;

			auxPanel.add(updateField, gbc) ;
			auxPanel.add(minField, gbc) ;
			auxPanel.add(maxField, gbc) ;

			mibDialog.setVisible(true) ; // waits until we've completed
		}
	} // addNewMeterItemHandler
	
	private class saveHandler implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	private class saveAsHandler implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			saveConfig(null) ;
		}
	}

	private class newPanelHandler implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			
			String name = JOptionPane.showInputDialog(GetBox.this, "Name?") ;
			if( name == null )
				return ;
			
			addNewPanel(name) ;
		}

	}
	
	private class deletePanelHandler implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			
		}

	}
	
	private static class configFileSaver extends JFileChooser {
		
		JPanel pane = new JPanel() ;
		JCheckBox check = new JCheckBox("password:") ;
		JPasswordField pass = new JPasswordField() ;
		
		configFileSaver(File startDir) {
			super(startDir) ;
			
			pane.setLayout(new FlowLayout()) ;
			pane.add(check) ;
			pane.add(pass) ;
			
			setAccessory(pane) ;
			
		}

	}
	
	private void saveConfig(File f) {
		
		if( f == null ) {
			JFileChooser jf = new configFileSaver(new File(System.getProperty("user.dir"))) ;
			int result = jf.showSaveDialog(this) ;
			f = jf.getSelectedFile() ;
			if( result != JFileChooser.APPROVE_OPTION )
						return ;
		}
				
		ObjectOutputStream oos ;
		int i ;
		
		try {
			oos = new ObjectOutputStream(new FileOutputStream(f)) ;
				
			int nPanels = tabbedPane.getTabCount() ;
			oos.writeInt(nPanels) ;
			
			for( i = 0 ; i < nPanels ; i++ ) {
				oos.writeUTF(tabbedPane.getTitleAt(i)) ;
			}
			
			oos.writeObject(sessionsTable) ;
			
			oos.close() ;
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "error saving config:  " + e) ;
			e.printStackTrace();
			return ;
		}
		configurationChanged = false ;
	}
	
	private void setConfig(File f) {
		ObjectInputStream ois ;
		int i ;
		String str ;
		sessionEntry ent, ent2 ;
		Hashtable newSessionsTable ;
		SessionManager mgr ;
		
		try {
			ois = new ObjectInputStream(new FileInputStream(f)) ;
			
			
			/*
			 * read in the list of panels 
			 */
			
			int nPanels = ois.readInt() ;
			
			for( i = 0 ; i < nPanels ; i++ ) {
				str = ois.readUTF() ;
				GetDisplayPanel panel = addNewPanel(str) ;
			} // for
			
			
			/*
			 * Add the components and sessions managers
			 */
			newSessionsTable = (Hashtable)ois.readObject() ;
			
			Enumeration hashIt = newSessionsTable.elements() ;
			Iterator it ;
			panelEntry pentry ;
			JPanel p ;
			while( hashIt.hasMoreElements() ) {
				ent = (sessionEntry)hashIt.nextElement() ;
				
				ent2 = (sessionEntry)getSessionEntry(ent.mgr) ;
				mgr = ent2.mgr ;
				
				it = ent.compenents.iterator() ;
				
				while( it.hasNext() ) {
					pentry = (panelEntry)it.next() ;
					p = (JPanel)addNewPanel(pentry.panelName) ;
					p.add(pentry.comp) ;
					ent2.addComponent(pentry.panelName, pentry.comp) ;
					if( GetElement.class.isInstance(pentry.comp) ) 
						mgr.submit((GetElement)pentry.comp) ;
				}
			}
		} // try
		catch( Exception e ) {
			JOptionPane.showMessageDialog(this, "error restoring config:  " + e) ;
			e.printStackTrace();
		}

	}

	private void initMenus() {
		JMenuItem item ;
		/*
		 *  File Menu
		 */

		item = new JMenuItem("Save") ;
		item.addActionListener(new saveHandler()) ;
		fileMenu.add(item) ;
		
		item = new JMenuItem("Save As...") ;
		item.addActionListener(new saveAsHandler()) ;
		fileMenu.add(item) ;
		
		fileMenu.addSeparator() ;
		
		item = new JMenuItem("Quit") ;
		item.addActionListener(new quitHandler()) ;
		fileMenu.add(item) ;
		
		menuBar.add(fileMenu) ;
		
		/*
		 * Panels Menu
		 */
		item = new JMenuItem("New Panel...") ;
		item.addActionListener(new newPanelHandler()) ;
		panelMenu.add(item) ;
		
		item = new JMenuItem("Delete Panel...") ;
		item.addActionListener(new deletePanelHandler()) ;
		panelMenu.add(item) ;
		
		panelMenu.addSeparator() ;
		
		addNewMeterItem.addActionListener(new addNewMeterHandler()) ;
		panelMenu.add(addNewMeterItem) ;
		
		item = new JMenuItem("Add New Label...") ;
		item.addActionListener(new addNewLabelHandler()) ;
		panelMenu.add(item) ;
		
		
		menuBar.add(panelMenu) ;
		
		setJMenuBar(menuBar) ;
	}

	/**
	 * initilizes GUI elements
	 */
	private void initComponents() {
		initMenus() ;
		
		getContentPane().setLayout(new BorderLayout()) ;
		
		tabbedPane.setPreferredSize(new Dimension(400, 600)) ;
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER) ;
		
		pack() ;
		
	}

	/**
	 * Constructor for GetBox.
	 * @throws HeadlessException
	 */
	public GetBox() throws HeadlessException {
		super();
		initComponents() ;
		menuLogic() ;
	}

	/**
	 * Constructor for GetBox.
	 * @param gc
	 */
	public GetBox(GraphicsConfiguration gc) {
		super(gc);
		initComponents() ;
		menuLogic() ;
	}

	/**
	 * Constructor for GetBox.
	 * @param title
	 * @throws HeadlessException
	 */
	public GetBox(String title) throws HeadlessException {
		super(title);
		initComponents() ;
		
		menuLogic() ;
	}

	/**
	 * Constructor for GetBox.
	 * @param title
	 * @param gc
	 */
	public GetBox(String title, GraphicsConfiguration gc) {
		super(title, gc);
	}

	public static void main(String[] args) {
		GetBox frm = new GetBox() ;
		File f ;
		int i ;
		String notFoundString = new String() ;
		for( i = 0 ; i < args.length ; i++ ) {
			f = new File(args[0]) ;		
			if(!f.exists()) {
				notFoundString += args[0] + " " ;
				continue ;
			}
			
			frm.setConfig(f) ;
			frm.configurationChanged = false ;
		} // for
		if( notFoundString.length() > 0 )
			JOptionPane.showMessageDialog(null, "file(s) " + notFoundString + " were not found") ;
			
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		
		frm.setTitle("GetBox") ;
		frm.pack() ;
		frm.setVisible(true) ;
		
	}
}

/*
 * $Log: GetBox.java,v $
 * Revision 1.2  2003/04/21 00:41:34  aepage
 * periodic checkin
 *
 */