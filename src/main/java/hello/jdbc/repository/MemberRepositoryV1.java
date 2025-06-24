package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/*
 * JDBC - DataSource 사용, JdbcUtils 사용
 */
@Slf4j
public class MemberRepositoryV1 {
    /* DI
    DriverManagerDataSource -> HikariDataSource로 변경해도 MemberRepositoryV1의 코드는
    전혀 변경하지 않아도 된다. MemberRepositoryV1는 DataSource 인터페이스에만 의존하기 때문이다.
    이것이 "DataSource"를 사용하는 장점이다. (DI + OCP)
    ★ "DataSource" => 커넥션을 획득하는 방법을 추상화
     */
    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // JDBC 개발 - 등록(CRUD - C)
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    // JDBC 개발 - 조회(CRUD - R)
    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    // JDBC 개발 - 변경(CRUD - U)
    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            /*  executeUpdate()
            executeUpdate()는 쿼리를 실행하고 영향받은 row수를 반환
            여기서는 하나의 데이터만 변경하기 때문에 결과로 1이 반환
            만약 회원이 100명이고, 모든 회원의 데이터를 한번에 수정하는 update sql을 실행하면 결과는 100
             */
            log.info("resultSize={}" + resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    // JDBC 개발 - 삭제(CRUD - D)
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db eroor", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, PreparedStatement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }

}
/*
DataSource 의존관계 주입
외부에서 DataSource를 주입 받아서 사용한다. 이제 직접 만든 DBConnectionUtil을 사용하지 않아도 된다.
DataSource는 표준 인터페이스 이기 때문에 DriverManagerDataSource에서 HikariDataSource로 변경되어도
해당 코드를 변경하지 않아도 된다.

JdbcUtils 편의 메서드
스프링은 JDBC를 편리하게 다룰 수 있는 JbcUtils라는 편의 메서드를 제공한다.
JdbcUtils를 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다.
 */