package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - DataSource, transactionManager 자동 등록
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService;

    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource);
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }
/*
    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);
        memberService = new MemberServiceV3_3(memberRepository);
    }
*/
    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        // given: 이런 상황일때
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when: 이걸 수행하면
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");

        // then: 이렇게 검증해라
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        // given: 이런 상황일때
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when: 이걸 수행하면
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then: 이렇게 검증해라
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());

        // memberA의 돈이 롤백 되어야 함
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}
/*
정상 이체 - 동작 O, 이체 중 예외 발생 - 동작 X
-> Why? 트랜잭션 적용이 안됨(@Transactional 사용했는데 적용 안됨, 더 구체적으로 rollback이 안됨.)
=> Why? 이 테스트는 스프링 컨테이너를 전혀 사용하고 있지 않음.
        DriverManagerDataSource 직접 만들고,
        MemberRepositoryV3도 직접 만들고,
        MemberService3_3도 직접 만들어서 조립해서 사용 함
결과적으로 스프링 컨테이너에 스프링 빈을 등록해서 사용하는게 아니라
내가 원하는 것들만 넣어서 테스트를 한 테스트

그러나 Transactinal AOP를 적용하려면 스프링이 제공하는 것이 제공되어야 사용 가능

 */

/*
void AopCheck()

실행 시 : h.jdbc.service.MemberServiceV3_3Test --memberService class=class hello.jdbc.service.MemberServiceV3_3$$SpringCGLIB$$0
$$SpringCGLIB$$0 이 붙는 이유
=> Spring이 AOP 기능을 적용하기 위해 해당 클래스의 프록시(Proxy)를 동적으로 생성했기 때문

Why? 프록시 생성
=> Spring에서 AOP를 적용할 때, 핵심 로직 앞뒤로 부가기능 (예: 트랜잭션, 로깅 등)을 넣기 위해 프록시 객체를 생성해.
=> @Transactional 이 붙어 있으면?
   Spring은 원본 객체 대신 프록시 객체를 생성해서 빈으로 등록
   그 프록시는 트랜잭션 시작 → 원본 호출 → 커밋/롤백을 자동으로 처리

Why? 왜 CGLIB 프록시가 생성됐을까?
=> MemberServiceV3_3는 인터페이스 없이 클래스 자체만 존재하므로,
   Spring은 JDK 동적 프록시를 쓸 수 없고, 대신 CGLIB 기반 클래스 상속 프록시를 사용해.

=> 따라서 이런 이름이 생성 됨
   hello.jdbc.service.MemberServiceV3_3$$SpringCGLIB$$0
 */