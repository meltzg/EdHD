package org.meltzg.edhd.submission;

import java.util.UUID;

public class StatusProperties {
	private UUID id;
	private StatusValue compileStatus;
	private String compileMsg;
	private StatusValue runStatus;
	private String runMsg;
	private StatusValue validateStatus;
	private String validateMsg;
	private StatusValue completeStatus;
	private String completeMsg;

	public StatusProperties(UUID id, StatusValue compileStatus, String compileMsg, StatusValue runStatus, String runMsg,
			StatusValue validateStatus, String validateMsg, StatusValue completeStatus, String completeMsg) {
		super();
		this.id = id;
		this.compileStatus = compileStatus;
		this.compileMsg = compileMsg;
		this.runStatus = runStatus;
		this.runMsg = runMsg;
		this.validateStatus = validateStatus;
		this.validateMsg = validateMsg;
		this.completeStatus = completeStatus;
		this.completeMsg = completeMsg;
	}

	public StatusProperties() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public StatusValue getCompileStatus() {
		return compileStatus;
	}

	public void setCompileStatus(StatusValue compileStatus) {
		this.compileStatus = compileStatus;
	}

	public String getCompileMsg() {
		return compileMsg;
	}

	public void setCompileMsg(String compileMsg) {
		this.compileMsg = compileMsg;
	}

	public StatusValue getRunStatus() {
		return runStatus;
	}

	public void setRunStatus(StatusValue runStatus) {
		this.runStatus = runStatus;
	}

	public String getRunMsg() {
		return runMsg;
	}

	public void setRunMsg(String runMsg) {
		this.runMsg = runMsg;
	}

	public StatusValue getValidateStatus() {
		return validateStatus;
	}

	public void setValidateStatus(StatusValue validateStatus) {
		this.validateStatus = validateStatus;
	}

	public String getValidateMsg() {
		return validateMsg;
	}

	public void setValidateMsg(String validateMsg) {
		this.validateMsg = validateMsg;
	}

	public StatusValue getCompleteStatus() {
		return completeStatus;
	}

	public void setCompleteStatus(StatusValue completeStatus) {
		this.completeStatus = completeStatus;
	}

	public String getCompleteMsg() {
		return completeMsg;
	}

	public void setCompleteMsg(String completeMsg) {
		this.completeMsg = completeMsg;
	}
}