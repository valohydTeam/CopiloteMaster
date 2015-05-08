package com.valohyd.copilotemaster.sqlite;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.valohyd.copilotemaster.models.POI;

public class PoisBDD {

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "pois.db";

	private static final String TABLE_POIS = "table_pois";
	private static final String COL_ID = "ID";
	private static final String COL_TYPE = "Type";
	private static final String COL_LAT = "Lat";
	private static final String COL_LONG = "Long";

	private SQLiteDatabase bdd;

	private POISQLite maBaseSQLite;

	public PoisBDD(Context context) {
		// On créer la BDD et sa table
		maBaseSQLite = new POISQLite(context, NOM_BDD, null, VERSION_BDD);
	}

	public void open() {
		// on ouvre la BDD en écriture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	public void close() {
		// on ferme l'accès à la BDD
		bdd.close();
	}

	public SQLiteDatabase getBDD() {
		return bdd;
	}

	public long insertPOI(POI poi) {
		// Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		// on lui ajoute une valeur associé à une clé (qui est le nom de la
		// colonne dans laquelle on veut mettre la valeur)
		values.put(COL_TYPE, poi.getType());
		values.put(COL_LAT, poi.getLocation().latitude);
		values.put(COL_LONG, poi.getLocation().longitude);
		// on insère l'objet dans la BDD via le ContentValues
		return bdd.insert(TABLE_POIS, null, values);
	}

	public int removePOIWithLocation(LatLng location) {
		// Suppression d'un contact de la BDD grâce à l'ID
		String where = COL_LAT + "= ? AND " + COL_LONG + "= ?";
		Log.d("POI", "" + location.latitude + "," + location.longitude);
		String whereArgs[] = { "" + location.latitude, "" + location.longitude };
		int deleted = bdd.delete(TABLE_POIS, where, whereArgs);
		
		// si pas de POI supprimés : 2e technique
		if(deleted == 0){
		    ArrayList<POI> pois = getAllPOIs();
		    // get the POI to delete
		    for(POI poi : pois){
		        if(poi.getLocation().latitude == location.latitude && 
		           poi.getLocation().longitude == location.longitude){
		            // delete it
		            where = COL_ID + "= ?";
		            Log.d("POI", "seconde methode");
		            String whereArgs2[] = { "" + poi.getId() };
		            return bdd.delete(TABLE_POIS, where, whereArgs2);
		        }
		    }
		}
		return deleted;
	}

	/**
	 * On recupère tous les pois de la base
	 * 
	 * @return liste des pois
	 */
	public ArrayList<POI> getAllPOIs() {
		ArrayList<POI> pois = new ArrayList<POI>();
		Cursor cursor = bdd.rawQuery("select * from " + TABLE_POIS, null);
		if (cursor.moveToFirst()) {

			while (cursor.isAfterLast() == false) {
				POI p = new POI();
				p.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
				p.setType(cursor.getInt(cursor.getColumnIndex(COL_TYPE)));

				double latitude = cursor.getDouble(cursor
						.getColumnIndex(COL_LAT));
				double longitude = cursor.getDouble(cursor
						.getColumnIndex(COL_LONG));
				p.setLocation(new LatLng(latitude, longitude));

				pois.add(p);
				cursor.moveToNext();
			}
		}
		return pois;
	}
}