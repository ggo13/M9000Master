package com.usi.m9000.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
public class PlafChanger extends JApplet {

	public void init() {

		JPLAFChanger jpc = new JPLAFChanger();
		getContentPane().add( jpc, BorderLayout.CENTER );
		jpc.setNativeLookAndFeel(true);
	}


	public static void main(String args[]) {

		JFrame f = new JFrame("Look'n'Feel changer");
		f.getContentPane().add( new JPLAFChanger() );

		f.pack();
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.setLocation(100,50);

        f.setVisible(true);
	}
}

class LandFChanger extends Container implements ActionListener {

	Frame root = null;

    public JRadioButton btnSys = new JRadioButton("System", false),
        btnXPlat = new JRadioButton("Metal", true);

    LandFChanger() {
        super();
        setLayout(new FlowLayout());

        ButtonGroup bg = new ButtonGroup();
        bg.add( btnSys );
        bg.add( btnXPlat );

        btnSys.addActionListener(this);
        btnXPlat.addActionListener(this);

		add( btnSys );
		add( btnXPlat );
    }

    public void initLookAndFeel(boolean bXPlatform) {

		if (root==null) {
	        root = getRoot();
		}
        try {
            if (bXPlatform) UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName() );
            else UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());

			updateFrames( root );
        } catch (Exception exc) {
            System.err.println("Error loading L&F: " + exc);
        }

        root.pack();
    }

    public final Frame getRoot() {

        Container root = getParent();
        if ( root!=null ) {
			while ( root.getParent()!=null ) {
				root = root.getParent();
			}
		} else { root = this; }

        return (Frame)root;
    }

    private void updateFrames(Frame f) {

		updateFrame(f);

		Frame[] allFrame = f.getFrames();
		for (int ii=0; ii<allFrame.length; ii++) {
			if (f!=allFrame[ii]) {
				// recursive call to catch all children
				updateFrame(allFrame[ii]);
			}
		}
	}

    public void updateFrame(Frame f) {

		SwingUtilities.updateComponentTreeUI( f );
		f.pack();

		// Window is the superclass of Dialog
		// get an array of the Windows and Dialogs
		Window[] allWindow = f.getOwnedWindows();
		for (int jj=0; jj<allWindow.length; jj++) {
			updateWindow( allWindow[jj] );
		}
	}

    private void updateWindow(Window w) {

        SwingUtilities.updateComponentTreeUI( w );
        w.pack();

        Window[] children = w.getOwnedWindows();
        for (int ii=0; ii<children.length; ii++) {
            // recursive call to catch all children
            updateWindow( children[ii] );
        }
    }

    public void actionPerformed(ActionEvent ae) {
        initLookAndFeel( ae.getSource()==btnXPlat );
    }
}

class JPLAFChanger extends JPanel implements ActionListener {

	LandFChanger lnfc = new LandFChanger();

    JDialog dInput;
    JTextField tfInput;
    JTextArea taInput;
    JTree tree;
    JLabel lStatus;

    String[] items = {
        "Editable", "Uneditable", "Disabled"
    };
    JComboBox cb;
    JButton btnAbout = new JButton("About"),
        btnInput = new JButton("Input");
    JTextArea textArea = new
    	JTextArea( "This UI is resizeable.\n\nThe ToolBar can be floated\n(drag using the bar on the left)\n\nThe Look and Feel can be changed as well\n(Select System/Metal).\n\n", 4,40 );
    JTable table;

    public JPLAFChanger() {

		setLayout( new BorderLayout() );

		textArea.setEnabled(false);
		tree = new JTree();
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			new JScrollPane( tree ), new JScrollPane( textArea ));

        add( sp, BorderLayout.CENTER );

		TableModel dataModel = new AbstractTableModel() {
			public int getColumnCount() { return 6; }
			public int getRowCount() { return 4;}
			public Object getValueAt(int row, int col) {
				return new Integer((row+1)*((col+1)*4));
			}
		};
		table = new JTable(dataModel);
        add( table, BorderLayout.SOUTH );

        JPanel toolPanel = new JPanel( new BorderLayout() );
        JToolBar tb = new JToolBar("JToolBar");

        tb.add( lnfc );

        tb.addSeparator();
        btnAbout.addActionListener(this);
        btnInput.addActionListener(this);
        tb.add( btnAbout );

        tb.addSeparator();
        tb.add( btnInput );
        cb = new JComboBox(items);
        cb.addActionListener(this);
        tb.add( cb );

        tb.addSeparator();
        lStatus = new JLabel("PhySci.codes");
        tb.add(lStatus);

        toolPanel.add( tb, BorderLayout.NORTH );

        add( toolPanel, BorderLayout.NORTH );
    }

    public void setNativeLookAndFeel(boolean nativeLnF) {
		lnfc.initLookAndFeel( !nativeLnF );
		if (nativeLnF) {
			lnfc.btnSys.setSelected(true);
		} else {
			lnfc.btnXPlat.setSelected(true);
		}
	}

    private void initializeInputDialog() {

        dInput = new JDialog();
        dInput.setTitle("Input Dialog");
        dInput.setModal(false);
        Container c = dInput.getContentPane();
        tfInput = new JTextField( "This is a single line text input area" );
        c.add( tfInput, BorderLayout.NORTH );

        taInput = new JTextArea("This is a \nmulti-line text area");
        c.add( taInput, BorderLayout.CENTER );
        dInput.setLocation(25,25);

        dInput.pack();
    }

    public void actionPerformed(ActionEvent ae) {

        String s = ae.getActionCommand();
        updateUser( s );
        if (s.equals("About")) {
            JOptionPane.showMessageDialog(this,
                "Look and Feel Changer Demo.", "About",
                JOptionPane.INFORMATION_MESSAGE );
        } else {
            if ( dInput==null ) {
                initializeInputDialog();
            }
            if ( s.equals("Input") ) {
                dInput.setVisible(true);
            } else {
                actOnSelect();
            }
        }
    }

    private final void updateUser( String s ) {

		textArea.append( s + "\n" );
		textArea.setCaretPosition( textArea.getText().length() );
	}

    private void actOnSelect() {

        int i = cb.getSelectedIndex();
        switch (i) {
            case 0:
                updateUser( "Enabled, Editable" );
                tfInput.setEditable(true);
                tfInput.setEnabled(true);
                taInput.setEditable(true);
                taInput.setEnabled(true);
                break;
            case 1:
                updateUser( "Not Editable" );
                tfInput.setEditable(false);
                taInput.setEditable(false);
                break;
            case 2:
                updateUser( "Disabled" );
                tfInput.setEnabled(false);
                taInput.setEnabled(false);
                break;
            default:
                updateUser("index " + i + " not recognized!");
        }
        dInput.show();
        dInput.toFront();
    }
}