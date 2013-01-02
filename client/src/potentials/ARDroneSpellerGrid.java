package potentials;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;

public class ARDroneSpellerGrid extends JFrame{
	private static final long serialVersionUID = 1L;
	private ArrayList<JLabel> grid;
	public ARDroneSpellerGrid() {
		grid = new ArrayList<JLabel>();
		setBackground(Color.black);
		init();
	}
	
	private void init(){
		setTitle("ARDRone speller");
		setSize(340, 350);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(buildContentPane());
		setVisible(true);
	}
	
	private JPanel buildContentPane(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 3));
		//panel.setBackground(Color.white);
		ImageIcon RotateLeft = createImageIcon("img/Rotate_left.gif");
		ImageIcon Forward = createImageIcon("img/Forward.gif");
		ImageIcon RotateRight = createImageIcon("img/Rotate_right.gif");
		ImageIcon Left = createImageIcon("img/Left.gif");
		ImageIcon TakeOffLand = createImageIcon("img/TakeOff_Land.gif");
		ImageIcon Right = createImageIcon("img/Right.gif");
		ImageIcon Up = createImageIcon("img/UP.gif");
		ImageIcon Backward = createImageIcon("img/Backward.gif");
		ImageIcon Down = createImageIcon("img/DOWN.gif");
		
		grid.add(new JLabel(RotateLeft));
		grid.add(new JLabel(Forward));
		grid.add(new JLabel(RotateRight));
		grid.add(new JLabel(Left));
		grid.add(new JLabel(TakeOffLand));
		grid.add(new JLabel(Right));
		grid.add(new JLabel(Up));
		grid.add(new JLabel(Backward));
		grid.add(new JLabel(Down));
		
		for(int i=0 ; i<9 ; i++){
			panel.add(grid.get(i));
			grid.get(i).setBackground(Color.gray);
		}
		
		return panel;
	}
	
	public void flash(boolean axis, int position){
		darken();
		if(axis) {	// column
			for(int i=0 ; i<3 ; i++){
				grid.get(position + 3*i).setOpaque(false);
				grid.get(position + 3*i).repaint();
			}
		} else {	// row
			for(int i=0 ; i<3 ; i++){
				grid.get(3*position + i).setOpaque(false);
				grid.get(3*position + i).repaint();
			}
		}
	}
	
	/**
	 * Darken the whole grid.
	 */
	private void darken(){
		for(int i=0 ; i<9 ; i++){
			grid.get(i).setOpaque(true);
			grid.get(i).repaint();
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	private ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
}
