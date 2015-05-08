package com.valohyd.copilotemaster.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class POISQLite extends SQLiteOpenHelper {

	private static final String TABLE_POIS = "table_pois";
	private static final String COL_ID = "ID";
	private static final String COL_TYPE = "Type";
	private static final String COL_LAT = "Lat";
	private static final String COL_LONG = "Long";

	private static final String CREATE_BDD = "CREATE TABLE " + TABLE_POIS
			+ " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TYPE + " INTEGER NOT NULL, "+ COL_LAT + " DOUBLE PRECISION NOT NULL, "+ COL_LONG + " DOUBLE PRECISION NOT NULL);";

	public POISQLite(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// on créé la table à partir de la requête écrite dans la variable
		// CREATE_BDD
		db.execSQL(CREATE_BDD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// On peut fait ce qu'on veut ici moi j'ai décidé de supprimer la table
		// et de la recréer
		// comme ça lorsque je change la version les id repartent de 0
		db.execSQL("DROP TABLE " + TABLE_POIS + ";");
		onCreate(db);
	}

}