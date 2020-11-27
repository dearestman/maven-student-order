package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.domain.CountryArea;
import edu.javacourse.studentorder.domain.PassportOffice;
import edu.javacourse.studentorder.domain.RegisterOffice;
import edu.javacourse.studentorder.domain.Street;
import edu.javacourse.studentorder.exeption.DaoExeption;

import java.util.List;

public interface DictionaryDao {
    List<Street> findStreets(String pattern) throws DaoExeption ;
    List<PassportOffice> findPassportOffices(String areaId) throws DaoExeption ;
    List<RegisterOffice> findRegisterOffices(String areaId) throws DaoExeption ;
    List<CountryArea> findAreas(String areaId) throws DaoExeption ;

}
