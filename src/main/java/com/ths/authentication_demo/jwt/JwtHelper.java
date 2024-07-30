package com.ths.authentication_demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtHelper {

//  private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//  private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

  private static final String secret = "THSJavaEnterpriseSolution140223===========================================THSJavaEnterpriseSolution140223===========================================================THSJavaEnterpriseSolution140223";

  private static final byte[] keyBytes = Decoders.BASE64.decode(secret);

  private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);


  private static final int MINUTES = 60;
  private static final Logger log = LoggerFactory.getLogger(JwtHelper.class);

  public static String generateToken(String email) {
    var now = Instant.now();
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
        .claims(claims)
        .subject(email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(MINUTES, ChronoUnit.MINUTES)))
//        .signWith(SignatureAlgorithm.HS512, secret)
//        .signWith(SECRET_KEY)
        .signWith(SECRET_KEY, Jwts.SIG.HS512)
        .compact();
  }

  public static String extractUsername(String token) {
    return getTokenBody(token).getSubject();
  }

  public static Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    log.info("Username : {}", username);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  private static Claims getTokenBody(String token) {
    try {
      return Jwts
          .parser()
          .verifyWith(SECRET_KEY)
//          .setSigningKey(secret)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (SignatureException | ExpiredJwtException e) { // Invalid signature or expired token
      throw new RuntimeException("Access denied: " + e.getMessage());
    }
  }

  private static boolean isTokenExpired(String token) {
    Claims claims = getTokenBody(token);
    return claims.getExpiration().before(new Date());
  }
}