package com.example.matchapi.common.security;

import com.example.matchapi.user.dto.UserRes;
import com.example.matchcommon.exception.NotUserActiveException;
import com.example.matchcommon.properties.JwtProperties;
import com.example.matchdomain.redis.entity.AccessToken;
import com.example.matchdomain.redis.entity.RefreshToken;
import com.example.matchdomain.redis.repository.AccessTokenRepository;
import com.example.matchdomain.redis.repository.RefreshTokenRepository;
import com.example.matchdomain.user.adaptor.UserAdaptor;
import com.example.matchdomain.user.entity.User;
import com.example.matchdomain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.example.matchdomain.common.model.Status.INACTIVE;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final UserAdaptor userAdaptor;


    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Key getRefreshKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getRefresh().getBytes(StandardCharsets.UTF_8));
    }


    public String createToken(Long userId) {
        Date now =new Date();

        final Key encodedKey = getSecretKey();

        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+jwtProperties.getAccessTokenSeconds()))
                .signWith(encodedKey)
                .compact();
    }

    public String createRefreshToken(Long userId){
        Date now=new Date();
        final Key encodedKey = getRefreshKey();

        return Jwts.builder()
                .setHeaderParam("type","refresh")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+jwtProperties.getRefreshTokenSeconds()))
                .signWith(encodedKey)
                .compact();
    }

    public UserRes.Token createTokens(Long userId){
        Date now=new Date();
        final Key encodedAccessKey = getSecretKey();

        final Key encodedRefreshKey = getRefreshKey();

        String accessToken = Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+jwtProperties.getAccessTokenSeconds()))
                .signWith(encodedAccessKey)
                .compact();

        String refreshToken= Jwts.builder()
                .setHeaderParam("type","refresh")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+jwtProperties.getRefreshTokenSeconds()))
                .signWith(encodedRefreshKey)
                .compact();

        refreshTokenRepository.save(RefreshToken.builder().userId(userId.toString()).token(refreshToken).ttl(jwtProperties.getRefreshTokenSeconds()).build());

        return new UserRes.Token(accessToken,refreshToken);
    }

    public Authentication getAuthentication(String token, ServletRequest servletRequest)  {
        try {
            Jws<Claims> claims;

            claims = Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .parseClaimsJws(token);

            Long userId=claims.getBody().get("userId",Long.class);
            Optional<User> users = userAdaptor.findByUserId(userId);

            if(users.isEmpty()){
                throw new NoSuchElementException("NOT EXISTS USER");
            }
            if(users.get().getStatus().equals(INACTIVE)){
                throw new NotUserActiveException("NOT ACTIVE USER");
            }

            return new UsernamePasswordAuthenticationToken(users.get(),"",users.get().getAuthorities());
        }catch(NoSuchElementException e){
            servletRequest.setAttribute("exception","NoSuchElementException");
            log.info("유저가 존재하지 않습니다.");
        }catch (NotUserActiveException e){
            servletRequest.setAttribute("exception","NotUserActiveException");
            log.info("유저가 비활성 상태입니다.");
        }
        return null;
    }



    public boolean validateToken(ServletRequest servletRequest, String token) {
        try {
            Jws<Claims> claims;
            claims = Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .parseClaimsJws(token);

            Long userId = claims.getBody().get("userId",Long.class);


            Optional<AccessToken> accessToken = accessTokenRepository.findById(token);


            if(accessToken.isPresent()){
                if(accessToken.get().getToken().equals(token)){
                    servletRequest.setAttribute("exception","HijackException");
                    return false;
                }
            }
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            servletRequest.setAttribute("exception","MalformedJwtException");
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            servletRequest.setAttribute("exception","ExpiredJwtException");
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            servletRequest.setAttribute("exception","UnsupportedJwtException");
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            servletRequest.setAttribute("exception","IllegalArgumentException");
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public String getJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader(JwtFilter.AUTHORIZATION_HEADER);
    }

    public String getRefreshToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader(JwtFilter.REFRESH_TOKEN_HEADER);
    }

    public Date getExpiredTime(String token){
        //받은 토큰의 유효 시간을 받아오기
        return Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token).getBody().getExpiration();
    }

    public Long getUserIdByRefreshToken(String refreshToken) {
        Jws<Claims> claims =
                Jwts.parser()
                        .setSigningKey(getRefreshKey())
                        .parseClaimsJws(refreshToken);

        System.out.println(claims.getBody().get("userId",Long.class));

        return claims.getBody().get("userId",Long.class);
    }

    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }


    public void logOut(Long userId) {
        long expiredAccessTokenTime=getExpiredTime(getJwt()).getTime() - new Date().getTime();
        log.info(String.valueOf(expiredAccessTokenTime));
        accessTokenRepository.save(AccessToken.builder().token(getJwt()).userId(String.valueOf(userId)).ttl(expiredAccessTokenTime).build());
    }
}
