package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        // 실제 DB와 연결해서 커넥션을 하나 얻게 됨.
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션을 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링: HikariProxyConnection(Proxy) -> JdbcConnection(Target)
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        Thread.sleep(1000);
        /* 커넥션 풀에서 커넥션을 생성하는 작업은 애플리케이션 실행 속도에
           영향을 주지 않기 위해 별도의 쓰레드에서 작동한다. 별도의 쓰레드
           에서 동작하기 때문에 테스트가 먼저 종료될 수 있어, Thread.sleep
           을 통해 대기 시간을 주어야 쓰레드 풀에 커넥션이 생성되는 로그를 확인할 수 있다. */
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        // 인터페이스 dataSource를 통해 커넥션을 가져온다는 차이가 있음.
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}
/* "파라미터 차이"
기존 DriverManager를 통해 커넥션을 획득하는 방법과
DataSource를 통해 커넥션을 획득하는 방법에는 큰 차이가 있다.

구체적으로 DataSource를 통해 커넥션을 획득하는 방법에는
생성하는 시점에만 URL, USERNAME, PASSWORD를 넣어주고,
사용하는 시점에는 그냥 getConnection()만 호출하면 됨
(URL, USERNAME, PASSWORD 몰라도 됨. Why? 이미 생성 시점에 생성 했기 때문)

더 구체적으로
DriverManager와 같은 경우 3가지 파라미터를 계속 전달해야 함(사용할 때마다)
=> 100번 사용하면 100번 전달
DataSource와 같은 경우 3가지 파리미터를 처음 객체를 생성할 때만 필요한 파라미터를 넘기고,
커넥션을 획득할 때는 단순히 getConnection()을 호출만 하면 됨.

★설정과 사용의 분리
 */