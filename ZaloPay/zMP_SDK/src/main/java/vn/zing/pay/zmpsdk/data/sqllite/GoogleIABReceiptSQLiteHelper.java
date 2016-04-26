/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.sqllite.TransactionSQLiteHelper.java
 * Created date: Dec 24, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data.sqllite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author YenNLH
 * 
 */
public class GoogleIABReceiptSQLiteHelper extends SQLiteOpenHelper {
	// @formatter:off
	public static final String TABLE_NAME   		= "zingpay_iab_receipts";
	public static final String COLUMN_ID 		   	= "_id";
	public static final String COLUMN_SDK_TRANS_ID  = "zpsdkTransID";
	public static final String COLUMN_APP_ID  		= "appID";
	public static final String COLUMN_SIGNATURE	   	= "signature";
	public static final String COLUMN_RECEIPT 	   	= "receipt";
	public static final String COLUMN_PAYLOAD 	   	= "payload";
	public static final String COLUMN_RETRY_NUMBER 	= "retryCount";
	public static final String COLUMN_TIME 	   	   	= "timestamp";
	
	private static final String DATABASE_NAME 		= "zingpay_transactions.db";
	private static final int DATABASE_VERSION 		= 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_NAME + "(" 
			+ COLUMN_ID 			+ " integer primary key autoincrement, " 
			+ COLUMN_SDK_TRANS_ID	+ " text not null, " 
			+ COLUMN_APP_ID			+ " text not null, " 
			+ COLUMN_SIGNATURE		+ " text not null, "
			+ COLUMN_RECEIPT		+ " text not null, "
			+ COLUMN_PAYLOAD		+ " text not null, "
			+ COLUMN_RETRY_NUMBER	+ " integer not null, "
			+ COLUMN_TIME			+ " integer not null);";
	// @formatter:on

	public GoogleIABReceiptSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
