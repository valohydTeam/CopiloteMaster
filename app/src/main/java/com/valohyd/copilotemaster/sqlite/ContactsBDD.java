package com.valohyd.copilotemaster.sqlite;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.valohyd.copilotemaster.models.Contact;

public class ContactsBDD {

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "contacts.db";

	private static final String TABLE_CONTACTS = "table_contacts";
	private static final String COL_ID = "ID";
	private static final int NUM_COL_ID = 0;
	private static final String COL_NAME = "Name";
	private static final int NUM_COL_NAME = 1;
	private static final String COL_PHONE = "Phone";
	private static final int NUM_COL_PHONE = 2;

	private SQLiteDatabase bdd;

	private ContactsSQLite maBaseSQLite;

	public ContactsBDD(Context context) {
		// On créer la BDD et sa table
		maBaseSQLite = new ContactsSQLite(context, NOM_BDD, null, VERSION_BDD);
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

	public long insertContact(Contact contact) {
		// Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		// on lui ajoute une valeur associé à une clé (qui est le nom de la
		// colonne dans laquelle on veut mettre la valeur)
		values.put(COL_NAME, contact.getName());
		values.put(COL_PHONE, contact.getNumber());
		// on insère l'objet dans la BDD via le ContentValues
		return bdd.insert(TABLE_CONTACTS, null, values);
	}

	public int updateContact(int id, Contact contact) {
		// La mise à jour d'un contact dans la BDD fonctionne plus ou moins
		// comme
		// une insertion
		// il faut simple préciser quel contact on doit mettre à jour grâce à
		// l'ID
		ContentValues values = new ContentValues();
		values.put(COL_NAME, contact.getName());
		values.put(COL_PHONE, contact.getNumber());
		return bdd.update(TABLE_CONTACTS, values, COL_ID + " = " + id, null);
	}

	public int removeContactWithID(int id) {
		// Suppression d'un contact de la BDD grâce à l'ID
		String where = COL_ID + "= " + "?";
		String whereArgs[] = { "" + id };
		return bdd.delete(TABLE_CONTACTS, where, whereArgs);
	}

	public int removeContactWithPhone(String phone) {
		// Suppression d'un contact de la BDD grâce à l'ID
		String where = COL_PHONE + "= " + "?";
		String whereArgs[] = { phone };
		return bdd.delete(TABLE_CONTACTS, where, whereArgs);
	}

	public Contact getContactWithName(String titre) {
		// Récupère dans un Cursor les valeur correspondant à un contact contenu
		// dans la BDD (ici on sélectionne le contact grâce à son nom)
		Cursor c = bdd.query(TABLE_CONTACTS, new String[] { COL_ID, COL_NAME,
				COL_PHONE }, COL_PHONE + " LIKE \"" + titre + "\"", null, null,
				null, null);
		return cursorToContact(c);
	}

	/**
	 * On recupère tous les contacts de la base
	 * 
	 * @return liste des contacts
	 */
	public ArrayList<Contact> getAllContacts() {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Cursor cursor = bdd.rawQuery("select * from " + TABLE_CONTACTS, null);
		if (cursor.moveToFirst()) {

			while (cursor.isAfterLast() == false) {
				Contact c = new Contact();
				c.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
				c.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
				c.setNumber(cursor.getString(cursor.getColumnIndex(COL_PHONE)));

				contacts.add(c);
				cursor.moveToNext();
			}
		}
		return contacts;
	}

	// Cette méthode permet de convertir un cursor en un contact
	private Contact cursorToContact(Cursor c) {
		// si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return null;

		// Sinon on se place sur le premier élément
		c.moveToFirst();
		// On créé un contact
		Contact contact = new Contact();
		// on lui affecte toutes les infos grâce aux infos contenues dans le
		// Cursor
		contact.setId(c.getInt(NUM_COL_ID));
		contact.setName(c.getString(NUM_COL_NAME));
		contact.setNumber(c.getString(NUM_COL_PHONE));
		// On ferme le cursor
		c.close();

		// On retourne le contact
		return contact;
	}
}