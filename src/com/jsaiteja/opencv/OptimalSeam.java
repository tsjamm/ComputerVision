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
import com.jsaiteja.utils.kolmogorov.GraphCut;
import com.jsaiteja.utils.kolmogorov.Terminal;

/**
 * @author SaiTeja
 *
 */
public class OptimalSeam implements SelectionListener {
	
	Mat image1,image2;
	String in1,in2;
	String out;
	boolean writeToFile;
	
	public OptimalSeam() {
		SelectionArea.addListener(this);
	}
	
	/* (non-Javadoc)
	 * @see com.jsaiteja.utils.SelectionListener#getSelection(org.opencv.core.Rect)
	 */
	@Override
	public Mat getSelection(Rect selection) {
		
		Mat image1 = this.image1.clone();
		Mat image2 = this.image2.clone();
		
		Rect border = selection;
		Mat subImage = image1.submat(border);
		
		Mat graphCut = new Mat();
		
		int overlapWidth = border.width;
		int overlapHeight = border.height;
		int xoffset = border.x;
		int yoffset = border.y;
		
		Mat noGraphCut = new Mat(image2.rows(),image2.cols(), image2.type());
		
		int estNodes = overlapHeight*overlapWidth;
		int estEdges = estNodes*4;
		System.out.println("EstNodes is "+estNodes);
		System.out.println("EstEdges is "+estEdges);
		GraphCut g = new GraphCut(estNodes, estEdges);
		
		for(int y=0;y<overlapHeight;y++) {
			g.setTerminalWeights(y*overlapWidth, Integer.MAX_VALUE, 0);
			g.setTerminalWeights(y*overlapWidth+overlapWidth-1,Integer.MAX_VALUE, 0);
//			if(y>=(overlapHeight/5)*2 && y<=(overlapHeight/5)*4) {
//				g.setTerminalWeights(y*overlapWidth+(overlapWidth/5)*2, Integer.MAX_VALUE, 0);
//				g.setTerminalWeights(y*overlapWidth+(overlapWidth/5)*4-1,Integer.MAX_VALUE, 0);
//			}
		}
		for(int x=0;x<overlapWidth;x++) {
			g.setTerminalWeights(x, 0, Integer.MAX_VALUE);
			g.setTerminalWeights(x+(overlapWidth*(overlapHeight-1)),Integer.MAX_VALUE, 0);
//			if(x>=(overlapWidth/5)*2 && x<=(overlapWidth/5)*4) {
//				g.setTerminalWeights(x+(overlapWidth*(overlapHeight/5)*2), Integer.MAX_VALUE, 0);
//				g.setTerminalWeights(x+(overlapWidth*(overlapHeight/5)*4),Integer.MAX_VALUE, 0);
//			}
		}
		//g.setTerminalWeights(overlapHeight/2 + overlapWidth/2, Integer.MAX_VALUE, 0);
		
		int rightCount=0;
		int bottomCount=0;
		
		for(int y=0;y<overlapHeight;y++) {
			for(int x=0;x< overlapWidth; x++) {
				double cap0 = 0;
				double cap1 = 0;
				double cap2 = 0;
				
				double[] a0 = image1.get(y+yoffset, x+xoffset);
				double[] b0 = image2.get(y+yoffset, x+xoffset);
				
				for(int i=0;i<3;i++){
					cap0 += Math.pow(a0[i]-b0[i],2);
				}
				cap0 = Math.sqrt(cap0);
				
				//for right edge
				if(x+1< overlapWidth) {
					double[] a1 = image1.get(y+yoffset, x+xoffset+1);
					double[] b1 = image2.get(y+yoffset, x+xoffset+1);
					
					for(int i=0;i<3;i++){
						cap1 += Math.pow(a1[i]-b1[i],2);
					}
					cap1 = Math.sqrt(cap1);
					rightCount++;
					g.setEdgeWeight(y*overlapWidth+x, y*overlapWidth+x+1, (float)(cap0+cap1));
				}
				
				//for bottom edge
				if(y+1< overlapHeight) {
					double[] a2 = image1.get(y+yoffset+1, x+xoffset);
					double[] b2 = image2.get(y+yoffset+1, x+xoffset);
					
					for(int i=0;i<3;i++){
						cap2 += Math.pow(a2[i]-b2[i],2);
					}
					cap2 = Math.sqrt(cap2);
					bottomCount++;
					g.setEdgeWeight(y*overlapWidth+x, (y+1)*overlapWidth+x, (float)(cap0+cap2));
				}
				//System.out.println("cap0="+cap0+"cap1="+cap1+"cap2="+cap2);
			}
		}
		System.out.println("right = "+rightCount);
		System.out.println("bottom = "+bottomCount);
		
		double flow = g.computeMaximumFlow(true, null);
		System.out.println("flow is "+flow);

		//image1.copyTo(noGraphCut.submat(new Rect(0,0,image1.cols(),image1.rows())));
		//image2.copyTo(noGraphCut.submat(new Rect(xoffset,0,image2.cols(),image2.rows())));
		//image2.submat(new Rect(overlapWidth,0,image2.cols()-overlapWidth,image2.rows())).copyTo(noGraphCut.submat(new Rect(xoffset+overlapWidth,0,image2.cols()-overlapWidth,image2.rows())));
		//image1.submat(new Rect(0,0,xoffset,image1.rows())).copyTo(noGraphCut.submat(new Rect(0,0,xoffset,image1.rows())));
		
		image2.copyTo(noGraphCut);
		//image1.submat(border).copyTo(noGraphCut.submat(yoffset, overlapHeight+yoffset, xoffset, xoffset+overlapWidth));
		
		graphCut = noGraphCut.clone();
		int unlinkedCount=0;
		int sourceCount=0;
		int sinkCount=0;
		for(int y=0; y<overlapHeight; y++) {
			for(int x=0; x<overlapWidth; x++) {
				if(g.getTerminal(y*overlapWidth+x).equals(Terminal.FOREGROUND)) {
					graphCut.put(y+yoffset, xoffset+x, image1.get(y+yoffset, xoffset+x));
					sourceCount++;
				} else if(g.getTerminal(y*overlapWidth+x).equals(Terminal.BACKGROUND)) { 
					graphCut.put(y, x, image2.get(y, x));
					sinkCount++;
				} else {
					//double[] empty = {0.0,255.0,0.0};
					//graphCut.put(y, xoffset+x, empty);
					unlinkedCount++;
				}
			}
		}
		System.out.println("unlinkedCount is "+unlinkedCount);
		System.out.println("sourceCount is "+sourceCount);
		System.out.println("sinkCount is "+sinkCount);
		if(writeToFile)
			Highgui.imwrite(out, graphCut);
		ImageWindow.imshow("Output Image", graphCut);
		
		return graphCut;
	}
	
	public void seamCut(String in1, String in2, String out, boolean writeToFile) {
		this.in1 = in1;
		this.in2 = in2;
		this.out = out;
		this.writeToFile = writeToFile;
		this.image1 = Highgui.imread(in1);
		this.image2 = Highgui.imread(in2);
		Imgproc.resize(image1, image1, new Size(640, 480));
		Imgproc.resize(image2, image2, new Size(640, 480));
		
		ImageWindow.imGetSelection(image1.clone());
	}
	
	public void seamOverlayCut(String in1, String in2, String out, boolean writeToFile) {
		this.in1 = in1;
		this.in2 = in2;
		this.out = out;
		this.writeToFile = writeToFile;
		this.image1 = Highgui.imread(in1);
		this.image2 = Highgui.imread(in2);
		Imgproc.resize(image1, image1, new Size(640, 480));
		Imgproc.resize(image2, image2, new Size(640, 480));
		
		//ImageWindow.imshow(image1.clone());
		//ImageWindow.imshow(image2.clone());
		
		Mat graphCut;
		
		int overlapWidth = image1.cols()/2;
		//int overlapWidth = image1.cols();
		int xoffset = image1.cols() - overlapWidth;
		
		Mat noGraphCut = new Mat(image1.rows(),image1.cols()*2 - overlapWidth, image1.type());
		
		int estNodes = image1.rows()*overlapWidth;
		int estEdges = estNodes*4;
		System.out.println("EstNodes is "+estNodes);
		System.out.println("EstEdges is "+estEdges);
		GraphCut g = new GraphCut(estNodes, estEdges);
		
		for(int y=0;y<noGraphCut.rows();y++) {
			g.setTerminalWeights(y*overlapWidth, Integer.MAX_VALUE, 0);
			g.setTerminalWeights(y*overlapWidth+overlapWidth-1, 0,Integer.MAX_VALUE);
		}
		
		int rightCount=0;
		int bottomCount=0;
		
		for(int y=0;y<noGraphCut.rows();y++) {
			for(int x=0;x< overlapWidth; x++) {
				double cap0 = 0;
				double cap1 = 0;
				double cap2 = 0;
				
				double[] a0 = image1.get(y, x+xoffset);
				double[] b0 = image2.get(y, x);
				
				for(int i=0;i<3;i++){
					cap0 += Math.pow(a0[i]-b0[i],2);
				}
				cap0 = Math.sqrt(cap0);
				
				//for right edge
				if(x+1< overlapWidth) {
					double[] a1 = image1.get(y, x+xoffset+1);
					double[] b1 = image2.get(y, x+1);
					
					for(int i=0;i<3;i++){
						cap1 += Math.pow(a1[i]-b1[i],2);
					}
					cap1 = Math.sqrt(cap1);
					rightCount++;
					g.setEdgeWeight(y*overlapWidth+x, y*overlapWidth+x+1, (float)(cap0+cap1));
				}
				
				//for bottom edge
				if(y+1< noGraphCut.rows()) {
					double[] a2 = image1.get(y+1, x+xoffset);
					double[] b2 = image2.get(y+1, x);
					
					for(int i=0;i<3;i++){
						cap2 += Math.pow(a2[i]-b2[i],2);
					}
					cap2 = Math.sqrt(cap2);
					bottomCount++;
					g.setEdgeWeight(y*overlapWidth+x, (y+1)*overlapWidth+x, (float)(cap0+cap2));
				}
				//System.out.println("cap0="+cap0+"cap1="+cap1+"cap2="+cap2);
			}
		}
		System.out.println("right = "+rightCount);
		System.out.println("bottom = "+bottomCount);
		
		double flow = g.computeMaximumFlow(true, null);
		System.out.println("flow is "+flow);

		image1.copyTo(noGraphCut.submat(new Rect(0,0,image1.cols(),image1.rows())));
		image2.copyTo(noGraphCut.submat(new Rect(xoffset,0,image2.cols(),image2.rows())));
		//image2.submat(new Rect(overlapWidth,0,image2.cols()-overlapWidth,image2.rows())).copyTo(noGraphCut.submat(new Rect(xoffset+overlapWidth,0,image2.cols()-overlapWidth,image2.rows())));
		//image1.submat(new Rect(0,0,xoffset,image1.rows())).copyTo(noGraphCut.submat(new Rect(0,0,xoffset,image1.rows())));
		
		
		graphCut = noGraphCut.clone();
		int unlinkedCount=0;
		int sourceCount=0;
		int sinkCount=0;
		for(int y=0; y<noGraphCut.rows(); y++) {
			for(int x=0; x<overlapWidth; x++) {
				if(g.getTerminal(y*overlapWidth+x).equals(Terminal.FOREGROUND)) {
					graphCut.put(y, xoffset+x, image1.get(y, xoffset+x));
					sourceCount++;
				} else if(g.getTerminal(y*overlapWidth+x).equals(Terminal.BACKGROUND)) { 
					graphCut.put(y, xoffset+x, image2.get(y, x));
					sinkCount++;
				} else {
					//double[] empty = {0.0,255.0,0.0};
					//graphCut.put(y, xoffset+x, empty);
					unlinkedCount++;
				}
			}
		}
		System.out.println("unlinkedCount is "+unlinkedCount);
		System.out.println("sourceCount is "+sourceCount);
		System.out.println("sinkCount is "+sinkCount);
		if(writeToFile)
			Highgui.imwrite(out, graphCut);
		ImageWindow.imshow("Output Image", graphCut);
	}
	
	public static void runEstimator(String in1, String in2, String out)
	{
		System.out.println("Starting Estimator");
		try{
			OptimalSeam os = new OptimalSeam();
			os.seamOverlayCut(in1,in2,out,true);
			//os.seamCut(in1,in2,out,true);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Completed Estimator");
	}
	
	public static void main(String[] args) {
		System.out.println("main of Optimal Seam started...");
		System.out.println("Number of Arguments = "+args.length);
		
		//Need to call this at least once or will result in exception
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		if(args.length==2)
			OptimalSeam.runEstimator(args[0],args[0],args[1]);
		if(args.length==3)
			OptimalSeam.runEstimator(args[0],args[1],args[2]);
		else
			System.out.println("Usage: 3 arguments, input1, input2 and output paths");

		System.out.println("main of Optimal Seam ended...");
	}
}
