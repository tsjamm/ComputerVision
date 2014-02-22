/**
 * 
 */
package com.jsaiteja.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.jsaiteja.utils.ImageWindow;
import com.jsaiteja.utils.SelectionArea;
import com.jsaiteja.utils.SelectionListener;

/**
 * @author SaiTeja
 *
 */
public class GrabCut implements SelectionListener{
	
	Mat image;
	String in;
	String out;
	boolean writeToFile;
	
	public GrabCut()
	{
		SelectionArea.addListener(this);
	}
	
	public void getForeground(String in, String out, boolean writeToFile)
	{
		
		this.in = in;
		this.out = out;
		this.writeToFile = writeToFile;
		this.image = Highgui.imread(in);
		Imgproc.resize(image, image, new Size(640, 480));
		
		ImageWindow.imGetSelection(image.clone());
	
	}
	
	public static void runGrabCut(String in, String out)
	{
		runGrabCut(in, out, true);
	}
	public static void runGrabCut(String in, String out, boolean writeToFile)
	{
		System.out.println("Starting GrabCut");
		GrabCut gc = new GrabCut();
		gc.getForeground(in, out, writeToFile);
		System.out.println("Completed GrabCut");
	}
	
	@Override
	public void getSelection(Rect selection) {
		
		System.out.println("in grabcut's getselection...");
		
		Rect border = selection;
		
		Mat result = new Mat();
		Mat bgModel = new Mat();
		Mat fgModel = new Mat();
		Mat masker = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
		System.out.println("mat cols "+image.cols());
		System.out.println("mat rows "+image.rows());
		Imgproc.grabCut(image, result, border, bgModel, fgModel, 2, Imgproc.GC_INIT_WITH_RECT);
		
		Core.compare(result, masker, result, Core.CMP_EQ);
		
		Mat output = new Mat(image.size(),CvType.CV_8UC3, new Scalar(255,255,255));
		image.copyTo(output, result);
		if(writeToFile)
			Highgui.imwrite(out, output);
		ImageWindow.imshow("Output Image", out);
	}
}
