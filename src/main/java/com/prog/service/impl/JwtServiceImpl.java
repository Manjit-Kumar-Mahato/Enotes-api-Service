package com.prog.service.impl;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.prog.entity.User;
import com.prog.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService{
	
	private String secretKey = "";

	public JwtServiceImpl() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
			SecretKey sk = keyGen.generateKey();
			secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String generateToken(User user) {

		Map<String, Object> claims = new HashMap<>();
		claims.put("id", user.getId());
		claims.put("role", user.getRoles());
		claims.put("status", user.getStatus().getIsActive());

		String token = Jwts.builder().claims().add(claims)
				.subject(user.getEmail())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() +  50*60*1000))
				.and()
				.signWith(getKey())
				.compact();

		return token;
	}
	
	private Key getKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public String extractUsername(String token) {
		Claims claims = extractAllClaims(token);
		return claims.getSubject();
	}
	
	public String role(String token) {
		Claims claims = extractAllClaims(token);
		String role = (String)claims.get("role");
		return role;
	}

	private Claims extractAllClaims(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(decryKey(secretKey))
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims;
	}

	private SecretKey decryKey(String secretKey) {
		byte[] ketBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(ketBytes);
	}

	@Override
	public Boolean validateToken(String token, UserDetails userDetails) {
		String username =  extractUsername(token);
		Boolean isExpired = isTokenExpired(token);
		if(username.equalsIgnoreCase(userDetails.getUsername()) && !isExpired) {
			return true;
		}
		return false;
	}

	private Boolean isTokenExpired(String token) {
		Claims claims = extractAllClaims(token);
		Date expireDate = claims.getExpiration();
		return expireDate.before(new Date());
	}

	


}
