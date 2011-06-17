package com.kids.Data;

public class MMLinkedListIterator 
{
	private MMListNode current;

MMLinkedListIterator(MMListNode theNode) 
{
	current = theNode;//header
}

public boolean isValid() 
{
	return current != null;
}

public Object retrieve() 
{
	return isValid() ? current.getValue() : null;
}

public void advance() 
{
	if (isValid())
	{ current = current.getNextNode();}
}

public MMListNode getCurrentNode()
{
	return current;
}

}
