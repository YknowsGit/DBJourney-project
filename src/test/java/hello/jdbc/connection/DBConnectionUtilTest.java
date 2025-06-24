package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class DBConnectionUtilTest {

    @Test
    void connection() {
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
    }
    /*
    url=jdbc:h2:tcp://localhost/~/test2 user=SA, class=class org.h2.jdbc.JdbcConnection

    => 이것이 바로 H2 데이터베이스 드라이버가 제공하는 H2 전용 커넥션이다.
    => 물론, 이 커넥션은 JDBC 표준 커넥션 인터페이스인 "java.sql.Connection"인터페이스를 구현하고있다.

    => H2 데이터베이스 드라이버를 어떻게 찾는 걸까? => JDBC 커넥션 인터페이스와 구현
    */

}
/*
인터페이스는 java.sql.Connection이고, (사진)
구현체는 실제 H2 드라이버가 제공하는 H2 커넥션을 반환
 */