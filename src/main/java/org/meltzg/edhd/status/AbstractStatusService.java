package org.meltzg.edhd.status;

import java.util.UUID;

import org.meltzg.edhd.db.DBServiceBase;

public abstract class AbstractStatusService extends DBServiceBase {
	
	public abstract StatusProperties getStatus(UUID id);
	public abstract void updateStatus(StatusProperties status);

	@Override
	protected String TABLE_NAME() {
		return "status";
	}

}
