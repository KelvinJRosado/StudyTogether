package com.example.kelvin.testcapstone;

import java.util.ArrayList;

/**
 * Created by kelvin on 6/20/17.
 * Contains info for Courses
 */

class Course {
    private int hours;
    private String prefix, number, title;
    private ArrayList<Section> sections = new ArrayList<>();

    Course(String prefix, String number, String title, int hours) {
        this.hours = hours;
        this.prefix = prefix;
        this.number = number;
        this.title = title;
    }

    void addSection(Section s) {
        sections.add(s);
    }

    public String toString() {
        String ss = "" + ("Prefix: " + prefix + "\n");
        ss += "Number: " + number + "\n";
        ss += "Title: " + title + "\n";
        ss += "Credit Hours: " + hours + "\n";
        ss += "Sections: \n";
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            ss += section.toString();
            ss += "\n";
        }
        return ss;
    }
}
