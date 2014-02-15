/**
 * 
 */
package com.jsaiteja.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * @author SaiTeja
 *
 */
public class ImageWindow {
	
	public static void imshow(String imgSrc)
	{
		imshow(imgSrc,imgSrc);
	}
	
	public static void imshow(String name, String imgSrc)
	{
		JFrame frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		
		ImageIcon image = new ImageIcon(imgSrc);
		frame.setSize(image.getIconWidth()+10, image.getIconHeight()+35);
		JLabel label = new JLabel("", image, JLabel.CENTER);
		frame.getContentPane().add(label);
		
		frame.validate();
		frame.setVisible(true);
	}
	
	public static void imshow(Mat img)
	{
		imshow("Image",img);
	}
	
	public static void imshow(String name, Mat img)
	{
		Imgproc.resize(img, img, new Size(640, 480));
	    MatOfByte matOfByte = new MatOfByte();
	    Highgui.imencode(".png", img, matOfByte);
	    byte[] byteArray = matOfByte.toArray();
	    BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        JFrame frame = new JFrame();
	        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        frame.pack();
	        frame.setVisible(true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
