/** 
 * ShinSoft (c), 2013
 * Arnaud KEIFLIN (arnaud@keiflin.fr), 24 janv. 2013
 *
 * Project : wallbase-grabber
 * Package location: com.shinsoft.wallbase.model
 * File name: SubscriptionRow.java
 *
 * Comments :
 **/


package com.shinsoft.wallbase.model;

public class SubscriptionRow
{
	private String url;
	private Integer nbNew;
	private String name;

	public SubscriptionRow(String name, String url, Integer nbNew)
	{
		this.name = name;
		this.url = url;
		this.nbNew = nbNew;
	}
	
	public String getUrl()
	{
		return url;
	}

	public Integer getNbNew()
	{
		return nbNew;
	}
	
	public String getName()
	{
		return name;
	}
}



