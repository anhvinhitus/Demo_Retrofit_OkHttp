/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.utils.HttpClientRequest.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import vn.zing.pay.zmpsdk.utils.Log;

@SuppressWarnings("deprecation")
public class HttpClientRequest {
	private Type mType;
	private String mUrl;
	private List<NameValuePair> mParams;
	private List<NameValuePair> mHeader;
	private DefaultHttpClient mClient = null;

	public static enum Type {
		GET, POST;
	}

	public HttpClientRequest(Type type, String url) {
		mType = type;
		mUrl = url;
		mHeader = new ArrayList<NameValuePair>();
		mParams = new ArrayList<NameValuePair>();
	}

	public void addHeader(String name, String value) {
		mHeader.add(new BasicNameValuePair(name, value));
	}

	public void addParams(String name, String value) {
		mParams.add(new BasicNameValuePair(name, value));
	}

	public String getParamsString() {
		StringBuilder sb = new StringBuilder();

		if (mType == Type.GET) {
			if (mUrl.contains("?")) {
				sb.append("&platform=android");
			} else {
				sb.append("?platform=android");
			}
		} else {
			sb.append("platform=android");
		}

		for (NameValuePair item : mParams) {
			sb.append("&");
			sb.append(item.getName());
			sb.append("=");
			String encodeData = null;
			try {
				encodeData = URLEncoder.encode(item.getValue(), "UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				Log.e(this, e);
			}
			sb.append(encodeData);
		}

		return sb.toString();
	}

	/**
	 * Executes HTTP request using the default context.
	 * 
	 * Note: Apache HTTP client has fewer bugs on Eclair and Froyo. It is the
	 * best choice for these releases.
	 * 
	 * @return the response to the request. This is always a final response,
	 *         never an intermediate response with an 1xx status code. Whether
	 *         redirects or authentication challenges will be returned or
	 *         handled automatically depends on the implementation and
	 *         configuration of this client.
	 * 
	 * @throws ClientProtocolException
	 *             in case of an http protocol error
	 * 
	 * @throws IOException
	 *             in case of a problem or the connection was aborted
	 */
	private HttpResponse getResponseApache() throws ClientProtocolException, IOException {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory(), 443));

		HttpParams params = new BasicHttpParams();
		params.setParameter("http.conn-manager.max-total", Integer.valueOf(30));
		params.setParameter("http.conn-manager.max-per-route", new ConnPerRouteBean(30));
		params.setParameter("http.protocol.expect-continue", Boolean.valueOf(false));
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);

		mClient = new DefaultHttpClient(cm, params);
		HttpUriRequest request;
		HttpPost post;
		switch (mType) {
		case GET:
			StringBuilder sb = new StringBuilder(mUrl);
			sb.append(getParamsString());
			request = new HttpGet(sb.toString());
			break;
		default:
			post = new HttpPost(mUrl);
			if (!mParams.isEmpty()) {
				post.setEntity(new UrlEncodedFormEntity(mParams, "UTF-8"));
			}
			request = post;
		}

		for (NameValuePair item : mHeader) {
			request.setHeader(item.getName(), item.getValue());
		}

		HttpContext context = new org.apache.http.protocol.BasicHttpContext();
		return mClient.execute(request, context);
	}

	public InputStream getInputStream() {
		Log.d(this, "URL: " + mUrl);
		Log.d(this, "METHOD: " + mType);
		if (mParams != null) {
			Log.d(this, "PARAMS: " + mParams.toString());
		}

		/**
		 * http://android-developers.blogspot.com/2011/09/androids-http-clients.
		 * html
		 */

		/**
		 * Apache HTTP client has fewer bugs on Eclair and Froyo. It is the best
		 * choice for these releases.
		 */
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			try {
				HttpResponse resp = getResponseApache();
				HttpEntity entity = resp.getEntity();

				if (entity != null) {
					return entity.getContent();
				}
			} catch (Exception ex) {
				Log.e(this, ex);
			}
		} else {
			/**
			 * For Gingerbread and better, HttpURLConnection is the best choice.
			 * Its simple API and small size makes it great fit for Android.
			 * Transparent compression and response caching reduce network use,
			 * improve speed and save battery. New applications should use
			 * HttpURLConnection; it is where we will be spending our energy
			 * going forward.
			 */
			StringBuilder urlStr = new StringBuilder(mUrl);
			String urlParameters = getParamsString();

			if (mType == Type.GET) {
				urlStr.append(urlParameters);
			}

			try {
				URL url = new URL(urlStr.toString());
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setReadTimeout(20000);
				urlConnection.setConnectTimeout(20000);
				
				Log.d(this, "HttpURLConnection: openConnection");
				// // POST METHOD ////
				if (urlParameters != null && mType == Type.POST) {
					// urlConnection.setRequestProperty("Connection", "close");
					urlConnection.setRequestMethod("POST");
					// Send post request
					urlConnection.setDoOutput(true);

					DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
					outputStream.writeBytes(urlParameters);
					outputStream.flush();
					outputStream.close();
				}

				InputStream in = urlConnection.getInputStream();
				return in;
			} catch (Exception ex) {
				Log.e(this, "############# NETWORK ERROR - getInputStream: " + ex.getMessage() + " ############");
			}
		}
		return null;
	}

	public String getText() {
		String strResult = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));

			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			reader.close();
			strResult = result.toString();
		} catch (Exception ex) {
			Log.e(this, "############# NETWORK ERROR - getText: " + ex.getMessage() + " ############");
		}

		Log.d(this, "RESPONSE: " + strResult);

		return strResult;
	}

	public Bitmap getImage() {
		InputStream inputStream = null;
		try {
			inputStream = getInputStream();
			Bitmap image = null;
			if (inputStream != null) {
				image = BitmapFactory.decodeStream(inputStream);
			}
			return image;
		} catch (Exception ex) {
			Log.e(this, "############# NETWORK ERROR: " + ex.getMessage() + " ############");
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
			}
		}
		return null;
	}

	private static byte[] getByteArrayApache(String url) {
		try {
			URI uri = new URI(url);
			HttpGet httpget = new HttpGet(uri);

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpEntity entity = httpclient.execute(httpget).getEntity();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.writeTo(baos);

			byte[] ret = baos.toByteArray();
			baos.close();

			return ret;
		} catch (Exception ex) {
			Log.e("==== HTTP - getByteArray ====", ex);
			return null;
		} finally {
		}
	}

	private static byte[] getByteArrayAndroid(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		byte[] byteArray = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream stream = null;
		try {
			URL toDownload = new URL(url);
			byte[] chunk = new byte[1024];
			int bytesRead;

			stream = toDownload.openStream();

			while ((bytesRead = stream.read(chunk)) > 0) {
				outputStream.write(chunk, 0, bytesRead);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException ex) {
				Log.e("getByteArray", "############# NETWORK ERROR: " + ex.getMessage() + " ############");
			}
		}
		byteArray = outputStream.toByteArray();

		try {
			outputStream.close();
		} catch (IOException ex) {
			Log.e("getByteArray", "############# NETWORK ERROR: " + ex.getMessage() + " ############");
		}

		return byteArray;
	}

	public static byte[] getByteArray(String url) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return getByteArrayApache(url);
		} else {
			return getByteArrayAndroid(url);
		}
	}

	public void close() {
		// if (this.mClient != null)
		// this.mClient.
	}
}
