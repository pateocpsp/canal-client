package com.pateo.qingcloud.canal.properties.es;


public class ParentChildren {
    private String name;
    private String parent;

    @Override
    public String toString() {
        return "ParentChildren{" +
                "name='" + name + '\'' +
                ", parent='" + parent + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
