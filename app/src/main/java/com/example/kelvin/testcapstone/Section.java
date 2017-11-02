package com.example.kelvin.testcapstone;

/**
 * Created by kelvin on 6/20/17.
 * Contains info for Sections
 */

class Section {
    private int crn, seats;
    private String section, days, time, teacher, building, room;

    Section(int crn, String section, String days, String time, int seats, String teacher, String building, String room) {
        this.crn = crn;
        this.seats = seats;
        this.section = section;
        this.days = days;
        this.time = time;
        this.teacher = teacher;
        this.building = building;
        this.room = room;
    }

    @Override
    public String toString() {
        String ss = "";
        ss += "\t\t\t" + "Section: " + section + "\n";
        ss += "\t\t\t" + "CRN: " + crn + "\n";
        ss += "\t\t\t" + "Days: " + days + "\n";
        ss += "\t\t\t" + "Time: " + time + "\n";
        ss += "\t\t\t" + "Seats: " + seats + "\n";
        ss += "\t\t\t" + "Instructor: " + teacher + "\n";
        ss += "\t\t\t" + "Building: " + building + "\n";
        ss += "\t\t\t" + "Room: " + room + "\n";
        return ss;
    }
}
