package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentSubmissionProperties extends AssignmentProperties {
	
	protected Map<String, PropValue> primaryConfig;
	private String primarySrcName;
	
	public AssignmentSubmissionProperties(UUID id, Long dueDate, String name, String desc, Map<String, PropValue> primaryConfig,
			Map<String, PropValue> config, String primarySrcName, String configSrcName) {
		super(id, dueDate, name, desc, config, configSrcName);
		this.primaryConfig = primaryConfig;
		this.primarySrcName = primarySrcName;
	}
	
	public AssignmentSubmissionProperties() {
	}
	
	public Map<String, PropValue> getPrimaryConfig() {
		return primaryConfig;
	}

	public void setPrimaryConfig(Map<String, PropValue> primaryConfig) {
		this.primaryConfig = primaryConfig;
	}

	public String getPrimarySrcName() {
		return primarySrcName;
	}

	public void setPrimarySrcName(String primarySrcName) {
		this.primarySrcName = primarySrcName;
	}
}
