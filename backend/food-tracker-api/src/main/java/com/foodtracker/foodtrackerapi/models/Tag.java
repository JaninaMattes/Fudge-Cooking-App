package com.foodtracker.foodtrackerapi.models;

import java.util.Objects;

public class Tag {

    private Integer tagId;
    private String label;

    public Tag(Integer tagId, String label) {
        this.tagId = tagId;
        this.label = label;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return Objects.equals(tagId, tag.tagId) && Objects.equals(label, tag.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, label);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", label='" + label + '\'' +
                '}';
    }
}
