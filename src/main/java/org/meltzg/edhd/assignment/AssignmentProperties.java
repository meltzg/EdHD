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
	protected UUID configLoc;
	protected String srcName;
	protected UUID srcLoc;

	public AssignmentProperties(UUID id, Long dueDate, String name, String desc, Map<String, PropValue> config,
			UUID configLoc, String srcName, UUID srcLoc) {
		super();
		this.id = id;
		this.dueDate = dueDate;
		this.name = name;
		this.desc = desc;
		this.config = config;
		this.configLoc = configLoc;
		this.srcName = srcName;
		this.srcLoc = srcLoc;
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
