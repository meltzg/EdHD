package org.meltzg.edhd.submission;

public enum StatusValue {
	SUCCESS(2),
	COMPLETE(1),
	PENDING(0),
	FAIL(-1);
	
	private int val;

	StatusValue(int val) {
		this.val = val;
	}
	
	public int value() {
		return this.val;
	}
	
	public static StatusValue fromInteger(int x) {
        switch(x) {
        case 0:
            return PENDING;
        case 1:
            return COMPLETE;
        case 2:
        	return SUCCESS;
        default:
        	return FAIL;
        }
    }
}
