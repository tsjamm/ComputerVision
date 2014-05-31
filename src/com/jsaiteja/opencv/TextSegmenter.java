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

/**
 * @author SaiTeja
 *
 */
public class TextSegmenter {


	public static void runSegmenter(String in, String out) throws Exception
	{
		// Define bounding rectangle - the pixels outside this rectangle will be labeled as background
		//Rect rectangle = new Rect(1,1,493,1153);
		Rect rectangle = new Rect(1,1,1153,493);

		Mat img = Highgui.imread(in);
		Mat result = new Mat();
		Mat bgModel = new Mat();
		Mat fgModel = new Mat();

		// GrabCut segmentation
		Imgproc.grabCut(img,	// input image
				result,			// segmentation result
				rectangle,		// rectangle containing foreground
				bgModel,fgModel,// models
				5,				// number of iterations
				Imgproc.GC_INIT_WITH_RECT); // use rectangle

		// Get the pixels marked as likely foreground
		Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
		Core.compare(result, source, result, Core.CMP_EQ);

		// Generate output image
		Mat foreground = new Mat (img.size(),Imgproc.COLOR_GRAY2RGB, new Scalar(255,255,255));
		img.copyTo(foreground,result); // bg pixels are not copied

		Highgui.imwrite(out, result);

		System.out.println("Done with runSegmenter Method....");
	}

	public static void main(String args[])
	{
		System.out.println("main of TextSegmenter started...");
		System.out.println("Number of Arguments = "+args.length);

		//Need to call this at least once or will result in exception
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		try{

			if(args.length==2)
				TextSegmenter.runSegmenter(args[0],args[1]);
			else
				System.out.println("Usage: 2 arguments, input, output paths");

		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("main of TextSegmenter ended...");
	}

}
