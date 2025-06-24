package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
    /*
    데이터베이스에 연결하려면 JDBC가 제공하는 DriveManager.getConnection(...)
    를 사용하면 된다. 이렇게 하면 라이브러리에 있는 데이터베이스 드라이버를 찾아
    해당 드라이버가 제공하는 커넥션을 반환해준다. 여기서는 H2 데이터베이스 드라이버가
    작동해서 실제 데이터베이스와 커넥션을 맺고 그 결과를 반환해준다.
     */

    // jdbc:h2:tcp://localhost/~/test
    /*
    예를 들어 URL이 jdbc:h2로 시작하면 이것은 h2 데이터베이스에 접근하기 위한 규칙
    따라서 H2 드라이버는 보인이 처리할 수 있으므로 실제 데이터베이스에 연결해서
    커넥션을 획득하고 이 커넥션을 클라이언트에 반환
    반면에 URL이 jdbc:h2로 시작했는데 MySQL드라이버가 먼저 실행되면 이 경우 본인이
    처리할 수 없다는 결과를 반환하게 되고, 다음 드라이버에게 순서가 넘어간다.
     */

}
