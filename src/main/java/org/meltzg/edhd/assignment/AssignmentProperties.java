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
	protected String srcName;
	
	public AssignmentProperties(UUID id, Long dueDate, String name, String desc, Map<String, PropValue> config, String srcName) {
		super();
		this.id = id;
		this.dueDate = dueDate;
		this.name = name;
		this.desc = desc;
		this.config = config;
		this.srcName = srcName;
	}

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

	public void setConfig(Map<String, PropValue> config) {
		this.config = config;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String configSrcName) {
		this.srcName = configSrcName;
	}
}
