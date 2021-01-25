package com.techflow.techhubbackend.model;

public enum ReportType {
    MISLEADING ("Misleading"),
    INAPPROPRIATE("Inappropriate"),
    SPAM("Spam"),
    SCAM("Scam"),
    OTHERS("Others");

    private final String name;

    ReportType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
