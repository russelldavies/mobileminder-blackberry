package com.mmtechco.mobileminder.util;


/**
 * This class creates dynamic randomly accessed object arrays.
 */
public class MMLinkedList {
	private int size;
	private MMListNode header;// start of the linkedlist

	/**
	 * This constructor initialises the class variables
	 */
	public MMLinkedList() {
		makeEmpty();
	}

	/**
	 * This method determines if the object array has no elements
	 * 
	 * @return true if the array has no elements
	 */
	public boolean isEmpty() {
		return header == null;
	}

	/**
	 * This method clears the elements in the array by initialising the object
	 * variables
	 */
	public void makeEmpty() {
		header = null;
		size = 0;
	}

	/**
	 * This method creates an iterator for the calling MMLinkedList object
	 * 
	 * @return iterator
	 */
	public MMLinkedListIterator getIterator() {
		return new MMLinkedListIterator(header);
	}

	/**
	 * This method adds an object to the array
	 * 
	 * @param inputObj
	 *            the object to to be added
	 */
	public void add(Object inputObj) {
		if (null == header) {
			MMListNode tempHead = new MMListNode(null, null);
			header = new MMListNode(tempHead, inputObj);
			tempHead = header;
		} else {
			header.addValue(inputObj);
		}
		size++;
	}

	/**
	 * This method returns the object at the first position in the array
	 * 
	 * @return object at the first position
	 */
	public Object getValueAtFirst() {
		return getValueAt(0);
	}

	/**
	 * This method returns the object at the specified position in the array
	 * 
	 * @param index
	 *            specified position
	 * @return object at the specified position
	 */
	public Object getValueAt(int index) {
		if (null == header) {
			return null;
		} else {
			return header.getValue(index);
		}
	}

	/**
	 * This method removes the object at the first position in the array
	 * 
	 * @return true if value was removed
	 */
	public boolean removeFirst() {
		return remove(0);
	}

	/**
	 * This method removes the object at the specified position in the array
	 * 
	 * @param index
	 *            specified position
	 * @return true if value was removed
	 */
	public boolean remove(int index) {
		return header.remove(index);
	}

	/**
	 * 
	 * @param inputObj
	 * @return
	 */
	public boolean remove(Object inputObj) {
		if (header.remove(inputObj)) {
			size--;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param inputObj
	 * @return
	 */
	public boolean contains(Object inputObj) {
		if (header.contains(inputObj)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean toArray(Object[] rtnArray) {
		// Object[] rtnArray = new Object[size];
		if (rtnArray.length >= size) {
			int count = 0;
			for (MMLinkedListIterator theItr = this.getIterator(); theItr
					.isValid() && rtnArray.length > count; theItr.advance()) {
				rtnArray[count++] = theItr.retrieve();
			}
			return true;
		} else {
			return false;
		}
		// return rtnArray;
	}

	public int size() {
		return size;
	}

}

class MMListNode {
	private static final String TAG = "MMListNode";
	private Object value;
	private MMListNode next;
	private MMListNode previous;
	private Logger logger = Logger.getInstance();

	public MMListNode(MMListNode inputPreviousLink, Object theElement) {
		value = theElement;
		previous = inputPreviousLink;
	}

	public boolean remove(int indexCount) {
		if (0 == indexCount) {
			removeThisLink();
			return true;
		}
		if (0 < indexCount) {
			return false;
		} else if (null != next) {
			return next.remove(--indexCount);
		} else {
			return false;
		}
	}

	public boolean contains(Object inputObj) {
		logger.log(TAG, "input value in linkedlist: " + (String) inputObj);

		/*
		 * if(value == inputObj) { return true; } else if(next != null) { return
		 * next.contains(inputObj); } else { return false;}
		 */
		MMListNode node = getNode(inputObj);
		if (null != node) {
			return true;
		} else {
			return false;
		}
	}

	public boolean remove(Object inputObj) {
		/*
		 * if(value == inputObj) { removeThisLink(); return true; } else if(next
		 * != null) { return next.remove(inputObj); } return false;
		 */
		MMListNode node = getNode(inputObj);
		if (null != node) {
			node.removeThisLink();
			return true;
		} else {
			return false;
		}
	}

	private MMListNode getNode(Object inputObj) {
		if (value == inputObj) {
			return this;
		} else if (next != null) {
			return next.getNode(inputObj);
		} else {
			return null;
		}
	}

	// TODO fix as not working
	public void removeThisLink() {
		if (previous == this)// this is the head
		{
			if (null != next) {
				next.previous = next;
			}
			previous = next;
		} else {
			previous.next = next;
		}
	}

	// add a value to the end of the list
	public int addValue(Object inputObj) {
		if (null == next) {
			next = new MMListNode(this, inputObj);
			return 1;
		} else {
			return next.addValue(inputObj) + 1;
		}
	}

	// inserts a node & value AFTER this node
	public void insertValue(Object inputObj) {
		MMListNode newNode = new MMListNode(this, inputObj);
		next.previous = newNode;
		newNode.next = next;
		newNode.previous = this;
		this.next = newNode;
	}

	// change the value at this node
	public void setValue(Object inputObj) {
		value = inputObj;
	}

	public Object getValue() {
		return value;
	}

	public Object getValue(int indexCount) {
		if (0 == indexCount) {
			return getValue();
		} else if (null != next) {
			return next.getValue(--indexCount);
		} else {
			return null;
		}
	}

	public MMListNode getNextNode() {
		return next;
	}

}
