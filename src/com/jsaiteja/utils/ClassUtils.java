/**
 * 
 */
package com.jsaiteja.utils;


/**
 * @author SaiTeja
 *
 */
public class ClassUtils {
	
	public static String getCurrentClassUrl(Class<?> c)
	{
		return c.getResource("").getPath().split("/",2)[1];
	}
	
	public static String getCurrentProjectUrl(Class<?> c)
	{
		return c.getClassLoader().getResource("").getPath().split("/",2)[1];
	}
	
	public static String getCurrentProjectResourcesUrl(Class<?> c)
	{
		return getCurrentProjectUrl(c)+"resources/";
	}
}
