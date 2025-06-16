package kr.kro.airbob.domain.member;

import kr.kro.airbob.domain.member.dto.MemberRequestDto.SignupMemberRequestDto;
import kr.kro.airbob.domain.member.exception.DuplicatedEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public void createMember(SignupMemberRequestDto request) {
        if(memberRepository.existsByEmail(request.getEmail())){
            throw new DuplicatedEmailException();
        }

        String hashedPassword = encoder.encode(request.getPassword());

        Member member = Member.createMember(request, hashedPassword);
        memberRepository.save(member);
    }
}
