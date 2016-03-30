package com.unifina.security;

import com.unifina.domain.security.SecUser;
import com.unifina.service.UnifinaSecurityService;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	private final UnifinaSecurityService unifinaSecurityService;
	private boolean lastAuthenticationMalformed = false;
	private boolean apiKeyPresent = false;

	public TokenAuthenticator(UnifinaSecurityService unifinaSecurityService) {
		this.unifinaSecurityService = unifinaSecurityService;
	}

	public SecUser authenticate(HttpServletRequest request) {
		String apiKey = parseAuthorizationHeader(request.getHeader("Authorization"));
		apiKeyPresent = apiKey != null;
		return apiKeyPresent ? unifinaSecurityService.getUserByApiKey(apiKey) : null;
	}

	public boolean lastAuthenticationMalformed() {
		return lastAuthenticationMalformed;
	}

	public boolean isApiKeyPresent() {
		return apiKeyPresent;
	}

	/**
	 * "Authorization: Token apiKey" => "apiKey"
	 */
	private String parseAuthorizationHeader(String s) {
		s = s == null ? null : s.trim();
		if (s != null && !s.isEmpty()) {
			String[] parts = s.split("\\s+");
			if (parts.length == 2 && parts[0].toLowerCase().equals("token")) {
				lastAuthenticationMalformed = false;
				return parts[1];
			} else {
				lastAuthenticationMalformed = true;
			}
 		}
		return null;
	}
}