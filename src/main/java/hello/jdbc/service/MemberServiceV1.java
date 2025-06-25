package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
/*
    public MemberServiceV1(MemberRepositoryV1 memberRepository) {
        this.memberRepository = memberRepository;

    }*/
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 시작
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // 계좌이체 => from의 돈을 줄이고, to의 돈을 올리는 것
        memberRepository.update(fromId, fromMember.getMoney() - money); // UPDATE SQL 실행
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money); // UPDATE SQL 실행
        // 커밋, 롤백
    }

    // 예외 상황 테스트를 위해 toId가 "ex"인 경우 예외 발생
    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
