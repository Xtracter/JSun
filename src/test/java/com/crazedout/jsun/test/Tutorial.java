package com.crazedout.jsun.test;

import com.crazedout.jsun.JSunClass;

@JSunClass(exclude="values")
public class Tutorial {

    private int id;
    private String title;
    private String description;
    private int level;

    public void setValues(int id, String title, String desc, int level){
        this.id=id;
        this.title=title;
        this.description=desc;
        this.level=level;
    }


    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public String getValues() {
        return id + " " + title + " " + description + " " + level;
    }
}
