package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.models.Contact;
import com.valohyd.copilotemaster.sqlite.ContactsBDD;

/**
 * Classe representant les contacts rapides
 * 
 * @author parodi
 * 
 */
public class ContactFragment extends SherlockFragment {

	ContactsBDD bdd;

	MultiSelectionAdapter mAdapter;

	ArrayList<Contact> contacts = new ArrayList<Contact>(); // Les contacts

	private final int PICK_CONTACT = 69; // Le request code pour choisir un
											// contact

	private Button addContactButton, removeContactsButton; // Boutons actions
	private ListView list; // Liste des contacts

	private Button smsGroupButton;

	private View mainView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mainView = inflater.inflate(R.layout.contact_layout, container, false);

		// BDD
		bdd = new ContactsBDD(getActivity());

		// INITIALISATION DES CONTACTS
		initContacts();

		list = (ListView) mainView.findViewById(R.id.contactList);

		mAdapter = new MultiSelectionAdapter(getActivity(), contacts);
		list.setAdapter(mAdapter);

		// SMS Group Button
		smsGroupButton = (Button) mainView.findViewById(R.id.smsGroupButton);
		smsGroupButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ArrayList<Contact> mArraySelected = mAdapter.getCheckedItems();
				StringBuilder builder = new StringBuilder();
				for (Contact c : mArraySelected) {
					builder.append(c.getNumber().trim() + ";");
				}
				final Intent smsIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "sms:" + 
						builder.toString() ) );
				showDialogMessage(smsIntent);
				//final Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				//smsIntent.setType("vnd.android-dir/mms-sms");
				//smsIntent.putExtra("address", builder.toString());
				//showDialogMessage(smsIntent);
			}
		});

		// AJOUT D'UN CONTACT
		addContactButton = (Button) mainView
				.findViewById(R.id.addContactButton);
		addContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// On va ajouter un contact du repertoire du tel
				readcontact();
			}
		});

		// SUPPRESSION D'UN CONTACT
		removeContactsButton = (Button) mainView
				.findViewById(R.id.removeContactButton);
		removeContactsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mAdapter != null) {
					// On supprime la selection
					ArrayList<Contact> mArraySelected = mAdapter
							.getCheckedItems();

					for (Contact selected : mArraySelected) {
						removeContact(selected);
					}
					refreshAdapter();
					
					// afficher le bouton "ajouter contact"
					removeContactsButton.setVisibility(View.GONE);
					smsGroupButton.setVisibility(View.GONE);
					addContactButton.setVisibility(View.VISIBLE);					
				}
			}
		});
		return mainView;
	}

	protected void showDialogMessage(final Intent smsIntent) {
		// Dialog de choix : message rapides ou normal
		AlertDialog.Builder dialogChoix = new AlertDialog.Builder(
				getActivity());
		dialogChoix.setIcon(R.drawable.ic_launcher);
		dialogChoix.setTitle(R.string.choice_messages_dialog_title);
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.select_dialog_item);
		arrayAdapter.add("Normal");
		arrayAdapter.add("Messages rapide");
		dialogChoix.setNegativeButton(R.string.close,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				});

		dialogChoix.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						//Si on a choisi normal on lance l'app de message directement
						if (which == 0) {
							
							getActivity().startActivity(smsIntent);
						}
						//Sinon on montre les messages rapide
						else {
							AlertDialog.Builder dialogMessages = new AlertDialog.Builder(
									getActivity());
							String[] quick_messages_array = getResources().getStringArray(R.array.quick_messages);
							dialogMessages
									.setIcon(R.drawable.ic_launcher);
							dialogMessages
									.setTitle(R.string.quick_messages_dialog_title);
							final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
									getActivity(),
									android.R.layout.select_dialog_item,quick_messages_array);
							
							dialogMessages
									.setNegativeButton(
											R.string.close,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
												}
											});

							dialogMessages
									.setAdapter(
											arrayAdapter,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													String message = arrayAdapter
															.getItem(which);
													smsIntent.putExtra(
															"sms_body",
															message);
													getActivity()
															.startActivity(
																	smsIntent);
												}
											});
							dialogMessages.show();
						}
					}
				});
		dialogChoix.show();
	}

	/**
	 * permet de dire de redessiner le menu
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		getActivity().supportInvalidateOptionsMenu();
	}

	/**
	 * Initialise la liste des contacts avec les preferences
	 */
	private void initContacts() {
		bdd.open();
		contacts = bdd.getAllContacts();
		bdd.close();
		if (contacts.isEmpty())
			mainView.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
	}

	/**
	 * Supprime un contact des preferences
	 * 
	 * @param contact
	 */
	private void removeContact(Contact contact) {
		if (contacts.contains(contact)) {
			bdd.open();
			int res = bdd.removeContactWithPhone(contact.getNumber());
			if (res == 1)
				contacts.remove(contact);
			else {
				Toast.makeText(getActivity(),
						R.string.erreur_suppression_contact, Toast.LENGTH_SHORT)
						.show();
			}
			bdd.close();
			if (contacts.isEmpty())
				mainView.findViewById(R.id.no_contacts).setVisibility(
						View.VISIBLE);
		}
	}

	private boolean isContactExits(String name) {
		for (Contact c : contacts) {
			if (c.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Ajoute un contact aux preferences
	 * 
	 * @param name
	 * @param number
	 */
	private void addContact(String name, String number) {
		Contact c = new Contact(name, number);

		if (isContactExits(name)) {
			Toast.makeText(getActivity(), R.string.contact_existant,
					Toast.LENGTH_SHORT).show();
		} else {
			Log.d("ADD", c.toString());
			bdd.open();
			bdd.insertContact(c);
			contacts.add(c);
			bdd.close();
		}
		mainView.findViewById(R.id.no_contacts).setVisibility(View.GONE);
	}

	/**
	 * Actualise l'adapter
	 */
	private void refreshAdapter() {
		mAdapter.setList(contacts);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Lecture du répertoire du tel
	 */
	public void readcontact() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);

	}

	/**
	 * En attente d'un resultat (ici attente du choix d'un contact depuis le
	 * repertoire)
	 */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		// Si l'utilisateur vient du choix d'un contact
		case (PICK_CONTACT):
			// Si tout s'est bien déroulé
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				String[] projection = new String[] {
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER };

				Cursor people = getActivity().getContentResolver().query(
						contactData, projection, null, null, null);

				int indexName = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
				int indexNumber = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

				people.moveToFirst();
				do {
					String name = people.getString(indexName);
					String number = people.getString(indexNumber);
					addContact(name, number); // On l'ajoute
					refreshAdapter(); // On refresh
				} while (people.moveToNext());
			}
			break;
		}
	}

	public class MultiSelectionAdapter extends BaseAdapter {

		Context mContext;

		LayoutInflater mInflater;

		ArrayList<Contact> mList;

		SparseBooleanArray mSparseBooleanArray;

		public MultiSelectionAdapter(Context context, ArrayList<Contact> list) {
			this.mContext = context;

			mInflater = LayoutInflater.from(mContext);

			mSparseBooleanArray = new SparseBooleanArray();

			mList = new ArrayList<Contact>();

			this.mList = new ArrayList<Contact>(list);

		}

		public void setList(ArrayList<Contact> list) {
			this.mList = new ArrayList<Contact>(list);
			this.mSparseBooleanArray = new SparseBooleanArray();
		}

		public ArrayList<Contact> getCheckedItems() {

			ArrayList<Contact> mTempArry = new ArrayList<Contact>();

			for (int i = 0; i < mList.size(); i++) {

				if (mSparseBooleanArray.get(i)) {

					mTempArry.add(mList.get(i));

				}

			}

			return mTempArry;

		}

		@Override
		public int getCount() {
			return mList.size();

		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);

		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.contact_row, null);

			}

			TextView name = (TextView) convertView
					.findViewById(R.id.contactName);

			name.setText(mList.get(position).getName());

			CheckBox mCheckBox = (CheckBox) convertView
					.findViewById(R.id.chkEnable);

			mCheckBox.setTag(position);

			mCheckBox.setChecked(mSparseBooleanArray.get(position));

			mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

			ImageButton mCallButton = (ImageButton) convertView
					.findViewById(R.id.callContactButton);
			mCallButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String uri = "tel:"
							+ mList.get(position).getNumber().trim();
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(Uri.parse(uri));
					mContext.startActivity(intent);
				}
			});

			ImageButton mSmsButton = (ImageButton) convertView
					.findViewById(R.id.smsContactButton);
			mSmsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent smsIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "sms:" + 
													mList.get(position).getNumber().trim() ) );
					showDialogMessage(smsIntent);
					//context.startActivity( intent );

					//Intent smsIntent = new Intent(Intent.ACTION_VIEW);
					//smsIntent.setType("vnd.android-dir/mms-sms");
					//smsIntent.putExtra("address", mList.get(position)
					//		.getNumber().trim());
					//showDialogMessage(smsIntent);
					// smsIntent.putExtra("sms_body","Body of Message");
					//mContext.startActivity(smsIntent);
				}
			});

			return convertView;

		}

		OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mSparseBooleanArray.put((Integer) buttonView.getTag(),
						isChecked);
				if (getCheckedItems().size() >= 2) {
					smsGroupButton.setVisibility(View.VISIBLE);
					addContactButton.setVisibility(View.GONE);
				} else if (getCheckedItems().size() == 1) {
					removeContactsButton.setVisibility(View.VISIBLE);
					smsGroupButton.setVisibility(View.GONE);
					addContactButton.setVisibility(View.GONE);
				} else {
					removeContactsButton.setVisibility(View.GONE);
					smsGroupButton.setVisibility(View.GONE);
					addContactButton.setVisibility(View.VISIBLE);
				}

			}

		};

	}
}
