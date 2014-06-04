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
/**
 * MIBJPanel.java
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */

package org.netsnmp.swingui;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.tree.* ;

import org.netsnmp.* ;


/**
 * A JPanel that provides a MIB Browser capablity
 *
 * @author Andrew E. Page <a href=mailto:aepage@users.sourceforge.net>aepage@users.sourceforge.net</a>
 */
public class MIBJPanel extends JPanel
{

	private JTree tree = new JTree() ;
	private JScrollPane scrollPane = new JScrollPane() ;
	private final OID rootOID ;
	private OID selectedOID = null ;
	
  /** Utility field used by event firing mechanism. */
  private javax.swing.event.EventListenerList listenerList =  null;
        
	/**
	 * Creates a new MIBJPanel
	 */
	public MIBJPanel() {
		this(new DefaultOID()) ;
	}
	
    /**
     *  Creates a new MIBJPanel with the tree root at rootOID
     *  @param rootOID OID to start the browswer
     */
      
	public MIBJPanel(OID rOID) {
		DefaultMutableTreeNode root ;
		MIB.leaf leaves[] ;
		this.rootOID = rOID ;
				
		this.setLayout(new BorderLayout()) ;
		
		this.add(scrollPane, BorderLayout.CENTER) ;
		scrollPane.setViewportView(tree) ;

		root = new DefaultMutableTreeNode() ;
		
		if( rOID != null ) {
			try {
				leaves = MIB.findLeaves(rOID) ;
			} catch (MIBItemNotFound e) {
				leaves = MIB.treeHead() ;
			}
		}
		else {
			leaves = MIB.treeHead() ;
		}

		addLeaves(root, leaves) ;
        tree.setModel(new DefaultTreeModel(root)) ;
		
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true) ;
        tree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                displayTreeTreeExpanded(evt);
            }
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
        });
		
		tree.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
                                            
                                                if( listenerList == null )
                                                    return ;
                                            
						int i, oids[] ;
						Object objs[], userObject ;
						OID oid ;
						Object [] listeners ;
						TreePath path = tree.getPathForLocation(e.getX(), e.getY()) ;
						
						if( path == null )
							return ; // not interested
						
						objs = path.getPath() ;
						oids = new int[objs.length-1] ;
						
						for( i = 0 ; i < oids.length ; i++ ) {
							userObject = ((DefaultMutableTreeNode)objs[i+1]).getUserObject() ;
							if( userObject.getClass() == MIB.leaf.class )
								oids[i] = ((MIB.leaf)userObject).id ;
							if( userObject.getClass() == Integer.class )
								oids[i] = ((Integer)userObject).intValue() ;
						}
						
						oid = rootOID.append(new DefaultOID(oids)) ;
						MIBEvent evt = new MIBEvent(this, oid) ;
						selectedOID = oid ;
						
						listeners = listenerList.getListenerList() ;
						for( i = listeners.length - 2 ; i >= 0 ; i -= 2 ) {
							if( listeners[i] != MIBJPanelListener.class )
								continue ;
							((MIBJPanelListener)listeners[i+1]).objectSelected(evt) ;
						}
						
					}
					
				}) ;
		
	}
	
	public OID getSelectedOID() {
		return selectedOID ;
	}
	
	protected void addLeaves(DefaultMutableTreeNode root, MIB.leaf[] leaves) {
		int i ;
		DefaultMutableTreeNode node ;
		
		for( i = 0 ; i < leaves.length ; i++ ) {
		   node = new DefaultMutableTreeNode(leaves[i]) ;
		   if( leaves[i].hasChildren )
			   node.add(new DefaultMutableTreeNode(new Integer(leaves[i].id)));
		   
		   root.add(node) ;
		}
  }
	private void displayTreeTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {
        // Add your handling code here:
        MIB.leaf leaves[] ;
        Object objs[] = evt.getPath().getPath() ;
        DefaultMutableTreeNode t ;
        int i, oids[] ;
        
        t = (DefaultMutableTreeNode)objs[objs.length-1] ;
        if( t.getChildCount() >= 1 && ((DefaultMutableTreeNode)t.getChildAt(0)).getUserObject().getClass() == MIB.leaf.class )
            return ; // already been populated
        
        oids = new int[objs.length-1] ;
        for( i = 1 ; i < objs.length ; i++ )
            oids[i-1] = ((MIB.leaf)((DefaultMutableTreeNode)objs[i]).getUserObject()).id ;
        
        try {
            leaves = MIB.findLeaves(rootOID.append(new DefaultOID(oids))) ;
        }
        catch( MIBItemNotFound e ) {
            // TBD check on this
            e.printStackTrace() ;
            return ;
        }
        
        t.remove(0) ;
        addLeaves(t, leaves) ;
        ((DefaultTreeModel)tree.getModel()).reload() ;
        tree.expandPath(evt.getPath()) ;
    }

	public static void main(String args[]) throws Exception {
		JFrame frm = new JFrame() ;
		MIBJPanel browser ;
		final JLabel label = new JLabel() ;
		
		if( args.length >= 1 ) {
			
			browser = new MIBJPanel(new DefaultOID(args[0])) ;
		}
		else {
			browser = new MIBJPanel() ;
		}
		
		frm.setTitle("MIB Browser") ;
		frm.getContentPane().setLayout(new BorderLayout()) ;
		frm.getContentPane().add(label, BorderLayout.SOUTH) ;
		
		browser.addMIBJPanelEvent(new MIBJPanelListener() {
					public void objectSelected(MIBEvent evt) {
						// System.out.println("Selected object " + evt.oid.toText() + " " + evt.oid.toString()) ;
						label.setText(evt.oid.toText() + " " + evt.oid.toString()) ;
					}
					
				}) ;
		
		
		frm.getContentPane().add(browser, BorderLayout.CENTER) ;
        
        frm.setSize(400,600) ;
        
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
        
        frm.show() ;
		
	}
        
        /** Registers MIBJPanelEvent to receive events.
         * @param listener The listener to register.
         */
        public synchronized void addMIBJPanelEvent(org.netsnmp.swingui.MIBJPanelListener listener) {
            if (listenerList == null ) {
                listenerList = new javax.swing.event.EventListenerList();
            }
            listenerList.add(org.netsnmp.swingui.MIBJPanelListener.class, listener);
        }
        
        /** Removes MIBJPanelEvent from the list of listeners.
         * @param listener The listener to remove.
         */
        public synchronized void removeMIBJPanelEvent(org.netsnmp.swingui.MIBJPanelListener listener) {
            listenerList.remove(org.netsnmp.swingui.MIBJPanelListener.class, listener);
        }
        
 
	
	
}

/*
 * $Log: MIBJPanel.java,v $
 * Revision 1.6  2003/04/29 12:18:44  aepage
 * modifications to use a JTabbedPane instead of a row of buttons.
 *
 * Revision 1.5  2003/03/31 00:43:08  aepage
 * removal of unuzed variables.
 *
 * Revision 1.4  2003/03/30 23:06:01  aepage
 * removal of unncessary imports
 *
 * Revision 1.3  2003/03/27 15:16:09  aepage
 * Reduction of exceptions thrown that really serve no critical purpose.
 *
 * Revision 1.2  2003/02/09 22:34:59  aepage
 * Added test for 0 listeners registered
 *
 * Revision 1.1.1.1  2003/02/07 23:56:51  aepage
 * Migration Import
 *
 * Revision 1.3  2003/02/07 22:04:41  aepage
 * comments
 *
 * Revision 1.2  2003/02/06 18:19:09  aepage
 * *** empty log message ***
 *
 * Revision 1.1  2003/02/06 01:35:27  aepage
 * Initial Checkin
 *
 * Revision 1.1  2003/02/05 02:25:02  aepage
 * Initial Checkin
 *
 */


