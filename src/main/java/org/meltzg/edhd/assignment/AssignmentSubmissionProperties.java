package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentSubmissionProperties extends AssignmentProperties {
	
	protected Map<String, PropValue> primaryConfig;
	private UUID primaryConfigLoc;
	private String primarySrcName;
	private UUID primarySrcLoc;
	
	public AssignmentSubmissionProperties(UUID id, Long dueDate, String name, String desc, Map<String, PropValue> primaryConfig, UUID primaryConfigLoc,
			Map<String, PropValue> config, UUID configLoc, String primarySrcName, UUID primarySrcLoc, String srcName, UUID srcLoc) {
		super(id, dueDate, name, desc, config, configLoc, srcName, srcLoc);
		this.primaryConfig = primaryConfig;
		this.primaryConfigLoc = primaryConfigLoc;
		this.primarySrcName = primarySrcName;
		this.primarySrcLoc = primarySrcLoc;
	}
	
	public AssignmentSubmissionProperties() {
	}
	
	public Map<String, PropValue> getPrimaryConfig() {
		return primaryConfig;
	}

	public void setPrimaryConfig(Map<String, PropValue> primaryConfig) {
		this.primaryConfig = primaryConfig;
	}

	public UUID getPrimaryConfigLoc() {
		return primaryConfigLoc;
	}

	public void setPrimaryConfigLoc(UUID primaryConfigLoc) {
		this.primaryConfigLoc = primaryConfigLoc;
	}

	public String getPrimarySrcName() {
		return primarySrcName;
	}

	public void setPrimarySrcName(String primarySrcName) {
		this.primarySrcName = primarySrcName;
	}

	public UUID getPrimarySrcLoc() {
		return primarySrcLoc;
	}

	public void setPrimarySrcLoc(UUID primarySrcLoc) {
		this.primarySrcLoc = primarySrcLoc;
	}
}
