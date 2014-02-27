/**
 * 
 */
package com.jsaiteja.opencv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

/**
 * @author SaiTeja
 *
 */
public class CameraCalibration {

	public static Size size=null;
	public static Vector<String> imageList=null;
	public static Vector<Point3> worldList=null;
	public static HashMap<String,HashMap<Point,Point3>> detailList=null;
	
	public static BufferedReader getBufferedReader(String filePath)
	{
		FileInputStream file;
		try
		{
			file = new FileInputStream(filePath);
		
			InputStreamReader isr = new InputStreamReader(file);
			BufferedReader br = new BufferedReader(isr);
		
			return br;
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void readFromFile()
	{
		Mat baseImage = Highgui.imread("Dataset/images/et000.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
		size = baseImage.size();
		
		System.out.println(size);
		
		imageList = new Vector<String>();
		worldList = new Vector<Point3>();
		detailList = new HashMap<String,HashMap<Point,Point3>>();
		
		try
		{
			BufferedReader imageList_br = getBufferedReader("Dataset/list.txt");
			String line = null;
			while((line = imageList_br.readLine()) !=null )
			{
				imageList.add(line);
				System.out.println(line);
			}
			imageList_br.close();
			
			BufferedReader worldPoints_br  = getBufferedReader("Dataset/WorldPoints.txt");
			line = null;
			while((line = worldPoints_br.readLine())!=null)
			{
				String coordinates[] = line.split(" ");
				double point3d[] = new double[3];
				for(int i=0;i<3;i++)
				{
					point3d[i] = Double.parseDouble(coordinates[i]);
				}
				worldList.add(new Point3(point3d[0],point3d[1],point3d[2]));
			}
			worldPoints_br.close();
			
			BufferedReader imagePoints_br  = getBufferedReader("Dataset/ImageDetails.txt");
			line = null;
			while((line = imagePoints_br.readLine())!=null)
			{
				String details[] = line.split(" ");
				System.out.println(details[0]);
				System.out.println(details[1]);
				HashMap<Point,Point3> tempMap = new HashMap<Point,Point3>();
				for(int i=0;i<Integer.parseInt(details[1]);i++)
				{
					double point5d[] = new double[5];
					if((line = imagePoints_br.readLine())!=null)
					{
						String abxyz[] = line.split(" ");
						for(int j=0;j<5;j++)
						{
							point5d[j] = Double.parseDouble(abxyz[j]);
						}
					}
					Point point2d = new Point(point5d[0],point5d[1]);
					Point3 point3d = new Point3(point5d[2],point5d[3],point5d[4]);
					tempMap.put(point2d,point3d);
				}
				
				detailList.put(details[0],tempMap);
				
			}
			imagePoints_br.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}

	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		readFromFile();
		
	}
	
}
