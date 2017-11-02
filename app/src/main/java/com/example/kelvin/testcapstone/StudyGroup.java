package com.example.kelvin.testcapstone;

import java.util.ArrayList;

class StudyGroup {
    ArrayList<StudyGroupMember> groupMembers = new ArrayList<>();
    //Stored for later use to get details
    int coursePk;
    private int groupPk;//Matches pk from server
    private String course = "";//Format: "PRFX NMBR : Title"

    StudyGroup(int pk) {
        groupPk = pk;
    }

    void addMember(StudyGroupMember m) {
        groupMembers.add(m);
    }

    void addCourse(String c) {
        course = c;
    }

    int getPk() {
        return groupPk;
    }

    @Override
    public String toString() {
        StringBuilder ss = new StringBuilder();
        ss.append("Group for ").append(course).append('\n');
        ss.append("Group Members: ").append('\n');
        for (StudyGroupMember member : groupMembers) {
            ss.append(member.toString()).append('\n');
        }
        ss.append('\n');
        return ss.toString();
    }

    //Reusing class for displaying classmates as opposed to group members
    String displayForClass() {
        StringBuilder ss = new StringBuilder();
        ss.append(course);

        if (groupMembers.size() > 0) {

            for (StudyGroupMember classmate : groupMembers) {
                ss.append(classmate.displayAsClassmate());
            }
        } else {
            ss.append("\t\t\tNo classmates\n");
        }
        return ss.toString();
    }
}
