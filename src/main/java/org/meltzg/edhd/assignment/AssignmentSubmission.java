package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentSubmission {
	protected UUID id;
	protected String user;
	
	protected Map<String, PropValue> config;
	protected UUID configLoc;
	protected String srcName;
	protected UUID srcLoc;

	public AssignmentSubmission(UUID id, String user, Map<String, PropValue> config,
			UUID configLoc, String srcName, UUID srcLoc) {
		super();
		this.id = id;
		this.user = user;
		
		this.config = config;
		this.configLoc = configLoc;
		this.srcName = srcName;
		this.srcLoc = srcLoc;
	}

	public AssignmentSubmission() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Map<String, PropValue> getConfig() {
		return config;
	}

	public void setConfig(Map<String, PropValue> config) {
		this.config = config;
	}

	public UUID getConfigLoc() {
		return configLoc;
	}

	public void setConfigLoc(UUID configLoc) {
		this.configLoc = configLoc;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String configSrcName) {
		this.srcName = configSrcName;
	}

	public UUID getSrcLoc() {
		return srcLoc;
	}

	public void setSrcLoc(UUID srcLoc) {
		this.srcLoc = srcLoc;
	}
}
