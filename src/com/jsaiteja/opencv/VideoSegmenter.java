/**
 * 
 */
package com.jsaiteja.opencv;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * @author SaiTeja
 *
 */
public class VideoSegmenter {
	
	private static final int K_FRAME_NUM = 10;
	private static final int MAX_FRAMES = 100;
//	private static final double TRESHOLD = 90000;
	
	public static void runSegmenter(String in, String out) throws Exception
	{
		VideoCapture vc = new VideoCapture();
		//VideoCapture vc = new VideoCapture(in);
		//VideoCapture vc = new VideoCapture(0);
		
		//System.out.println("Trying to open video: "+in);
		//vc.open(in);
		
		vc.open(0);
		
		int openCheckCount=0;
		int maxOpenChecks=10;
		while(!vc.isOpened())
		{
			System.out.println(openCheckCount+" ... Video not opened ... will try again "+(maxOpenChecks-openCheckCount)+" times");
			Thread.sleep(1000);
			openCheckCount++;
			if(openCheckCount>=maxOpenChecks)
			{
				System.out.println("Could not open the Video... Terminating...");
				return;
			}
		}
		
		List<Mat> histograms = new ArrayList<Mat>();
		List<Double> distances = new ArrayList<Double>();
		List<Mat> keyFrames = new ArrayList<Mat>();
		List<Mat> shotKeys = new ArrayList<Mat>();
		
		int count=0,keyCount=0;
		Mat frame = new Mat();
		while(vc.read(frame))
		{
			if(frame==null)
				break;
			if(count%K_FRAME_NUM==0)
			{
				System.out.println("Key Frame "+keyCount);
				String toWrite = out+keyCount+".bmp";
				System.out.println("Writing to file: "+toWrite);
				Highgui.imwrite(toWrite, frame);
				//break;
				
				keyFrames.add(frame.clone());
				
				Mat img = new Mat(frame.height(), frame.width(), CvType.CV_8UC2);
				Imgproc.cvtColor(frame, img, Imgproc.COLOR_RGB2GRAY);
				Vector<Mat> bgr_planes = new Vector<Mat>();
				Core.split(img, bgr_planes);
				
				Mat hist=new Mat();
				MatOfInt histSize=new MatOfInt(256);
				MatOfFloat histRange=new MatOfFloat(0.0f,255.0f);
				boolean accumulate = false;
				
				Imgproc.calcHist(bgr_planes, new MatOfInt(0),new Mat(), hist, histSize, histRange, accumulate);
				
				histograms.add(hist);
				//System.out.println(hist.dump());
				//String toWrite = out+count+".bmp";
				//System.out.println("Writing to file: "+toWrite);
				//Highgui.imwrite(toWrite, hist);
				
				keyCount++;
			}
			
			
			if(count==MAX_FRAMES)
				break;
			
			count++;
			
			
		}
		
		for(int h=1, sf=0 ; h<histograms.size() ; h++) {
			Mat hist = histograms.get(h-1);
			Mat nextHist = histograms.get(h);
			if(!hist.size().equals(nextHist.size())) {
				distances.add(-1.0);
				System.out.println("histograms not equal in size... -1 is the distance");
				continue;
			}
			
			double distance=0;
			for(int i=0;i<hist.rows();i++) {
				double diff = hist.get(i, 0)[0] - nextHist.get(i, 0)[0];
				diff = diff*diff;
				distance+=diff;
			}
			distance=Math.sqrt(distance);
			distances.add(distance);
			
//			System.out.println("distance at h="+h+" is d="+distance);
//			if(distance>TRESHOLD) {
//				shotKeys.add(keyFrames.get(h));
//				String toWrite = out+"Frame"+h+"-Shot"+sf+".bmp";
//				System.out.println("Writing to file: "+toWrite);
//				Highgui.imwrite(toWrite, shotKeys.get(sf));
//				sf++;
//			}
		}
		
		double threshold = 0,sum=0, highest=Integer.MIN_VALUE, lowest=Integer.MAX_VALUE;
		for(double dist: distances) {
			sum+=dist;
			if(dist>highest) highest = dist;
			if(dist<lowest) lowest = dist;
		}
		threshold = sum/distances.size();
		threshold = (threshold+highest)/2;
		for(int d=0, sf=0 ; d<distances.size() ; d++) {
			double distance = distances.get(d);
			System.out.println("distance at keyFrame="+d+" is "+distance + "    the treshold is "+threshold);
			if(distance>threshold) {
				shotKeys.add(keyFrames.get(d+1));
				String toWrite = out+"Frame"+(d+1)+"-Shot"+sf+".bmp";
				System.out.println("Writing to file: "+toWrite);
				Highgui.imwrite(toWrite, shotKeys.get(sf));
				sf++;
			}
		}
		
		vc.release();
		System.out.println("Done with runSegmenter Method....");
	}
	
	public static void main(String args[])
	{
		System.out.println("main of VideoSegmenter started...");
		System.out.println("Number of Arguments = "+args.length);
		
		//Need to call this at least once or will result in exception
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try{
			
			if(args.length==2)
				VideoSegmenter.runSegmenter(args[0],args[1]);
			else
				System.out.println("Usage: 2 arguments, input and output paths");
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("main of VideoSegmenter ended...");
	}
	
}
