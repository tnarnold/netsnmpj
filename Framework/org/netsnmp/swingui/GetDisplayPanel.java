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

import javax.swing.JPanel;
import javax.swing.* ;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.* ;

import org.netsnmp.* ;
import org.netsnmp.framework.* ;

/**
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 *
 * Object to control various UI versions of GetElement
 */
public class GetDisplayPanel extends JPanel {
	
	private JPopupMenu controlMenu = new JPopupMenu() ;
	private JMenuItem deleteItem = new JMenuItem("Delete") ;
	private JMenuItem moveUpItem = new JMenuItem("Move Up") ;
	private JMenuItem moveDownItem = new JMenuItem("MoveDown") ;
	private JMenuItem frameTitleItem = new JMenuItem("Set Frame Title...") ;
	
	private JMenuItem setUpdatePeriodItem = new JMenuItem("Set Update Period...") ;
	
	
	private JComponent targetComponent ;
	private GetElement targetElement ;
	private UpdatePeriodAccessor targetUpdateAccessor ;
	private LastUpdateProvider targetLastUpdateProvider ;
	
	
	/**
	 * Overridden by owning components
	 * @param e
	 */
	protected void deleteElementItem(GetElement e) {
		
	}
	
	protected void deleteComponentItem(JComponent c) {
		remove(c) ;	
		updateUI() ;
	}
	
	private void doSetUpdatePeriod() {
		String str ;
		long t ;
		str = JOptionPane.showInputDialog(this, "New Update Period:") ;
		if( str == null )
			return ;
		
		try {
			t = (long)(Float.parseFloat(str) * 1000.0) ;
		}
		catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "" + e) ;
			return ;
		}
		
		targetUpdateAccessor.setUpdatePeriod(t) ;
		
	}
	
	private int findComponentIndex(Component c) {
		int idx = -1 ;
		int i, n = getComponentCount() ; 
		for( i = 0 ; i < n ; i++ ) {
			if( c == getComponent(i) ) {
				idx = i ;
				break ;
			}
		} // for
		return idx ;
	}
	
	private class moveItemUpHandler implements ActionListener {

		/*
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			int idx = findComponentIndex(targetComponent) ;
			if( idx == -1 ) return ; // ???
			
			remove(targetComponent) ;
			add(targetComponent, idx - 1) ;
			updateUI() ;
		}
		
	}
	
	private class moveItemDownHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int idx = findComponentIndex(targetComponent) ;
			if( idx == -1 ) return ; // ???
			
			remove(targetComponent) ;
			add(targetComponent, idx+1) ;
		}
	}
		
	private void setupControlMenu() {
		
		controlMenu.setBorderPainted(true) ;
		
		
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( targetComponent != null )
					deleteComponentItem(targetComponent) ;
				if( targetElement != null )
					deleteElementItem(targetElement) ;
					
			}				
		}) ;
		
		setUpdatePeriodItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						doSetUpdatePeriod() ;
					}
		}) ;
		
		moveUpItem.addActionListener(new moveItemUpHandler()) ;
		moveDownItem.addActionListener(new moveItemDownHandler()) ;
		
		frameTitleItem.addActionListener(new frameTitleItemHandler()) ;
		addMouseListener(new popupMenuHandler()) ;
			
		controlMenu.add(deleteItem) ;
		controlMenu.add(moveUpItem) ;
		controlMenu.add(moveDownItem) ;
		
		controlMenu.add(frameTitleItem) ;
		
		controlMenu.addSeparator() ;
		
		controlMenu.add(setUpdatePeriodItem) ;
	}
	
	
	private class frameTitleItemHandler implements ActionListener {		
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			String result = JOptionPane.showInputDialog("Title for Control:") ;
			if( result == null ) 
				return ;
				
			targetComponent.setBorder(new TitledBorder(result)) ;
			targetComponent.updateUI() ;
		}
	}
	
	
	
	
	private class popupMenuHandler extends MouseAdapter {
		private void popupHandler(MouseEvent e) {
			Component c ;
			GetElement ge ;
			super.mouseClicked(e);
			
			if( !e.isPopupTrigger() )
				return ;
			
			if( (c = findComponentAt(e.getX(), e.getY())) == null )
				return ;
				
			targetComponent = null ;
			targetElement = null ;
			targetUpdateAccessor = null ;
			
			if( GetElement.class.isInstance(c) ) {
				targetElement = (GetElement)c ;
			}
			else {
			}
				
			if( JComponent.class.isInstance(c) ) {
				targetComponent = (JComponent)c ;
			}
			else {
			}
			
			/*
			 * If this is already the first component, don't bother moving it up 
			 */
			if( targetComponent != null && targetComponent == getComponent(0) )
				moveUpItem.setEnabled(false) ;
			else
				moveUpItem.setEnabled(true) ;
				
			/*
			 * If this is already the last component, don't bother moving it down
			 */
			if( targetComponent != null && targetComponent == getComponent(getComponentCount() - 1)) 
				moveDownItem.setEnabled(false) ;
			else
				moveDownItem.setEnabled(true) ;
		
			/*
			 * If we can set this items update period enable the item to do that
			 */		
		
			if( UpdatePeriodAccessor.class.isInstance(c)) {
				targetUpdateAccessor = (UpdatePeriodAccessor)c ;
				setUpdatePeriodItem.setEnabled(true) ;
			}
			else {
				setUpdatePeriodItem.setEnabled(false) ; 
			}
				
			
			controlMenu.show(e.getComponent(), e.getX(), e.getY()) ;
		}

		/**
		 * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			
			popupHandler(e) ;
		}

		/**
		 * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			
			popupHandler(e) ;
		}

	}
	
	GetDisplayPanel() {
		
		setupControlMenu() ;
		
	}
	
	private class sessionEntry {
		SessionManager mgr ;	
	}
	
	private static String sessionKey(String host, String community) {
		return host + "-" + community ;
	}
	
	
	public static void main(String args[]) throws MIBItemNotFound {
			JFrame frm = new JFrame() ;
			Container contentPane = frm.getContentPane() ;
			contentPane.setLayout(new BorderLayout()) ;
			
			frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
			
			GetDisplayPanel gdp = new GetDisplayPanel() ;
			
			gdp.setLayout(new GridLayout(0, 1)) ;
			
			ValueMeter mtr = new ValueMeter(new DefaultOID("UCD-SNMP-MIB::dskPercent.1")) ;
			ValueMeter mtr2 = new ValueMeter(new DefaultOID("UCD-SNMP-MIB::dskPercent.2")) ;
		
			mtr.setUpdatePeriod(5000) ;
			mtr.setStringPainted(true) ;
			mtr.setBorderPainted(true) ;
			
			mtr2.setUpdatePeriod(5000) ;
			mtr2.setStringPainted(true) ;
			mtr2.setBorderPainted(true) ;
			
			gdp.add(mtr) ;
			gdp.add(mtr2) ;
			
			gdp.setMinimumSize(new Dimension(200, 300)) ;
			
			frm.pack() ;
			contentPane.add(gdp, BorderLayout.CENTER) ;
			
			
			SessionManager mgr = new SessionManager("localhost", "abecrombe") ;
			
			mgr.submit(mtr, 1000) ;
			mgr.submit(mtr2, 1500) ;
			
			frm.setVisible(true) ;
	} // main

}
