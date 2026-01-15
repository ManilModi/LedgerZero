package org.example.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.dataModel.UserDeviceData;

import java.util.Date;
import java.util.List;

public class JwtUtil {


    public static String generateJWTToken(String SECRET_KEY,int exTime, Long userId, String phoneNumber,String fullName, List<UserDeviceData> devices) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("phone", phoneNumber)
                .claim("devices", devices)
                .claim("fullName", fullName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exTime))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
