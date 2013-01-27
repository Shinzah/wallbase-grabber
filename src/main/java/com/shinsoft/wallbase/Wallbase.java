/** 
 * ShinSoft (c), 2013
 * Arnaud KEIFLIN (arnaud@keiflin.fr), 24 janv. 2013
 *
 * Project : wallbase-grabber
 * Package location: com.shinsoft.wallbase
 * File name: Wallbase.java
 *
 * Comments :
 **/


package com.shinsoft.wallbase;

import java.io.IOException;

public class Wallbase
{
	public static void main(String[ ] args)
	{
		try
		{
			new Grabber().proceed();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}



