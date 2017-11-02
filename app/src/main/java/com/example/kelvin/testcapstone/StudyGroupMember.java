package com.example.kelvin.testcapstone;

class StudyGroupMember {
    private int studentPk;//Matches pk from server

    private String name;
    private String email;

    StudyGroupMember(int pk) {
        studentPk = pk;
    }

    void addName(String n) {
        name = n;
    }

    void addEmail(String e) {
        email = e;
    }

    int getPk() {
        return studentPk;
    }

    private String getEmail() {
        return email;
    }

    private String getName() {
        return name;
    }

    @Override
    public String toString() {
        final String TAB = "\u0009";
        return TAB + TAB + TAB + getName() + ": " + getEmail();
    }

    //Used for representing classmate, not
    String displayAsClassmate() {
        return "\t\t\t" + getName();
    }
}
