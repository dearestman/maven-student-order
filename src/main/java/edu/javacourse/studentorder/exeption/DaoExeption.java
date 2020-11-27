package edu.javacourse.studentorder.exeption;

public class DaoExeption extends Exception{
    public DaoExeption() {
    }

    public DaoExeption(String message) {
        super(message);
    }

    public DaoExeption(Throwable cause) {
        super(cause);
    }

    public DaoExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
