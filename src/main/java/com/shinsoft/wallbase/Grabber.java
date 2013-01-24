/** 
 * ShinSoft (c), 2013
 * Arnaud KEIFLIN (arnaud@keiflin.fr), 24 janv. 2013
 *
 * Project : wallbase-grabber
 * Package location: com.shinsoft.wallbase
 * File name: Grabber.java
 *
 * Comments :
 **/


package com.shinsoft.wallbase;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.shinsoft.wallbase.model.SubscriptionRow;
import com.shinsoft.wallbase.shared.Constants;

public class Grabber
{
	private HttpClient client;

	public Grabber()
	{
		this.client = new DefaultHttpClient();
	}

	public void proceed() throws IOException
	{
		// Init Properties
		Properties properties = initProperties();

		// Login Wallbase
		login(properties);

		// Goto Subscriptions page
		List<SubscriptionRow> subscriptions = getSubscriptions();
		if (subscriptions.isEmpty())
		{
			System.out.println("No new wallpapers for subscriptions ...");	
		}
		else
		{
			System.out.println("New wallpapers for " + subscriptions.size() + " subscriptions found ...");
			
			// Run Wallbase Downloader
			launchWallbaseDownloader(properties);
	
			// Copy Paste it into clipboard
			for (SubscriptionRow currentRow : subscriptions)
			{
				System.out.println(currentRow.getName()+" have "+currentRow.getNbNew()+" new wallpapers.");
				copyToClipboard(currentRow.getUrl());
			}
			
			System.out.println("Processing done !");
		}

	}

	private void launchWallbaseDownloader(Properties properties) throws IOException
	{
		Runtime.getRuntime().exec(properties.getProperty(Constants.CFG_WALLBASE_APP_PATH));
	}

	private List<SubscriptionRow> getSubscriptions() throws ClientProtocolException, IOException
	{
		List<SubscriptionRow> ret = new ArrayList<SubscriptionRow>();

		HttpPost post = new HttpPost(Constants.WALLBASE_URL_SUBSCRIPTIONS);
		HttpResponse response = client.execute(post);
		String htmlSource = EntityUtils.toString(response.getEntity());
		post.abort();
		Document source = Jsoup.parse(htmlSource);

		Elements elementsByClass = source.getElementsByClass(Constants.HTML_ELEMENT_CLASS_SUB_CATLIST_ROW);
		ListIterator<Element> listIterator = elementsByClass.listIterator();

		while(listIterator.hasNext())
		{
			Element currentElement = listIterator.next();
			SubscriptionRow row = buildSubscriptionRow(currentElement);
			if (row != null)
			{
				ret.add(row);
			}
		}
		return ret;
	}


	private SubscriptionRow buildSubscriptionRow(
					Element currentElement)
					throws IOException,
					ClientProtocolException
	{
		HttpPost post;
		HttpResponse response;
		
		Elements newNumElement = currentElement.getElementsByClass(Constants.HTML_ELEMENT_CLASS_NEWNUM);
		if (newNumElement != null && !newNumElement.isEmpty())
		{
			String newNum = CharMatcher.DIGIT.retainFrom(newNumElement.text());

			Element hrefElement = currentElement.getElementsByTag(Constants.HTML_ELEMENT_TAG_A).get(0);
			String name = hrefElement.text();
			
			// Load the href URL to retrieve the correct URL for Wallbase Downloader
			String url = null;
			post = new HttpPost(hrefElement.attr(Constants.HTML_ELEMENT_ATTR_HREF));
			response = client.execute(post);
			String htmlSubscriptionSource = EntityUtils.toString(response.getEntity());
			post.abort();
			Document sourceSubscriptions = Jsoup.parse(htmlSubscriptionSource);
			Elements urlElements = sourceSubscriptions.getElementById(Constants.HTML_ELEMENT_ID_SUBSCR_LIST).getElementsByClass(Constants.HTML_ELEMENT_CLASS_HEADER).get(0).getElementsByTag(Constants.HTML_ELEMENT_TAG_A);
			ListIterator<Element> urlElementsIterator = urlElements.listIterator();
			while (urlElementsIterator.hasNext())
			{
				Element currentElementURL = urlElementsIterator.next();
				if (currentElementURL.attr(Constants.HTML_ELEMENT_ATTR_HREF).startsWith("tags/info/"))
				{
					url = new StringBuilder("http://wallbase.cc/").append(currentElementURL.attr(Constants.HTML_ELEMENT_ATTR_HREF)).toString();
					break;
				}
			}

			return new SubscriptionRow(name,url,Integer.valueOf(newNum));
		}
		else
		{
			return null;
		}
	}

	private void login(Properties prop) throws ClientProtocolException, IOException
	{
		HttpPost post = new HttpPost(Constants.WALLBASE_URL_LOGIN);

		List<NameValuePair> lstParam = new ArrayList<NameValuePair>();

		lstParam.add(new BasicNameValuePair("usrname",prop.getProperty(Constants.CFG_LOGIN)));
		lstParam.add(new BasicNameValuePair("pass",prop.getProperty(Constants.CFG_PASSWD)));
		lstParam.add(new BasicNameValuePair("nopass_email",""));
		lstParam.add(new BasicNameValuePair("nopass","0"));
		post.setEntity(new UrlEncodedFormEntity(lstParam));

		client.execute(post);
		post.abort();
	}


	private Properties initProperties() throws IOException
	{
		Properties prop = new Properties();
		prop.load(getClass().getClassLoader().getResourceAsStream(Constants.CONFIG_PROPERTIES));
		return prop;
	}


	private void copyToClipboard(String url)
	{
		StringSelection stringSelection = new StringSelection(url);
		Clipboard clipboard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
		clipboard.setContents (stringSelection, null);
	}
}



