package com.mmtechco.mobileminder.contacts;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.blackberry.api.pdap.BlackBerryContact;

public class ContactsReader {
	private static final String TAG = ToolsBB.getSimpleClassName(ContactsReader.class);
	
	Logger logger = Logger.getInstance();

	public ContactsReader() throws PIMException {
		// Create list of contact
		ContactList blackBerryContactList = (ContactList) PIM.getInstance()
				.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
		// Retrieve contact list items, which will need to be converted into an
		// array/vector
		Enumeration allContacts = blackBerryContactList.items();
		// Vector to store contacts, once converted from enum
		Vector blackBerryContacts = enumToVector(allContacts);
		// Retrive specific contact (to test)
		// blackBerryContact =
		// (BlackBerryContact)blackBerryContacts.elementAt(listField.getSelectedIndex());
		BlackBerryContact bbContact = (BlackBerryContact) blackBerryContacts
				.elementAt(1);
		// and output
		// displayContact(bbContact);
		// ContactDetailsScreen contactDetailsScreen = new
		// ContactDetailsScreen(bbContact);

		// Get an array of all populated contact fields.
		int fieldsWithData[] = bbContact.getFields();
		logger.log(TAG, String.valueOf(fieldsWithData.length));
	}

	// Convert the list of contacts from an Enumeration to a Vector
	private Vector enumToVector(Enumeration contactEnum) {
		Vector v = new Vector();
		if (contactEnum == null) {
			return v;
		}
		while (contactEnum.hasMoreElements()) {
			v.addElement(contactEnum.nextElement());
		}
		return v;
	}
}