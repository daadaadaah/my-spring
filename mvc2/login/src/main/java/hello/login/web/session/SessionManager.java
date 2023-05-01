package hello.login.web.session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager는 세션을 관리해준다
 */
public class SessionManager {

    public static final String SESSION_COOKE_NAME = "mySessionId";
    private Map<String, Object> sessionStore = new ConcurrentHashMap<>();

    /**
     * 세션 생성
     * 1. sessionId 생성 (임의의 추정 불가능한 랜덤값)
     * 2. 세션 저장소에 보관할 sessionId 와 값 저장
     * 3. sessionId로 Cookie 생성
     * 4. 클라이언트에 만들어진 Cookie 전달
     */
    public void createSession(Object value, HttpServletResponse response) {

        // 1. sessionId 생성
        String seestionId = UUID.randomUUID().toString();

        // 2. 세션 저장소에 보관할 sessionId 와 값 저장
        sessionStore.put(seestionId, value);

        // 3. sessionId로 Cookie 생성
        Cookie mySessionCookie = new Cookie(SESSION_COOKE_NAME, seestionId);

        // 4. 클라이언트에 만들어진 Cookie 전달
        response.addCookie(mySessionCookie);
    }

    /**
     * 세션 조회
     */
    public Object getSession(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, SESSION_COOKE_NAME);

        if(sessionCookie == null) {
            return null;
        }

        return sessionStore.get(sessionCookie.getValue());
    }

    /**
     * 세션 만료
     */
    public void expire(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, SESSION_COOKE_NAME);
        if(sessionCookie != null) {
            sessionStore.remove(sessionCookie.getValue());
        }
    }

    public Cookie findCookie(HttpServletRequest request, String cookieName) {
        if(request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElse(null);
    }
}
