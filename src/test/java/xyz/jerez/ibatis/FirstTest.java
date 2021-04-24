package xyz.jerez.ibatis;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.simplelistparameter.Car;
import org.apache.ibatis.submitted.simplelistparameter.CarMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * @author liqilin
 * @since 2021/4/24 19:21
 */
public class FirstTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources
      .getResourceAsReader("org/apache/ibatis/submitted/simplelistparameter/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/simplelistparameter/CreateDB.sql");

  }

  @Test
  void shouldGetACar() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
      Car car = new Car();
      car.setDoors(Arrays.asList("2", "4"));
      List<Car> cars = carMapper.getCar(car);
      Assertions.assertNotNull(cars);
    }
  }

}
