/**
 * 
 */
package com.jsaiteja.opencv;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.jsaiteja.utils.ImageWindow;
import com.jsaiteja.utils.SelectionArea;
import com.jsaiteja.utils.SelectionListener;

/**
 * @author SaiTeja
 * TODO
 */
public class SeamEstimator implements SelectionListener {
	
	Mat image1,image2;
	String in1,in2;
	String out;
	boolean writeToFile;
	
	public SeamEstimator()
	{
		SelectionArea.addListener(this);
	}
	
	/**
	 * This method uses laplacian and gaussian methods to combine
	 * left and right parts of two images into one.
	 * 
	 * @param in1
	 * @param in2
	 * @param out
	 * @param writeToFile
	 */
	public void halfCombine(String in1, String in2, String out, boolean writeToFile)
	{
		this.in1 = in1;
		this.in2 = in2;
		this.out = out;
		this.writeToFile = writeToFile;
		this.image1 = Highgui.imread(in1);
		this.image2 = Highgui.imread(in2);
		Imgproc.resize(image1, image1, new Size(640, 480));
		Imgproc.resize(image2, image2, new Size(640, 480));
		
		
		Mat G = new Mat();
		
		List<Mat> gpA = new ArrayList<Mat>();
		List<Mat> gpB = new ArrayList<Mat>();
		List<Mat> lpA = new ArrayList<Mat>();
		List<Mat> lpB = new ArrayList<Mat>();
		
		G = image1.clone();
		gpA.add(G.clone());
		for(int i=0;i<6;i++)
		{
			Imgproc.pyrDown(G, G);
//			Imgproc.GaussianBlur(G, G,new Size(3,3), 0, 0, Imgproc.BORDER_DEFAULT);
			gpA.add(G.clone());
			System.out.println("i="+i+" r="+G.rows()+" c="+G.cols());
		}
		
		G = image2.clone();
		gpB.add(G.clone());
		for(int i=0;i<6;i++)
		{
			Imgproc.pyrDown(G, G);
//			Imgproc.GaussianBlur(G, G,new Size(3,3), 0, 0, Imgproc.BORDER_DEFAULT);
			gpB.add(G.clone());
		}
		
		//Mat GE = new Mat(G.rows(), G.cols(), G.type());
//		Mat L = new Mat(G.rows(), G.cols(), G.type());
		Mat GE = new Mat();
		Mat L = new Mat();
		
		lpA.add(gpA.get(5).clone());
		for(int i=5;i>0;i--)
		{
			Imgproc.pyrUp(gpA.get(i), GE);
			System.out.println(i+" rows "+gpA.get(i-1).rows()+"   "+gpA.get(i).rows()+"   "+GE.rows());
			System.out.println(i+" cols "+gpA.get(i-1).cols()+"   "+gpA.get(i).cols()+"   "+GE.cols());
			Core.subtract(gpA.get(i-1), GE, L);
//			Imgproc.Laplacian(gpA.get(i), L, i);
			lpA.add(L.clone());
		}
		
		lpB.add(gpB.get(5).clone());
		for(int i=5;i>0;i--)
		{
			Imgproc.pyrUp(gpB.get(i), GE);
			Core.subtract(gpB.get(i-1), GE, L);
//			Imgproc.Laplacian(gpB.get(i), L, i);
			lpB.add(L.clone());
		}
		
		List<Mat> LS = new ArrayList<Mat>();
		for(int i=0;i<lpA.size();i++)
		{
			Mat one = lpA.get(i).clone();
			Mat two = lpB.get(i).clone();
			
			Mat combine = one.clone();
			for(int j=0;j<one.cols();j++)
			{
				for(int k=0;k<one.rows();k++)
				{
					if(j<one.cols()/2)
					{
						//combine.put(k, j, one.get(k, j));
					}
					else
					{
						combine.put(k, j, two.get(k, j));
					}
				}
			}
			System.out.println("i="+i+" combine rows="+combine.rows()+" cols="+combine.cols());
			LS.add(combine.clone());
		}
		
		Mat output = LS.get(0).clone();
		System.out.println("output rows="+output.rows()+" cols="+output.cols());
		for(int i=1;i<6;i++)
		{
			Imgproc.pyrUp(output, output);
			Core.add(output, LS.get(i), output);
			System.out.println("i="+i+"output rows="+output.rows()+" cols="+output.cols());
		}
		System.out.println("output rows="+output.rows()+" cols="+output.cols());
		if(writeToFile)
			Highgui.imwrite(out, output);
		ImageWindow.imshow("Output Image", output);
	}
	
	public static void runEstimator(String in1, String in2, String out)
	{
		System.out.println("Starting Estimator");
		SeamEstimator se = new SeamEstimator();
		se.halfCombine(in1, in2, out, true);
		System.out.println("Completed Estimator");
	}
	
	
	@Override
	public void getSelection(Rect selection)
	{
		
		
	}
	
}
