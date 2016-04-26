/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.utils.StorageUtil.java
 * Created date: Dec 19, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Environment;
import android.util.Base64;

/**
 * @author YenNLH
 * 
 */
public class StorageUtil {

	/**
	 * 
	 * @return
	 */
	public static boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static void decompress(byte[] compressed, String location) throws IOException {

		InputStream is;
		ZipInputStream zis;

		String filename;
		is = new ByteArrayInputStream(compressed);
		zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;
		byte[] buffer = new byte[1024];
		int count;

		while ((ze = zis.getNextEntry()) != null) {
			filename = ze.getName();

			// Need to create directories if not exists, or
			// it will generate an Exception...
			if (ze.isDirectory()) {
				String path = location + File.separator + filename;
				File fmd = new File(path);
				fmd.mkdirs();
				hideImageFromGallery(path + File.separator);
				continue;
			}
			FileOutputStream fout = new FileOutputStream(location + File.separator + filename);

			while ((count = zis.read(buffer)) != -1) {
				fout.write(buffer, 0, count);
			}

			fout.close();
			zis.closeEntry();
		}

		zis.close();

	}

	public static void decompress(String zipText, String location) throws IOException {
		byte[] compressed = Base64.decode(zipText, Base64.DEFAULT);
		InputStream is;
		ZipInputStream zis;

		String filename;
		is = new ByteArrayInputStream(compressed);
		zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;
		byte[] buffer = new byte[1024];
		int count;

		while ((ze = zis.getNextEntry()) != null) {
			filename = ze.getName();
			// Need to create directories if not exists, or
			// it will generate an Exception...
			if (ze.isDirectory()) {
				String path = location + File.separator + filename;
				File fmd = new File(path);
				fmd.mkdirs();
				hideImageFromGallery(path + File.separator);
				continue;
			}
			FileOutputStream fout = new FileOutputStream(location + File.separator + filename);

			while ((count = zis.read(buffer)) != -1) {
				fout.write(buffer, 0, count);
			}

			fout.close();
			zis.closeEntry();
		}
		zis.close();
	}

	/**
	 * Create .nomedia file in order to prevent gallery application shows this
	 * folder into album
	 * 
	 * @param path
	 *            Local path
	 * 
	 * @throws IOException
	 *             if it's not possible to create the file.
	 */
	public static void hideImageFromGallery(String path) throws IOException {
		String NOMEDIA = ".nomedia";
		File nomediaFile = new File(path + NOMEDIA);
		if (!nomediaFile.exists()) {
			nomediaFile.createNewFile();
		}
	}

	public static void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}

		fileOrDirectory.delete();
	}
}
