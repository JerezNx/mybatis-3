package xyz.jerez.ibatis;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.simplelistparameter.CarMapper;
import org.apache.ibatis.submitted.simplelistparameter.Rv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liqilin
 * @since 2021/4/18 20:33
 */
public class CacheTest {

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

  /**
   * 一级缓存
   * 测试同一会话下，并发查询
   */
  @Test
  void testLocalCache() throws InterruptedException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
      Rv rv = new Rv();
      rv.doors1 = Arrays.asList("2", "4");
      for (int i = 0; i < 20; i++) {
        new Thread(() -> {
          List<Rv> rvs = carMapper.getRv1(rv);
          System.out.println(rvs.size());
        }).start();
      }
      TimeUnit.SECONDS.sleep(5);
      List<Rv> rvs = carMapper.getRv1(rv);
    }
  }

  /**
   * 一级缓存
   */
  @Test
  void testLocalCache1() throws InterruptedException {
//    设置作用域为 STATEMENT
    sqlSessionFactory.getConfiguration().setLocalCacheScope(LocalCacheScope.STATEMENT);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
//      Arrays.asList("1", "2", "3", "4").parallelStream()
//        .map(carMapper::getRv3)
//        .forEach(System.out::println);

//        .forEach(list -> {
//          list.forEach(car -> System.out.print(car.name + "\t"));
//          System.out.println();
//        });
      carMapper.getRv3("1");
      TimeUnit.SECONDS.sleep(1);
      final List<Rv> rv3 = carMapper.getRv3("1");
      System.out.println(rv3);
    }
  }

  /**
   * 二级缓存
   */
  @Test
  void testSecondCache() {
//    开启二级缓存（默认就是开启的）
    sqlSessionFactory.getConfiguration().setCacheEnabled(true);
    try (SqlSession sqlSession1 = sqlSessionFactory.openSession()) {
      CarMapper carMapper1 = sqlSession1.getMapper(CarMapper.class);
      carMapper1.getRv3("1");
    }

    try (SqlSession sqlSession2 = sqlSessionFactory.openSession()) {
      CarMapper carMapper2 = sqlSession2.getMapper(CarMapper.class);
      carMapper2.getRv3("1");
    }
  }

}
