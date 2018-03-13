package org.meltzg.edhd.status;

public enum StatusValue {
	SUCCESS(1),
	PENDING(0),
	FAIL(-1);
	
	private int val;

	StatusValue(int val) {
		this.val = val;
	}
}
