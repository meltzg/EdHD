package org.meltzg.edhd.assignment;

import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class to represent an assignment definition. The primary configuration represents the assignment
 * that all users will have access to.  the configuration inherited from AssignmentSubmission is treated as
 * the secondary configuration used to create the validation data
 */
public class AssignmentDefinition extends AssignmentSubmission {

    private Long dueDate;
    private String name;
    private String desc;
    private Map<String, PropValue> primaryConfig;
    private UUID primaryConfigLoc;
    private String primarySrcName;
    private UUID primarySrcLoc;

    public AssignmentDefinition(UUID id, String user, Long dueDate, String name, String desc, Map<String, PropValue> primaryConfig, UUID primaryConfigLoc,
                                Map<String, PropValue> config, UUID configLoc, String primarySrcName, UUID primarySrcLoc, String srcName, UUID srcLoc) {
        super(id, user, config, configLoc, srcName, srcLoc);
        this.dueDate = dueDate;
        this.name = name;
        this.desc = desc;
        this.primaryConfig = primaryConfig;
        this.primaryConfigLoc = primaryConfigLoc;
        this.primarySrcName = primarySrcName;
        this.primarySrcLoc = primarySrcLoc;
    }

    public AssignmentDefinition() {
    }

    public AssignmentDefinition(AssignmentDefinition other) {
        super(other);
        this.dueDate = other.dueDate;
        this.name = other.name;
        this.desc = other.desc;
        this.primaryConfig = new HashMap<String, PropValue>();
        for (Map.Entry<String, PropValue> entry : other.primaryConfig.entrySet()) {
            this.primaryConfig.put(entry.getKey(), new PropValue(entry.getValue()));
        }
        this.primaryConfigLoc = other.primaryConfigLoc;
        this.primarySrcName = other.primarySrcName;
        this.primarySrcLoc = other.primarySrcLoc;
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
