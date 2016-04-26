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

import vn.zing.pay.zmpsdk.entity.DTransaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author YenNLH
 * 
 */
public class TransactionDataSource {
	// Database fields
	private SQLiteDatabase database;
	private TransactionSQLiteHelper dbHelper;

	public TransactionDataSource(Context context) {
		dbHelper = new TransactionSQLiteHelper(context);
	}

	private void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	private void close() {
		dbHelper.close();
	}

	public boolean addTransaction(DTransaction transaction) {
		DTransaction object = getTransaction(transaction.appTransID);
		try {
			open();
			if (object == null) {
				ContentValues values = new ContentValues();
				
				values.put(TransactionSQLiteHelper.COLUMN_AMOUNT, transaction.amount);
				values.put(TransactionSQLiteHelper.COLUMN_APP_TRANS_ID, transaction.appTransID);
				values.put(TransactionSQLiteHelper.COLUMN_RETRY_NUMBER, transaction.retryCount);
				values.put(TransactionSQLiteHelper.COLUMN_UDID, transaction.UDID);
				values.put(TransactionSQLiteHelper.COLUMN_SDK_TRANS_ID, transaction.sdkTransID);
				values.put(TransactionSQLiteHelper.COLUMN_STATUS, transaction.status.getNum());
				values.put(TransactionSQLiteHelper.COLUMN_TIME, transaction.time);
				
				long id = database.insert(TransactionSQLiteHelper.TABLE_NAME, null, values);
				
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

	public void updateTransaction(DTransaction pTransaction) {
		try {
			open();
			String strFilter = TransactionSQLiteHelper.COLUMN_SDK_TRANS_ID + "='" + pTransaction.sdkTransID + "'";
			ContentValues values = new ContentValues();
			values.put(TransactionSQLiteHelper.COLUMN_RETRY_NUMBER, pTransaction.retryCount);
			values.put(TransactionSQLiteHelper.COLUMN_STATUS, pTransaction.status.getNum());
			database.update(TransactionSQLiteHelper.TABLE_NAME, values, strFilter, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	public void deleteAllTransaction() {
		try {
			open();
			database.delete(TransactionSQLiteHelper.TABLE_NAME, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}

	public void deleteTransaction(String pSdkTransID) {
		try {
			open();
			database.delete(TransactionSQLiteHelper.TABLE_NAME, TransactionSQLiteHelper.COLUMN_SDK_TRANS_ID + "=?",
					new String[] { pSdkTransID });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}

	public DTransaction getTransaction(String sdkTransID) {
		Cursor cursor = null;
		try {
			open();
			cursor = database.query(TransactionSQLiteHelper.TABLE_NAME, null,
					TransactionSQLiteHelper.COLUMN_SDK_TRANS_ID + "=?", new String[] { sdkTransID }, null, null, null);

			DTransaction object = null;
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					object = new DTransaction(cursor);
				}
				return object;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		} finally {
			// make sure to close the cursor
			if (cursor != null && !cursor.isClosed())
				cursor.close();
			close();
		}

	}

	public List<DTransaction> getTransactionList() {

		Cursor cursor = null;
		List<DTransaction> transactions = new ArrayList<DTransaction>();
		try {
			open();
			cursor = database.query(TransactionSQLiteHelper.TABLE_NAME, null, null, null, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					DTransaction transaction = new DTransaction(cursor);
					transactions.add(transaction);
					cursor.moveToNext();
				}
			}
			return transactions;

		} catch (Exception e) {
			return transactions;
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
			close();
		}

	}

}