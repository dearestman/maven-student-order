package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.config.Config;
import edu.javacourse.studentorder.domain.*;
import edu.javacourse.studentorder.exeption.DaoExeption;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentOrderDaoImpl implements StudentOrderDao{

     public static final String INSERT_ORDER =
             "INSERT INTO js_student_order(student_order_status, student_order_date, h_sur_name, " +
                     "h_given_name, h_patronymic, h_date_of_birth, h_passport_serial, h_passport_number, " +
                     "h_passport_date, h_passport_office_id, h_post_index, h_street_code, h_building, h_extension, " +
                     "h_apartment, h_university_id, h_student_number, w_sur_name, w_given_name, w_patronymic, w_date_of_birth, w_passport_serial, w_passport_number, " +
                     "w_passport_date, w_passport_office_id, w_post_index, w_street_code, w_building, w_extension, w_apartment, w_university_id, w_student_number, " +
                     "certificate_id, register_office_id, marriage_date) " +
             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
    // TODO refactoring- make one method

    private static final String INSERT_CHILD = "INSERT INTO js_student_child(" +
            "student_order_id, c_sur_name, c_given_name, " +
            "c_patronymic, c_date_of_birth, c_certificate, c_certificate_date, " +
            "c_register_office_id, c_post_index, c_street_code, c_building, c_extension, " +
            "c_apartment)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);" ;


    private static final String SELECT_ORDERS = "SELECT ro.r_office_area_id, ro.r_office_name, so.*, " +
            "po_h.P_office_area_id as h_p_office_area_id, po_h.p_office_name as h_p_office_name, " +
            "po_w.P_office_area_id as w_p_office_area_id, po_w.p_office_name as w_p_office_name " +
            "FROM js_student_order so " +
            "INNER JOIN js_register_office ro ON ro.r_office_id = so.register_office_id " +
            "INNER JOIN js_passport_office po_h ON po_h.p_office_id = so.h_passport_office_id " +
            "INNER JOIN js_passport_office po_w ON po_w.p_office_id = so.w_passport_office_id " +
            "WHERE student_order_status = ? ORDER BY student_order_date";

    private static final String SELECT_CHILD = "SELECT soc.*, ro.r_office_area_id, ro.r_office_name " +
            "FROM js_student_child soc " +
            "INNER JOIN js_register_office ro on ro.r_office_id = soc.c_register_office_id " +
            "WHERE soc.student_order_id IN ";
//    private static final String SELECT_ORDERS = "SELECT * FROM js_student_order WHERE student_order_status  = 0 ORDER BY student_order_date";


    private static final String SELECT_ORDERS_FULL = "SELECT ro.r_office_area_id, ro.r_office_name, so.*, " +
            "po_h.P_office_area_id as h_p_office_area_id, po_h.p_office_name as h_p_office_name, " +
            "po_w.P_office_area_id as w_p_office_area_id, po_w.p_office_name as w_p_office_name, " +
            "soc.*, ro_c.r_office_area_id, ro_c.r_office_name " +
            "FROM js_student_order so " +
            "INNER JOIN js_register_office ro ON ro.r_office_id = so.register_office_id " +
            "INNER JOIN js_passport_office po_h ON po_h.p_office_id = so.h_passport_office_id " +
            "INNER JOIN js_passport_office po_w ON po_w.p_office_id = so.w_passport_office_id " +
            "INNER JOIN js_student_child soc ON soc.student_order_id=so.student_order_id " +
            "INNER JOIN js_register_office ro_c on ro_c.r_office_id = soc.c_register_office_id " +
            "WHERE student_order_status = ? ORDER BY student_order_date";

    private Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(
                Config.getProperty(Config.DB_URL),
                Config.getProperty(Config.DB_LOGIN), Config.getProperty(Config.DB_PASSWORD)
        );
        return  con;
    }

    @Override
    public Long saveStudentOrder(StudentOrder so) throws DaoExeption {

        Long result = -1L;


        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(INSERT_ORDER, new String[] {"student_order_id"})){

            con.setAutoCommit(false);

            try {
                stmt.setInt(1, StudentOrderStatus.START.ordinal());
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

                //husband and wife
                setParamsForAdult(stmt, 3, so.getHusband());
                setParamsForAdult(stmt, 18, so.getWife());

                //marry info
                stmt.setString(33, so.getMarriageCertificateId());
                stmt.setLong(34, so.getMarriageOffice().getOfficeId());
                stmt.setDate(35, Date.valueOf(so.getMarriageDate()));

                stmt.executeUpdate();

                ResultSet gkRs = stmt.getGeneratedKeys();
                if (gkRs.next()) {
                    result = gkRs.getLong(1);
                }
                gkRs.close();

                saveChildren(con, so, result);

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
        catch (SQLException ex){
            throw new DaoExeption(ex);
        }

        return result;
    }



    private void saveChildren(Connection con, StudentOrder so, Long soId) throws SQLException{

        try(PreparedStatement stmt = con.prepareStatement(INSERT_CHILD)){
            for (Child child : so.getChildren()){
                stmt.setLong(1, soId);
                setParamsForChild(stmt,child);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

    }

    private void setParamsForChild(PreparedStatement stmt, Child child) throws SQLException {
        setParamsForPerson(stmt, 2, child);
        stmt.setString(6,child.getCertificateNumber());
        stmt.setDate(7,Date.valueOf(child.getDateOfBirth()));
        stmt.setLong(8,child.getIssueDepartment().getOfficeId());
        setParamsForAddress(stmt,9, child);
    }

    private void setParamsForAdult(PreparedStatement stmt, int start, Adult adult) throws SQLException {
        setParamsForPerson(stmt, start, adult);
        stmt.setString(start+4, adult.getPassportSerial());
        stmt.setString(start+5, adult.getPassportNumber());
        stmt.setDate(start+6, Date.valueOf(adult.getIssueDate()));
        stmt.setLong(start+7, adult.getIssueDepartment().getOfficeId());
        //husband address
        setParamsForAddress(stmt, start + 8, adult);
        stmt.setLong(start+13, adult.getUniversity().getUniversityId());
        stmt.setString(start+14, adult.getStudentId());
    }

    private void setParamsForPerson(PreparedStatement stmt, int start, Person person) throws SQLException {
        stmt.setString(start, person.getSurName());
        stmt.setString(start +1, person.getGivenName());
        stmt.setString(start +2, person.getPatronymic());
        stmt.setDate(start +3, Date.valueOf(person.getDateOfBirth()));
    }

    private void setParamsForAddress(PreparedStatement stmt, int start, Person person) throws SQLException {
        Address h_address = person.getAddress();
        stmt.setString(start, h_address.getPostCode());
        stmt.setLong(start +1, h_address.getStreet().getStreetCode());
        stmt.setString(start +2, h_address.getBuilding());
        stmt.setString(start +3, h_address.getExtension());
        stmt.setString(start +4, h_address.getApartment());
    }

    @Override
    public List<StudentOrder> getStudentOrders() throws DaoExeption {
        return getStudentOrdersOneSelect();
//        return getStudentOrdersTwoSelect();
    }

//    @org.jetbrains.annotations.NotNull
    private List<StudentOrder> getStudentOrdersOneSelect() throws DaoExeption {
        List<StudentOrder> result = new LinkedList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_ORDERS_FULL, new String[] {"student_order_id"})) {

            Map<Long, StudentOrder> maps = new HashMap<>();
            stmt.setInt(1, StudentOrderStatus.START.ordinal());
            ResultSet rs = stmt.executeQuery();
            List<Long> ids = new LinkedList<>();
            while (rs.next()){

                Long soId = rs.getLong("student_order_id");
                if (!maps.containsKey(soId)){

                    StudentOrder so = getFullStudentOrder(rs);

                    result.add(so);
                    maps.put(soId, so);
//                ids.add(so.getStudentOrderId());
                }
                StudentOrder so = maps.get(soId);
                so.addChild(fillChild(rs));

            }
            findChildren(con, result);

            rs.close();
        } catch (SQLException ex){
            throw new DaoExeption(ex);
        }
        return result;
    }


    private List<StudentOrder> getStudentOrdersTwoSelect() throws DaoExeption {
        List<StudentOrder> result = new LinkedList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_ORDERS_FULL, new String[] {"student_order_id"})) {

            stmt.setInt(1, StudentOrderStatus.START.ordinal());
            ResultSet rs = stmt.executeQuery();
            List<Long> ids = new LinkedList<>();
            while (rs.next()){
                StudentOrder so = getFullStudentOrder(rs);

                result.add(so);
                ids.add(so.getStudentOrderId());
            }
            findChildren(con, result);

            rs.close();
        } catch (SQLException ex){
            throw new DaoExeption(ex);
        }
        return result;
    }

    private StudentOrder getFullStudentOrder(ResultSet rs) throws SQLException {
        StudentOrder so = new StudentOrder();
        fillStudentOrder(rs, so);
        fillMarriage(rs, so);

        so.setHusband(fillAdult(rs, "h_"));
        so.setWife(fillAdult(rs, "w_"));
        return so;
    }

    private void findChildren(Connection con, List<StudentOrder> result) throws SQLException {
        String cl = "(" + result.stream().map(so -> String.valueOf(so.getStudentOrderId()))
                .collect(Collectors.joining(",")) + ")";

        Map<Long, StudentOrder> maps = result.stream().collect(Collectors
                .toMap(so -> so.getStudentOrderId(), so -> so));



        try (PreparedStatement stmt = con.prepareStatement(SELECT_CHILD+cl)){
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                Child ch = fillChild(rs);
                StudentOrder so = maps.get(rs.getLong("student_order_id"));
            }
        }
    }

    private Child fillChild(ResultSet rs) throws SQLException {
        String surName = rs.getString("c_sur_name");
        String givenName = rs.getString("c_given_name");
        String patronymic = rs.getString("c_patronymic");
        LocalDate dateOfBirth = rs.getDate("c_date_of_birth").toLocalDate();

        Child ch = new Child(surName,givenName,patronymic,dateOfBirth);

        ch.setCertificateNumber(rs.getString("c_certificate"));
        ch.setIssueDate(rs.getDate("c_certificate_date").toLocalDate());

        Long roId = rs.getLong("c_register_office_id");
        String roArea = rs.getString("r_office_area_id");
        String roName = rs.getString("r_office_name");
        RegisterOffice ro = new RegisterOffice(roId, roArea, roName);
        ch.setIssueDepartment(ro);


        Address adr = new Address();
        Street st = new Street(rs.getLong("c_street_code"),"");
        adr.setStreet(st);
        adr.setPostCode(rs.getString("c_post_index"));
        adr.setBuilding(rs.getString("c_building"));
        adr.setExtension(rs.getString("c_extension"));
        adr.setApartment(rs.getString("c_apartment"));
        ch.setAddress(adr);



        return  ch;
    }


    private void fillStudentOrder(ResultSet rs, StudentOrder so) throws SQLException {
        so.setStudentOrderId(rs.getLong("student_order_id"));
        so.setStudentOrderDate(rs.getTimestamp("student_order_date").toLocalDateTime());
        so.setStudentOrderStatus(StudentOrderStatus.fromValue(rs.getInt("student_order_status")));
    }

    private void fillMarriage(ResultSet rs, StudentOrder so) throws SQLException {
        so.setMarriageCertificateId(rs.getString("certificate_id"));
        so.setMarriageDate(rs.getDate("marriage_date").toLocalDate());

        Long roId = rs.getLong("register_office_id");

        String areaId = rs.getString("r_office_area_id");
        String name = rs.getString("r_office_name");
        RegisterOffice ro = new RegisterOffice(roId,areaId,name);
        so.setMarriageOffice(ro);
    }

    private Adult fillAdult(ResultSet rs, String pref) throws SQLException {
        Adult adult = new Adult();
        adult.setSurName(rs.getString(pref+"sur_name"));
        adult.setGivenName(rs.getString(pref+"given_name"));
        adult.setPatronymic(rs.getString(pref+"patronymic"));
        adult.setDateOfBirth(rs.getDate(pref+"date_of_birth").toLocalDate());
        adult.setPassportSerial(rs.getString(pref+"passport_serial"));
        adult.setPassportNumber(rs.getString(pref+"passport_number"));
        adult.setIssueDate(rs.getDate(pref+"passport_date").toLocalDate());

        Long poId = rs.getLong(pref + "passport_office_id");
        String poArea = rs.getString(pref + "p_office_area_id");
        String poName = rs.getString(pref + "p_office_name");
        PassportOffice po = new PassportOffice(poId,poArea,poName);
        adult.setIssueDepartment(po);

        Address adr = new Address();

        Street st = new Street(rs.getLong(pref+"street_code"),"");
        adr.setStreet(st);

        adr.setPostCode(rs.getString(pref+"post_index"));
        adr.setBuilding(rs.getString(pref+"building"));
        adr.setExtension(rs.getString(pref+"extension"));
        adr.setApartment(rs.getString(pref+"apartment"));

        University uni = new University(rs.getLong(pref+"university_id"),"");
        adult.setUniversity(uni);
        adult.setStudentId(rs.getString(pref+"student_number"));

        return adult;
    }

}
