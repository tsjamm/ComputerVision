/**
 * 
 */
package com.jsaiteja.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
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
	
	private Point tl, br;
	
	public void getForeground(String in, String in2, String out)
	{
		Mat image = Highgui.imread(in);
		Mat image2 = Highgui.imread(in2);
		new ImageWindow(in);
		new ImageWindow(in2);
		
		tl = new Point(20,20);
		br = new Point(70,70);
		
		//Imgproc.grabCut(image, new Mat(), new Rect(tl,br), new Mat(), new Mat(), 1, 0 /* GC_INIT_WITH_RECT */);
		
		Mat output = backgroundSubtracting(image, image2);
		
		Highgui.imwrite(out, output);
		new ImageWindow(out);
		
	}
	
	private Mat backgroundSubtracting(Mat img, Mat background) {
		Mat firstMask = new Mat();
		Mat bgModel = new Mat();
		Mat fgModel = new Mat();
		Mat mask;
		Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
		Mat dst = new Mat();
		Rect rect = new Rect(tl, br);

		Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel, 1, Imgproc.GC_INIT_WITH_RECT);
		//Core.compare(firstMask, source/* GC_PR_FGD */, firstMask, Core.CMP_EQ);

		Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255,
				255, 255));
		img.copyTo(foreground, firstMask);

		//return foreground;
		 Core.rectangle(img, tl, br, new Scalar(255,0,0));

		   Mat tmp = new Mat();
		   Imgproc.resize(background, tmp, img.size());
		   background = tmp;
		   mask = new Mat(foreground.size(), CvType.CV_8UC1, new Scalar(255, 255, 255));

		Imgproc.cvtColor(foreground, mask, 6/* COLOR_BGR2GRAY */);
		Imgproc.threshold(mask, mask, 254, 255, 1 /* THRESH_BINARY_INV */);

		Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
		background.copyTo(dst);

		background.setTo(vals, mask);

		Core.add(background, foreground, dst, mask);

		firstMask.release();
		source.release();
		bgModel.release();
		fgModel.release();
		vals.release();
		
		return dst;
	}
	
	public static void runGrabCut(String in, String in2, String out)
	{
		System.out.println("Starting GrabCut");
		GrabCut gc = new GrabCut();
		gc.getForeground(in, in2, out);
		System.out.println("Completed GrabCut");
	}
}
