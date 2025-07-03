package kr.kro.airbob.common.lock.aspect;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import kr.kro.airbob.common.lock.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String evaluatedKey = getKey(joinPoint, method, distributedLock.key());
        String lockName = distributedLock.lockName() + "::" + evaluatedKey;

        RLock lock = redissonClient.getLock(lockName);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    TimeUnit.SECONDS
            );

            if (!isLocked) {
                throw new IllegalStateException("Lock 획득 실패: " + lockName);
            }

            return joinPoint.proceed();
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String getKey(ProceedingJoinPoint joinPoint, Method method, String keyExpression) {
        EvaluationContext context = new StandardEvaluationContext();

        Object[] args = joinPoint.getArgs();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
