package org.meltzg.edhd.hadoop;

public class HDFSEntry {
    private String group;
    private String owner;
    private String permissions;
    private String path;
    private long size;
    private boolean isDirectory;

    public HDFSEntry(String group, String owner, String permissions, String path, long size, boolean isDirectory) {
        super();
        this.group = group;
        this.owner = owner;
        this.permissions = permissions;
        this.path = path;
        this.setSize(size);
        this.isDirectory = isDirectory;
    }

    public HDFSEntry() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
}