/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.sqllite.TransactionDataSource.java
 * Created date: Dec 24, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data.sqllite;

import java.util.ArrayList;
import java.util.List;

import vn.zing.pay.zmpsdk.entity.google.DGoogleIabReceipt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author YenNLH
 *
 */
public class GoogleIABReceiptDataSource {
	// Database fields
	private SQLiteDatabase database;
	private GoogleIABReceiptSQLiteHelper dbHelper;

	public GoogleIABReceiptDataSource(Context context) {
		dbHelper = new GoogleIABReceiptSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public boolean insertReceipt(DGoogleIabReceipt pIabReceipt) {
		DGoogleIabReceipt object = getReceipt(pIabReceipt.zmpTransID);
		try {
			open();
			if (object == null) {
				ContentValues values = new ContentValues();
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_SDK_TRANS_ID, pIabReceipt.zmpTransID);
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_APP_ID, pIabReceipt.appID);
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_SIGNATURE, pIabReceipt.signature);
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_RECEIPT, pIabReceipt.receipt);
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_PAYLOAD, pIabReceipt.payload);
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_RETRY_NUMBER, pIabReceipt.retryCount);
				values.put(GoogleIABReceiptSQLiteHelper.COLUMN_TIME, pIabReceipt.time);
				long id = database.insert(GoogleIABReceiptSQLiteHelper.TABLE_NAME, null, values);
				if (id != -1)
					return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			close();
		}
	}

	public void updateTransaction(DGoogleIabReceipt pIabReceipt) {
		try {
			open();
			String strFilter = GoogleIABReceiptSQLiteHelper.COLUMN_SDK_TRANS_ID + "='"	+ pIabReceipt.zmpTransID + "'";
			ContentValues values = new ContentValues();
			values.put(GoogleIABReceiptSQLiteHelper.COLUMN_RETRY_NUMBER,	pIabReceipt.retryCount);
			database.update(GoogleIABReceiptSQLiteHelper.TABLE_NAME, values, strFilter, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	public void deleteAll() {
		try {
			open();
			database.delete(GoogleIABReceiptSQLiteHelper.TABLE_NAME, null, null);	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
	}

	public void deleteReceipt(String pSdkTransID) {
		try {
			open();
			database.delete(GoogleIABReceiptSQLiteHelper.TABLE_NAME,
					GoogleIABReceiptSQLiteHelper.COLUMN_SDK_TRANS_ID + "=?",
					new String[] { pSdkTransID });		
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
		
	}

	public DGoogleIabReceipt getReceipt(String pZmpTransID) {
		Cursor cursor = null;
		try {
			open();
			cursor = database.query(
					GoogleIABReceiptSQLiteHelper.TABLE_NAME, null,
					GoogleIABReceiptSQLiteHelper.COLUMN_SDK_TRANS_ID + "=?",
					new String[] { pZmpTransID }, null, null, null);

			DGoogleIabReceipt object = null;
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					object = new DGoogleIabReceipt(cursor);
				}
				return object;
			} else {
				return null;
			}
		}catch (Exception e) {
			return null;
		}
		finally {
			// make sure to close the cursor
			if (cursor != null && !cursor.isClosed()) cursor.close();
			close();
		}
		
	}

	public List<DGoogleIabReceipt> getAll() {
		
		Cursor cursor = null;
		List<DGoogleIabReceipt> transactions = new ArrayList<DGoogleIabReceipt>();
		try {	
			open();
			cursor = database.query(GoogleIABReceiptSQLiteHelper.TABLE_NAME, null, null, null,
					null, null, null);

			if (cursor!= null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					DGoogleIabReceipt transaction = new DGoogleIabReceipt(cursor);
					transactions.add(transaction);
					cursor.moveToNext();
				}
			}
			return transactions;	
			
		}catch (Exception e) {
			return transactions;
		}finally{
			if (cursor != null && !cursor.isClosed()) cursor.close();
			close();
		}
	
	}

}