package org.meltzg.edhd.hadoop;

import java.util.List;

/**
 * Represents a locatino in HDFS
 */
public class HDFSLocationInfo {
    private String location;
    private List<HDFSEntry> children;

    public HDFSLocationInfo(String location, List<HDFSEntry> children) {
        super();
        this.location = location;
        this.children = children;
    }

    public HDFSLocationInfo() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<HDFSEntry> getChildren() {
        return children;
    }

    public void setChildren(List<HDFSEntry> children) {
        this.children = children;
    }
}
