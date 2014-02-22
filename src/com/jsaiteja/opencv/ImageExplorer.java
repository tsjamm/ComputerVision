/**
 * 
 */
package com.jsaiteja.opencv;

import org.opencv.core.Core;

import com.jsaiteja.utils.ClassUtils;

/**
 * @author SaiTeja
 *
 */
public class ImageExplorer {
	
	public ImageExplorer()
	{
		//TODO
	}

	
	// A dummy test method for testing (Duh :P)
	private void test()
	{
		System.out.println(ClassUtils.getCurrentClassUrl(getClass()));
		System.out.println(ClassUtils.getCurrentProjectUrl(getClass()));
	}
	
	// This method that actually calls the various methods for processing
	public void run(String args[])
	{
		System.out.println("Run started");
		switch(args.length)
		{
			case 0: 
				//System.out.println(getClass().getResource("").getPath());
				test();
				break;
			case 1:
				
				break;
			case 2:
				//FaceDetector.getFaces(args[0], args[1]);
				GrabCut.runGrabCut(args[0], args[1]);
				break;
			case 3:
				SeamEstimator.runEstimator(args[0],args[1],args[2]);
				break;
			default:
				System.out.println("Number of arguments not usable.... enter correct number of args");
				break;
		}
		System.out.println("Run completed");
		return;
	}
	
	public static void main(String args[])
	{
		System.out.println("main of ImageExplorer started...");
		System.out.println("Number of Arguments = "+args.length);
		
		//Need to call this at least once or will result in exception
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		ImageExplorer ie = new ImageExplorer();
		ie.run(args);

		System.out.println("main of ImageExplorer ended...");
	}
	
}
