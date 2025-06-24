package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV100", 10000);
        repository.save(member);

        // findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        log.info("member == findMember {}", member == findMember);
        log.info("member equals findMember {}", member.equals(findMember));

        assertThat(findMember).isEqualTo(member);
        /*
        Why?
        lombok의 @Data를 사용하면 EqualsAndHashCode를 자동으로 만들어 줌.
        모든 필드를 가지고...(ex. memberId, money)
        assertThat, isEqualTo도 내부에서 equals를 써서 비교하기 때문에 True로 나오는 것.
         */

        // update: money: 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        // delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
        // Member deleteMember = repository.findById(member.getMemberId());
        /*
        삭제를 하면 데이터 사라짐 -> NoSuchElementException 터지도록 조회 기능에서 설정해놓음
        NoSuchElementException 터지면 -> 데이터가 없으니까 삭제 성공.
         */
    }

}