package com.xlmkit.springboot.support.jwtsession;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.xlmkit.springboot.action.sdk.XJson;
import com.xlmkit.springboot.action.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.Getter;
import lombok.Setter;

public class JWTSessionSupport implements HandlerMethodArgumentResolver {

	private @Autowired ApplicationContext context;
	@Setter
	private String tokenName = "session_token";
	@Setter
	private String secret = UUID.randomUUID().toString();
	@Setter
	private int liveMinute = 60 * 12;
	@Setter
	private boolean force = true;
	@Setter
	private boolean enableFromHeader = false;
	@Setter
	private boolean enableFromJSONBody = false;
	@Getter
	@Setter
	private Map<Class<?>, Class<? extends AbsJWTSession>> componentClassMap = new HashMap<Class<?>, Class<? extends AbsJWTSession>>();

	public JWTSessionSupport() {
	}

	public JWTSessionSupport(Class<? extends AbsJWTSession> prototypeComponentClass) {
		componentClassMap.put(prototypeComponentClass, prototypeComponentClass);
	}

	public JWTSessionSupport(Class<? extends AbsJWTSession> prototypeComponentClass, boolean force) {
		componentClassMap.put(prototypeComponentClass, prototypeComponentClass);
		this.force = force;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return componentClassMap.containsKey(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest req = (HttpServletRequest) webRequest.getNativeRequest();
		String token = req.getParameter(tokenName);
		if (enableFromHeader && StringUtils.isEmpty(token)) {
			token = req.getHeader(tokenName);
		}
		JSONObject jsonBody = null;
		if (enableFromJSONBody && StringUtils.isEmpty(token)) {
			XJson json = HttpUtils.getXJson(req);
			if (json != null) {
				jsonBody = json.getJsonObject();
				token = json.getJsonObject().getString(tokenName);
			}
		}
		return resolveArgument(jsonBody,parameter, token);
	}

	public Object resolveArgument(JSONObject jsonBody, MethodParameter parameter, String token) {
		if (token == null) {
			if (force) {
				throw new JWTSessionValidateFailException("登录超时0");
			} else {
				return null;
			}
		}
		if (token.length() < 30) {
			if (force) {
				throw new JWTSessionValidateFailException("登录超时1");
			} else {
				return null;
			}
		}
		String sign = null;
		String id = null;
		String secret = null;
		AbsJWTSession session = null;
		if (token.startsWith("00")) {
			sign = token.substring(2);
			secret = this.secret;
		} else if (token.startsWith("01")) {
			String[] sp = token.split("--_--");
			if (sp.length != 2) {
				if (force) {
					throw new JWTSessionValidateFailException("登录超时2");
				} else {
					return null;
				}
			}
			id = sp[0].substring(2);
			sign = sp[1];

			Class<? extends AbsJWTSession> prototypeComponentClass = componentClassMap.get(parameter.getParameterType());

			session = context.getBean(prototypeComponentClass);
			secret = session.getSecret(id);
			if (StringUtils.isEmpty(secret)) {
				if (force) {
					throw new JWTSessionValidateFailException("登录超时3");
				} else {
					return null;
				}
			}
		} else {
			if (force) {
				throw new JWTSessionValidateFailException("登录超时4");
			} else {
				return null;
			}
		}
		String saveId;
		String data = null;
		try {
			DecodedJWT jwt = getKeyByToken(sign, secret);
			saveId = jwt.getId();
			Claim claim = jwt.getClaim("data");
			if (!(claim instanceof NullClaim)) {
				data = claim.asString();

			}

		} catch (Exception e) {
			if (force) {
				throw new JWTSessionValidateFailException("登录超时5");
			} else {
				return null;
			}
		}
		if (session == null) {
			Class<? extends AbsJWTSession> prototypeComponentClass = componentClassMap.get(parameter.getParameterType());
			session = context.getBean(prototypeComponentClass);
		}
		session.init(jsonBody,saveId, data);
		return session;
	}

	private static DecodedJWT getKeyByToken(String token, String secret) throws SignatureVerificationException,
			TokenExpiredException, IllegalArgumentException, UnsupportedEncodingException {

		Algorithm algorithm = Algorithm.HMAC256(secret);
		JWTVerifier verifier = JWT.require(algorithm).build(); // Reusable verifier instance
		DecodedJWT jwt = verifier.verify(token);
		return jwt;
	}

	public String createToken(Object id) {
		return "00" + createToken(id, liveMinute, secret);
	}

	public static String createToken(Object id, long liveMinute, String secret) {
		Date issueD = new Date();
		Date expD = new Date(issueD.getTime() + liveMinute * 60 * 1000);
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			Builder builder = JWT.create();
			builder.withJWTId(id.toString());
			builder.withIssuedAt(issueD);
			builder.withExpiresAt(expD);
			return builder.sign(algorithm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String createToken(Object id, String secret) {
		return "01" + id + "--_--" + createToken(id, liveMinute, secret);
	}

	public String createTokenWithData(Object id, Object data) {
		return "00" + createTokenWithData(id, liveMinute, secret, data);
	}

	public static String createTokenWithData(Object id, long liveMinute, String secret, Object data) {
		Date issueD = new Date();
		Date expD = new Date(issueD.getTime() + liveMinute * 60 * 1000);
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			Builder builder = JWT.create();
			builder.withJWTId(id.toString());
			builder.withIssuedAt(issueD);
			builder.withClaim("data", JSON.toJSONString(data));
			System.out.println( JSON.toJSONString(data));
			builder.withExpiresAt(expD);
			return builder.sign(algorithm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String createTokenWithData(Object id, String secret, Object data) {
		return "01" + id + "--_--" + createTokenWithData(id, liveMinute, secret, data);
	}

}
