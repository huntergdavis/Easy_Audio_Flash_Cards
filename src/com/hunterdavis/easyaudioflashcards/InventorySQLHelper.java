package com.hunterdavis.easyaudioflashcards;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;


public class InventorySQLHelper extends android.database.sqlite.SQLiteOpenHelper {
	private static final String DATABASE_NAME = "easyaudioflashcards.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "easyaudioflashcards";

	// Columns
	public static final String NAME = "name";
	public static final String URIA = "uria";
	public static final String URII = "urii";


	public InventorySQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement,"+ NAME +" text not null, "+ URIA + " text not null," + URII + " text not null);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		String sql = null;
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add note text;";
		if (oldVersion == 2)
			sql = "";

		if (sql != null)
			db.execSQL(sql);
	}

}
