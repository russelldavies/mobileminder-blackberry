package com.kids.Monitor.Contacts.ContactPic;
/**
 * This class acts as a container for Contact information to be sent between methods in the ContactPic class.
 * This container consists of the contact photo, the type of image and the email address stored in the contact information.
 */
class ContactPhotoContainer{
	
	public String photoStream;	// This is the photo itself, stored in HEX form
	public String photoType;
	public String email;
	
/**
 * The constructor initialises the class variables
 */
	public ContactPhotoContainer()
	{
		photoStream = null;
		photoType = null;
		email = null;
	}
	
/**
 * These method are the setters for the class variables
 */
	
	public void setPhoto(String _photo)
	{
		photoStream = _photo;
	}
	
	public void setPhotoType(String _photoType)
	{
		photoType = _photoType;
	}
	
	public void setEmail(String _email)
	{
		email = _email;
	}
}