package edu.javacourse.studentorder.domain;

public class University {
    private Long universityId;
    private String universityName;


    public University() {
    }

    public University(Long universityId, String universityName) {
        this.universityName = universityName;
        this.universityId = universityId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }
}
