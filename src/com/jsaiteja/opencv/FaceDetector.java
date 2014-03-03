/**
 * 
 */
package com.jsaiteja.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

import com.jsaiteja.utils.ClassUtils;
import com.jsaiteja.utils.ImageWindow;

/**
 * @author SaiTeja
 *
 */
public class FaceDetector {
	
	//Takes an input image and returns an output image with
	public static void getFaces(String in, String out)
	{
		CascadeClassifier faceDetector = new CascadeClassifier(ClassUtils.getCurrentProjectResourcesUrl(FaceDetector.class)+"lbpcascade_frontalface.xml");
		Mat image = Highgui.imread(in);
		ImageWindow.imshow(in); //displays the image in a jframe
		
		if(image.empty())
		{
			System.out.println("Input image not loaded properly... returning");
			return;
		}
		
		MatOfRect faceDetections = new MatOfRect();
		
		faceDetector.detectMultiScale(image, faceDetections);
		
		System.out.println("Detected "+faceDetections.toArray().length+" faces");
		
		for(Rect face : faceDetections.toArray())
		{
			int x1 = face.x;
			int y1 = face.y;
			int x2 = face.x + face.width;
			int y2 = face.y + face.height;
			Point A = new Point(x1,y1);
			Point B = new Point(x2,y2);
			Scalar color = new Scalar(0,255,0);//Color in BGR format
			Core.rectangle(image, A, B, color);
		}
		
		System.out.println("Saving the Detections to Output file...");
		Highgui.imwrite(out, image);
		ImageWindow.imshow(out); //displays the image in a jframe
		return;
	}
	
	public static void main(String args[])
	{
		System.out.println("main of FaceDetector started...");
		System.out.println("Number of Arguments = "+args.length);
		
		//Need to call this at least once or will result in exception
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		if(args.length==2)
			FaceDetector.getFaces(args[0], args[1]);
		else
			System.out.println("Usage: 2 arguments, input and output paths");

		System.out.println("main of FaceDetector ended...");
	}
	
}
