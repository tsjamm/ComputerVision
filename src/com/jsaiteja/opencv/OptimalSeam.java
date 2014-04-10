/**
 * 
 */
package com.jsaiteja.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.jsaiteja.utils.GraphCutBoykovKolmogorov;
import com.jsaiteja.utils.ImageWindow;
import com.jsaiteja.utils.SelectionArea;
import com.jsaiteja.utils.SelectionListener;

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
		
		Mat graphCut;
		Mat graphCutAndCutline;
		
		int overlapWidth = image1.cols()/2;
		int xoffset = image1.cols() - overlapWidth;
		
		Mat noGraphCut = new Mat(image1.rows(),image1.cols()*2 - overlapWidth, image1.type());
		
		image1.copyTo(noGraphCut.submat(new Rect(0,0,image1.cols(),image1.rows())));
		image1.copyTo(noGraphCut.submat(new Rect(xoffset,0,image1.cols(),image1.rows())));
		
		int estNodes = image1.rows()*overlapWidth;
		int estEdges = estNodes*4;
		
		GraphCutBoykovKolmogorov g = new GraphCutBoykovKolmogorov(noGraphCut.cols(), noGraphCut.rows());
		
		for(int y=0;y<image1.rows();y++) {
			g.setTWeight(0, y, 2<<32,0);//source-- leftmost column
			g.setTWeight(overlapWidth-1, y, 0, 2<<32);//sink -- rightmost column
		}
		
		for(int y=0;y<image1.rows();y++) {
			for(int x=0;x< overlapWidth; x++) {
				double[] a0 = image1.get(y, x+xoffset);
				double[] b0 = image2.get(y, x);
				double cap0 = 0;
				for(int i=0;i<3;i++){
					cap0 += Math.pow(b0[i]-a0[i],2);
				}
				cap0 = Math.sqrt(cap0);
				
				if(x+1< overlapWidth) {
					double[] a1 = image1.get(y, x+xoffset+1);
					double[] b1 = image2.get(y, x+1);
					double cap1 = 0;
					for(int i=0;i<3;i++){
						cap1 += Math.pow(b1[i]-a1[i],2);
					}
					cap1 = Math.sqrt(cap1);
					
					g.setInternWeight(x+xoffset, y, x+1+xoffset, y, (int)(cap0+cap1));
					g.setInternWeight(x+1+xoffset, y, x+xoffset, y, (int)(cap0+cap1));
				}
				
				if(y+1< image1.rows()) {
					double[] a2 = image1.get(y+1, x+xoffset);
					double[] b2 = image2.get(y+1, x);
					double cap2 = 0;
					for(int i=0;i<3;i++){
						cap2 += Math.pow(b2[i]-a2[i],2);
					}
					cap2 = Math.sqrt(cap2);
					
					g.setInternWeight(x, y, x, y+1, (int)(cap0+cap2));
					g.setInternWeight(x, y+1, x, y, (int)(cap0+cap2));
				}
			}
		}
		g.doCut();
		double flow = g.getFlow();
		System.out.println("flow is "+flow);
		
		graphCut = noGraphCut.clone();
		
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
		int xoffset = image1.cols() - overlapWidth;
		
		Mat noGraphCut = new Mat(image1.rows(),image1.cols()*2 - overlapWidth, image1.type());
		
		int estNodes = image1.rows()*overlapWidth;
		int estEdges = estNodes*4;
		System.out.println("EstNodes is "+estNodes);
		System.out.println("EstEdges is "+estEdges);
		GraphCutBoykovKolmogorov g = new GraphCutBoykovKolmogorov(overlapWidth, noGraphCut.rows());
		
		for(int y=0;y<noGraphCut.rows();y++) {
//			for(int x=0; x<overlapWidth;x++) {
//				g.setTWeight(x,y,Integer.MAX_VALUE,0);
//			}
			g.setTWeight(0, y, Integer.MAX_VALUE,0);//source-- leftmost column
			g.setTWeight(overlapWidth-1, y, 0, Integer.MAX_VALUE);//sink -- rightmost column
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
					g.setInternWeight(x, y, x+1, y, (cap0+cap1));
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
					g.setInternWeight(x, y, x, y+1, (cap0+cap2));
				}
				//System.out.println("cap0="+cap0+"cap1="+cap1+"cap2="+cap2);
			}
		}
		System.out.println("right = "+rightCount);
		System.out.println("bottom = "+bottomCount);
		g.doCut();
		double flow = g.getFlow();
		System.out.println("flow is "+flow);

		//image1.copyTo(noGraphCut.submat(new Rect(0,0,image1.cols(),image1.rows())));
		//image2.copyTo(noGraphCut.submat(new Rect(xoffset,0,image2.cols(),image2.rows())));
		image2.submat(new Rect(overlapWidth,0,image2.cols()-overlapWidth,image2.rows())).copyTo(noGraphCut.submat(new Rect(xoffset+overlapWidth,0,image2.cols()-overlapWidth,image2.rows())));
		image1.submat(new Rect(0,0,xoffset,image1.rows())).copyTo(noGraphCut.submat(new Rect(0,0,xoffset,image1.rows())));
		
		
		graphCut = noGraphCut.clone();
		int unlinkedCount=0;
		int sourceCount=0;
		int sinkCount=0;
		for(int y=0; y<noGraphCut.rows(); y++) {
			for(int x=0; x<overlapWidth; x++) {
				
				if(g.linkedTo(x, y)==0) {
					graphCut.put(y, xoffset+x, image1.get(y, xoffset+x));
					sourceCount++;
				} else if(g.linkedTo(x, y)==1) { 
					graphCut.put(y, xoffset+x, image2.get(y, x));
					sinkCount++;
				} else {
					double[] empty = {0.0,255.0,0.0};
					graphCut.put(y, xoffset+x, empty);
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
		OptimalSeam os = new OptimalSeam();
		os.seamOverlayCut(in1,in2,out,true);
		
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
