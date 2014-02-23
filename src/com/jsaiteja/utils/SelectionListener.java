/**
 * 
 */
package com.jsaiteja.utils;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * @author SaiTeja
 *
 */
public interface SelectionListener
{
	Mat getSelection(Rect selection);
}
