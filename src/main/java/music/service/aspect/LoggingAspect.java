package music.service.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* music.service.service.*.*(..))")
    public void logMethodCall(JoinPoint joinPoint) {
        logger.info("Method called: {}", joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(pointcut = "execution(* music.service.service.*.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception in method: {}", joinPoint.getSignature().toShortString(), ex);
    }
}