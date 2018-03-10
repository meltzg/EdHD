package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentCreationProperties {
	private UUID id;
	private Integer dueDate;
	private String name;
	private String desc;
	private Map<String, PropValue> primaryConfig;
	private Map<String, PropValue> secondaryConfig;
	
	public AssignmentCreationProperties(UUID id, Integer dueDate, String name, String desc, Map<String, PropValue> primaryConfig,
			Map<String, PropValue> secondaryConfig) {
		super();
		this.id = id;
		this.dueDate = dueDate;
		this.name = name;
		this.desc = desc;
		this.primaryConfig = primaryConfig;
		this.secondaryConfig = secondaryConfig;
	}

	public UUID getId() {
		return id;
	}

	public Integer getDueDate() {
		return dueDate;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public GenJobConfiguration getPrimaryConfig() {
		return new GenJobConfiguration(primaryConfig);
	}

	public GenJobConfiguration getSecondaryConfig() {
		return new GenJobConfiguration(secondaryConfig);
	}
}
