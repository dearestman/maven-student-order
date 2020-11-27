package edu.javacourse.studentorder;

import edu.javacourse.studentorder.dao.DictionaryDaoImpl;
import edu.javacourse.studentorder.domain.*;
import edu.javacourse.studentorder.domain.children.AnswerChildren;
import edu.javacourse.studentorder.domain.register.AnswerCityRegister;
import edu.javacourse.studentorder.domain.student.AnswerStudent;
import edu.javacourse.studentorder.domain.wedding.AnswerWeeding;
import edu.javacourse.studentorder.mail.MailSender;
import edu.javacourse.studentorder.validator.ChildrenValidator;
import edu.javacourse.studentorder.validator.CityRegisterValidator;
import edu.javacourse.studentorder.validator.StudentValidator;
import edu.javacourse.studentorder.validator.WeedingValidator;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class StudentOrderValidator {

    private CityRegisterValidator cityRegisterVal;
    private WeedingValidator weedingVal;
    private ChildrenValidator childrenVal;
    private StudentValidator studentVal;
    private MailSender mailSender;

    public StudentOrderValidator(){
        cityRegisterVal = new CityRegisterValidator();
        weedingVal = new WeedingValidator();
        childrenVal = new ChildrenValidator();
        studentVal = new StudentValidator();
        mailSender = new MailSender();
    }
    public static void main(String[] args)throws Exception{



//        StudentOrderValidator sov = new StudentOrderValidator();
//        sov.checkAll();
    }

    public void checkAll(){
        List<StudentOrder> soList = readStudentOrders();

        for (StudentOrder so : soList){
            checkOneOrder(so);
        }
//
//        for (StudentOrder so: soList) {
//            System.out.println();
//            checkOneOrder(so);
//        }

    }

    public List<StudentOrder> readStudentOrders(){
        List<StudentOrder> soList = new LinkedList<StudentOrder>();

        for(int c=0; c< 5; c++){
            StudentOrder so = SaveStudentOrder.buildStudentOrder(c);
            soList.add(so);
        }

        return soList;
    }

    public void checkOneOrder(StudentOrder so){
        AnswerCityRegister citiAnswer = checkCityRegister(so);
//        AnswerWeeding wedAnswer = checkWedding(so);
//        AnswerChildren childAnswer = checkChildren(so);
//        AnswerStudent studentAnswer = checkStudent(so);
//        sendMail(so);
    }

    public AnswerCityRegister checkCityRegister(StudentOrder so){
        return cityRegisterVal.checkCityRegister(so);

    }

    public AnswerWeeding checkWedding(StudentOrder so){
        return weedingVal.checkWedding(so);
    }

    public AnswerChildren checkChildren(StudentOrder so){
        return childrenVal.checkChildren(so);
    }

    public AnswerStudent checkStudent(StudentOrder so) {
        return studentVal.checkStudent(so);
    }

    public void sendMail(StudentOrder so){
        mailSender.sendMail(so);
    }
}
