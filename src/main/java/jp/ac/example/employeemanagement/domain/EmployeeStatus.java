package jp.ac.example.employeemanagement.domain;

public enum EmployeeStatus {
    ACTIVE,
    LEAVE,
    RESIGNED;

    public String getLabel() {
        return switch (this) {
            case ACTIVE -> "在籍";
            case LEAVE -> "休職";
            case RESIGNED -> "退職";
        };
    }
}
