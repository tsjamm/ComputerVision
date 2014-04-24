/**
 * 
 */
package com.jsaiteja.opencv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.utils.Converters;

/**
 * @author SaiTeja
 *
 */
public class CameraCalibration {
	
	public static Size size=null;
	public static Vector<String> imageList=null;
	public static Vector<Point3> worldList=null;
	public static HashMap<String,HashMap<Point3,Point>> detailList=null;
	
	public static Vector<Mat> imagePointList=null;
	public static Vector<Mat> worldPointList=null;
	public static Vector<MatOfPoint2f> imagePointList1=null;
	public static Vector<MatOfPoint3f> worldPointList1=null;
	
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
	
	public static BufferedWriter getBufferedWriter(String filePath)
	{
		FileOutputStream file;
		try
		{
			file = new FileOutputStream(filePath);
		
			OutputStreamWriter osr = new OutputStreamWriter(file);
			BufferedWriter bw = new BufferedWriter(osr);
		
			return bw;
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void writeToResult(String toWrite, BufferedWriter bw)
	{
		try{
			bw.append(toWrite);
			bw.newLine();
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Couldnt write to file....");
		}
		
	}
	
	public static void readFromFile()
	{
		Mat baseImage = Highgui.imread("Dataset/images/et000.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
		size = baseImage.size();
		
		System.out.println(size);
		
		imageList = new Vector<String>();
		worldList = new Vector<Point3>();
		detailList = new HashMap<String,HashMap<Point3,Point>>();
		
		imagePointList = new Vector<Mat>();
		worldPointList = new Vector<Mat>();
		
		imagePointList1 = new Vector<MatOfPoint2f>();
		worldPointList1 = new Vector<MatOfPoint3f>();
		
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
				//System.out.println(details[0]);
				//System.out.println(details[1]);
				HashMap<Point3,Point> tempMap = new HashMap<Point3,Point>();
				Vector<Point> tempPointVector = new Vector<Point>();
				Vector<Point3> tempPoint3Vector = new Vector<Point3>();
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
					tempMap.put(point3d,point2d);
					//imagePointList.add();
					//worldPointList.add(point3d);
					tempPointVector.add(point2d);
					tempPoint3Vector.add(point3d);
					
					//not working
					//imagePointList1.add(new MatOfPoint2f(point2d));
					//worldPointList1.add(new MatOfPoint3f(point3d));
				}
				//System.out.println(tempMap.size());
				detailList.put(details[0],tempMap);
				Mat temp2dMat = Converters.vector_Point2d_to_Mat(tempPointVector);
				Mat temp3dMat = Converters.vector_Point3d_to_Mat(tempPoint3Vector);
				imagePointList.add(temp2dMat);
				worldPointList.add(temp3dMat);
				
				temp2dMat.convertTo(temp2dMat, CvType.CV_32F);
				temp3dMat.convertTo(temp3dMat, CvType.CV_32F);
				
				imagePointList1.add(new MatOfPoint2f(temp2dMat));
				worldPointList1.add(new MatOfPoint3f(temp3dMat));
				
			}
			imagePoints_br.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public static void calibrate()
	{
		BufferedWriter bw = getBufferedWriter("result.txt");
		
		Mat cameraMatrix = Calib3d.initCameraMatrix2D(worldPointList1, imagePointList1, size);
		
		Mat distCoeffs = new Mat();
		
		int flags = 0;
		flags |= Calib3d.CALIB_USE_INTRINSIC_GUESS;
		flags |= Calib3d.CALIB_ZERO_TANGENT_DIST;
		flags |= Calib3d.CALIB_FIX_K4;
		flags |= Calib3d.CALIB_FIX_K5;
		
		Vector<Mat> rvecs = new Vector<Mat>();
		Vector<Mat> tvecs = new Vector<Mat>();

		double error = Calib3d.calibrateCamera(worldPointList, imagePointList, size, cameraMatrix, distCoeffs, rvecs, tvecs, flags);
		
		System.out.println("Camera Matrix");
		System.out.println(cameraMatrix.dump());
		
		System.out.println("Rotation Matrices");
		writeToResult("Rotation Matrices",bw);
		for(Mat m : rvecs)
		{
			System.out.println(m.dump());
			writeToResult(m.dump(),bw);
		}
		
		System.out.println("Translation Matrices");
		writeToResult("Translation Matrices",bw);
		for(Mat m : tvecs)
		{
			System.out.println(m.dump());
			writeToResult(m.dump(),bw);
		}
		
		System.out.println("Distortion Coefficients");
		writeToResult("Distortion Coefficients",bw);
		System.out.println(distCoeffs.dump());
		writeToResult(distCoeffs.dump(),bw);
		
		System.out.println("The Reprojection Error is "+error);
		writeToResult("The Reprojection Error is "+error,bw);
		
		
		//checking the projected points
		System.out.println("Checking the projected points...");
		writeToResult("Checking the projected points...",bw);
		for(Map.Entry<String, HashMap<Point3,Point>> e : detailList.entrySet())
		{
			int index = Integer.parseInt(e.getKey());
			System.out.println("Image is "+imageList.get(index));
			writeToResult("Image is "+imageList.get(index),bw);
			for(Map.Entry<Point3, Point> e2 : e.getValue().entrySet())
			{
				MatOfPoint2f imagePoints = new MatOfPoint2f();
				Calib3d.projectPoints(new MatOfPoint3f(e2.getKey()), rvecs.get(index), tvecs.get(index), cameraMatrix, (new MatOfDouble(distCoeffs)), imagePoints);
				//System.out.println(imagePoints.dump() + " " + (new MatOfPoint2f(e2.getValue())).dump());
				writeToResult(imagePoints.dump() + " " + (new MatOfPoint2f(e2.getValue())).dump(),bw);
			}
			
		}
		
		try {
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		readFromFile();
		calibrate();
	}
	
}
