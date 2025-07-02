package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/*
 * JdbcTemplate 사용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    // JDBC 개발 - 등록(CRUD - C)
    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;
    }

    // JDBC 개발 - 조회(CRUD - R)
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return (Member) template.queryForObject(sql, memberRowMapper(), memberId);
    }

    // JDBC 개발 - 변경(CRUD - U)
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }
    // JDBC 개발 - 삭제(CRUD - D)

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }

    private RowMapper<Object> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }
}
/*
JDBC 반복 문제

- 커넥션 조회, 커넥션 동기화
- PreparedStatement 생성 및 파라미터 바인딩
- 쿼리 실행
- 결과 바인딩
- 예외 발생시 스프링 예외 변환기 실행
- 리소스 종료

=> 이런 반복을 효과적으로 처리하는 방법이 바로 템플릿 콜백 패턴이다
 */

/*
JdbcTemplate는 JDBC로 개발할 때 발생하는 반복을 대부분 해결해준다.
뿐만 아니라, 지금까지 학습한 트랜잭션을 위한 커넥션 동기화 +
예외 발생시, 스프링 예외 변환기도 자동으로 실행해준다.
 */