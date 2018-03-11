package org.meltzg.edhd.assignment;

import java.util.Map;
import java.util.UUID;

import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

public class AssignmentProperties {
	protected UUID id;
	protected Long dueDate;
	protected String name;
	protected String desc;
	protected Map<String, PropValue> config;

	AssignmentProperties() {
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
	
	public GenJobConfiguration getConfig() {
		if (config.isEmpty()) {
			return null;
		}
		return new GenJobConfiguration(config);
	}

	public void setSecondaryConfig(Map<String, PropValue> config) {
		this.config = config;
	}

}
