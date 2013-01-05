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
		String path = "../img/";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		ImageIcon RotateLeft = new ImageIcon(classloader.getResource(path + "Rotate_left.gif"));
		ImageIcon Forward = new ImageIcon(classloader.getResource(path + "Forward.gif"));
		ImageIcon RotateRight = new ImageIcon(classloader.getResource(path + "Rotate_right.gif"));
		ImageIcon Left = new ImageIcon(classloader.getResource(path + "Left.gif"));
		ImageIcon TakeOffLand = new ImageIcon(classloader.getResource(path + "TakeOff_Land.gif"));
		ImageIcon Right = new ImageIcon(classloader.getResource(path + "Right.gif"));
		ImageIcon Up = new ImageIcon(classloader.getResource(path + "UP.gif"));
		ImageIcon Backward = new ImageIcon(classloader.getResource(path + "Backward.gif"));
		ImageIcon Down = new ImageIcon(classloader.getResource(path + "DOWN.gif"));

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
}
