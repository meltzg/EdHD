package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentCreationProperties {
	private UUID id;
	private Long dueDate;
	private String name;
	private String desc;
	private Map<String, PropValue> primaryConfig;
	private Map<String, PropValue> secondaryConfig;
	
	public AssignmentCreationProperties(UUID id, Long dueDate, String name, String desc, Map<String, PropValue> primaryConfig,
			Map<String, PropValue> secondaryConfig) {
		super();
		this.id = id;
		this.dueDate = dueDate;
		this.name = name;
		this.desc = desc;
		this.primaryConfig = primaryConfig;
		this.secondaryConfig = secondaryConfig;
	}
	
	AssignmentCreationProperties() {
		
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Long getDueDate() {
		return dueDate;
	}

	public void setDueDate(Long dueDate) {
		this.dueDate = dueDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Map<String, PropValue> getPrimaryConfig() {
		return primaryConfig;
	}

	public void setPrimaryConfig(Map<String, PropValue> primaryConfig) {
		this.primaryConfig = primaryConfig;
	}

	public Map<String, PropValue> getSecondaryConfig() {
		return secondaryConfig;
	}

	public void setSecondaryConfig(Map<String, PropValue> secondaryConfig) {
		this.secondaryConfig = secondaryConfig;
	}
}
