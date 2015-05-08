package com.valohyd.copilotemaster.models;

/**
 * Represente un contact
 * 
 * @author LPARODI
 * 
 */
public class Contact {

	private int id;
	private String name;
	private String phoneNumber;

	public Contact() {
	}

	public Contact(String name, String phoneNumber) {
		this.name = name;
		this.phoneNumber = phoneNumber;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String isbn) {
		this.name = isbn;
	}

	public String getNumber() {
		return phoneNumber;
	}

	public void setNumber(String titre) {
		this.phoneNumber = titre;
	}

	public String toString() {
		return "ID : " + id + "\nNom : " + name + "\nTel : " + phoneNumber;
	}
}