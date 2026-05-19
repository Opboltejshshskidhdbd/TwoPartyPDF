package com.twoparty.pdfreader;

public class StudyItem {
    public int id;
    public String name;
    public String path; // Folder ke liye ye hamesha null rahega
    public int parentId;

    public StudyItem(int id, String name, String path, int parentId) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.parentId = parentId;
    }
}
