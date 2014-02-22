/**
 * 
 */
package com.jsaiteja.utils;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * @author SaiTeja
 *
 */
@SuppressWarnings("serial")
public class SelectionArea extends JPanel
{
	
	BufferedImage image;
	Mat imageBase;
	Graphics2D g2d;
	Point startPoint = null;
	Point endPoint = null;

	static List<SelectionListener> listeners = new ArrayList<SelectionListener>();
	
	public SelectionArea()
	{
		//setBackground(Color.WHITE);

		MyMouseListener ml = new MyMouseListener();
		addMouseListener(ml);
		addMouseMotionListener(ml);
	}
	public SelectionArea(Mat img)
	{
		this();
		this.imageBase = img;

	}
	
    public static void addListener(SelectionListener toAdd) {
        listeners.add(toAdd);
    }
	
    public void invokeListener()
    {
    	for(SelectionListener sl : listeners)
    	{
    		sl.getSelection(getSelectionRectangle());
    	}
    }
    
	public Rect getSelectionRectangle()
	{
		org.opencv.core.Point sp = new org.opencv.core.Point(startPoint.x, startPoint.y);
		org.opencv.core.Point ep = new org.opencv.core.Point(endPoint.x, endPoint.y);
		Rect selection = new Rect(sp, ep);
		System.out.println("sp"+sp.toString());
		System.out.println("ep="+ep.toString());
		System.out.println("x="+selection.x);
		System.out.println("y="+selection.y);
		System.out.println("h="+selection.height);
		System.out.println("w="+selection.width);
		return selection;
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if(image == null)
		{
			createEmptyImage();
		}

		g.drawImage(image, 0, 0, null);

		if (startPoint != null && endPoint != null)
		{
			int x = Math.min(startPoint.x, endPoint.x);
			int y = Math.min(startPoint.y, endPoint.y);
			int width = Math.abs(startPoint.x - endPoint.x);
			int height = Math.abs(startPoint.y - endPoint.y);
			g.drawRect(x, y, width, height);
		}
	}

	private void createEmptyImage()
	{
		Imgproc.resize(imageBase, imageBase, new Size(640, 480));
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".png", imageBase, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		try
		{
			InputStream in = new ByteArrayInputStream(byteArray);
			image = ImageIO.read(in);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		g2d = (Graphics2D)image.getGraphics();
		g2d.setColor(Color.BLACK);
	}

	public void addRectangle(int x, int y, int width, int height, Color color)
	{
		g2d.setColor(color);
		g2d.drawRect(x, y, width, height);
		repaint();
	}

	public void clear()
	{
		createEmptyImage();
		repaint();
	}

	class MyMouseListener extends MouseInputAdapter
	{
		private int xMin;
		private int xMax;
		private int yMin;
		private int yMax;

		public void mousePressed(MouseEvent e)
		{
			clear();
			startPoint = e.getPoint();
			endPoint = startPoint;
			xMin = startPoint.x;
			xMax = startPoint.x;
			yMin = startPoint.y;
			yMax = startPoint.y;
		}

		public void mouseDragged(MouseEvent e)
		{
			endPoint = e.getPoint();
			xMin = Math.min(xMin, endPoint.x);
			xMax = Math.max(xMax, endPoint.x);
			yMin = Math.min(yMin, endPoint.y);
			yMax = Math.max(yMax, endPoint.y);
			repaint(xMin, yMin, xMax - xMin + 1, yMax - yMin + 1);
		}

		public void mouseReleased(MouseEvent e)
		{
			int x = Math.min(startPoint.x, endPoint.x);
			int y = Math.min(startPoint.y, endPoint.y);
			int width = Math.abs(startPoint.x - endPoint.x);
			int height = Math.abs(startPoint.y - endPoint.y);

			if (width != 0 && height != 0)
			{
				addRectangle(x, y, width, height, e.getComponent().getForeground());
				
				invokeListener();
			}
			startPoint = null;
		}
	}
}