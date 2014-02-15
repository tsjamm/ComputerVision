/**
 * 
 */
package com.jsaiteja.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.jsaiteja.utils.ImageWindow;

/**
 * @author SaiTeja
 *
 */
public class GrabCut {
	
	public void getForeground(String in, String out)
	{
		Mat image = Highgui.imread(in);
		ImageWindow.imshow("Input Image", in);
		
		int x = 600;
		int y = 10;
		int w = image.cols() - (x+10);
		int h = image.rows() - (y+10);
		Rect border = new Rect(x,y,w,h);
		
		Mat result = new Mat();
		Mat bgModel = new Mat();
		Mat fgModel = new Mat();
		Mat masker = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
		
		Imgproc.grabCut(image, result, border, bgModel, fgModel, 2, Imgproc.GC_INIT_WITH_RECT);
		
		Core.compare(result, masker, result, Core.CMP_EQ);
		
		Mat output = new Mat(image.size(),CvType.CV_8UC3, new Scalar(255,255,255));
		image.copyTo(output, result);
		Highgui.imwrite(out, output);
		ImageWindow.imshow("Output Image", out);
		
	}
	
	public static void runGrabCut(String in, String out)
	{
		System.out.println("Starting GrabCut");
		GrabCut gc = new GrabCut();
		gc.getForeground(in, out);
		System.out.println("Completed GrabCut");
	}
}
