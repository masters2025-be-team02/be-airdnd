package kr.kro.airbob.domain.auth;

import java.time.Duration;
import java.util.Optional;
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
    private final SessionRedisRepository sessionRedisRepository;

    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        if (!BCrypt.checkpw(password, member.getPassword())) {
            throw new InvalidPasswordException();
        }

        String sessionId = UUID.randomUUID().toString();
        sessionRedisRepository.saveSession(sessionId, member.getId());

        return sessionId;
    }

    public void logout(String sessionId) {
        sessionRedisRepository.deleteSession(sessionId);
    }

    public Optional<Long> getMemberIdFromSession(String sessionId) {
        return sessionRedisRepository.getMemberIdBySession(sessionId);
    }
}
