package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentCreationProperties extends AssignmentProperties {
	
	protected Map<String, PropValue> primaryConfig;
	
	public AssignmentCreationProperties(UUID id, Long dueDate, String name, String desc, Map<String, PropValue> primaryConfig,
			Map<String, PropValue> secondaryConfig) {
		super();
		this.id = id;
		this.dueDate = dueDate;
		this.name = name;
		this.desc = desc;
		this.primaryConfig = primaryConfig;
		this.config = secondaryConfig;
	}
	
	public GenJobConfiguration getPrimaryConfig() {
		if (primaryConfig.isEmpty()) {
			return null;
		}
		return new GenJobConfiguration(primaryConfig);
	}

	public void setPrimaryConfig(Map<String, PropValue> primaryConfig) {
		this.primaryConfig = primaryConfig;
	}
}
