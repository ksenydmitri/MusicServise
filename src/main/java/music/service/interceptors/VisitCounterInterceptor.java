package music.service.interceptors;

import music.service.service.VisitCounterService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class VisitCounterInterceptor implements HandlerInterceptor {
    private final VisitCounterService visitCounterService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) {
        String url = request.getRequestURI();
        visitCounterService.incrementVisitCount(url);
    }
}