package kr.kro.airbob.domain.auth;

import java.time.Duration;
import java.util.UUID;
import kr.kro.airbob.domain.auth.exception.InvalidPasswordException;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        if (!BCrypt.checkpw(password, member.getPassword())) {
            throw new InvalidPasswordException();
        }

        String sessionId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("SESSION:" + sessionId, member.getId(), Duration.ofHours(1)); // TTL 설정

        return sessionId;
    }

    public void logout(String sessionId) {
        redisTemplate.delete("SESSION:" + sessionId);
    }

    public Long getMemberIdFromSession(String sessionId) {
        return (Long) redisTemplate.opsForValue().get("SESSION:" + sessionId);
    }
}
