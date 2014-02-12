/**
 * 
 */
package com.jsaiteja.utils;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author SaiTeja
 *
 */
public class ImageWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ImageWindow(String imgSrc)
	{
		this(imgSrc,imgSrc);
	}
	
	public ImageWindow(String name, String imgSrc)
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
	
}
