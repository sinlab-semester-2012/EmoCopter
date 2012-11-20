package eegUtils.gui;


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;

import eegUtils.util.DataBuffer;

import JSci.awt.DefaultGraph2DModel;
import JSci.awt.Graph2DModel;
import JSci.swing.JLineGraph;

public class RawSignalPlot extends JFrame {
	private static final long serialVersionUID = 1L;
	private DefaultGraph2DModel model = new DefaultGraph2DModel();
	private RawSignalDataSeries signalDataSeries;

	// DataBuffer contains incomming samples	
	private DataBuffer.Float buffer;
	
	// the size of the buffer
	private int size;
	
	// target array for plotting data
	private float[] target;
	
	// the graph
	private JTailedLineGraph graph;
	
	// draw new incomming samples at the end of the graph
	private boolean tailing = false;
	
	// show samples per second
	private boolean showSps = false;
	
	// total number of processed samples
	private long samples = 0;
	
	// startTime for calculating samples per second
	private long startTime = 0;
	
	public RawSignalPlot(int size) {
		super("Raw Signal");
				
		this.size = size;
		
		startTime = System.currentTimeMillis();
		
		buffer = new DataBuffer.Float(size);
		target = new float[size];

		signalDataSeries = new RawSignalDataSeries();
		
		model.addSeries(signalDataSeries);
		
		init();
	}

	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));
        
        graph = new JTailedLineGraph(model);
        graph.setColor(0, Color.red);
        
        panel.add(graph);
        
        add(panel,"Center");
        
        setSize(600, 400);
        setVisible(true);
	}
	
	public void setScale(float scale) {
		buffer.setScale(scale);
	}
	
	public float getScale() {
		return buffer.getScale();
	}
	
	public void setYExtrema(int min, int max) {
		graph.setYExtrema(min, max);
	}
	
	public void setGridLines(boolean value) {
		graph.setGridLines(value);
	}
	
	public void showSps(boolean showSps) {
		this.showSps = showSps;
	}
	
	public boolean getShowSps() {
		return this.showSps;
	}
	
	public void setTailing(boolean tailing) {
		this.tailing = tailing;
	}
	
	public boolean isTailing() {
		return this.tailing;
	}
	
	public void add(float value) {
		buffer.add(value);
		
		samples++;
		
		if(tailing) {
			buffer.getData(target);
			signalDataSeries.setValues(target);
		} else {
			signalDataSeries.setValues(buffer.data);
		}
	}
	
	public class JTailedLineGraph extends JLineGraph {
		private static final long serialVersionUID = 1L;

		public JTailedLineGraph(Graph2DModel graph2dModel) {
			super(graph2dModel);
		}
		
		@Override
		protected void offscreenPaint(Graphics graphics) {
			super.offscreenPaint(graphics);
			
			if(!tailing) {
				Point p1 = dataToScreen(samples, getYMaximum());
				Point p2 = dataToScreen(samples, getYMinimum());
			
				graphics.setColor(Color.BLUE);
				graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
			
			if(showSps) {
				int sps = (int) (samples / (double) ((System.currentTimeMillis()-startTime) / 1000));

				int x = getWidth() - 150;
				int y = 20;
				
				int width = 120;
				int height = 20;
				
				// draw info rect
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillRect(x, y, width, height);
				
				// draw info boundary
				graphics.setColor(Color.DARK_GRAY);
				graphics.drawRect(x, y, width, height);
				
				FontMetrics metrics = graphics.getFontMetrics();
				
				if(System.currentTimeMillis()-startTime > 1000) {
					graphics.drawString(String.format("%s samples/sec", sps), x+5, y + metrics.getHeight());	
				} else {
					graphics.drawString(String.format("calculating.."), x+5, y + metrics.getHeight());
				}
			}
		}
	}
	
	public class RawSignalDataSeries extends DefaultGraph2DModel.DataSeries {
		private static final long serialVersionUID = 1L;

		public RawSignalDataSeries() {
			setValues(new float[length()]);
		}
		
		@Override
		public float getXCoord(int x) {
			return (float) ((samples / size) * size) + x;
		}
		
		@Override
		public int length() {
			return size;
		}
	}
}
