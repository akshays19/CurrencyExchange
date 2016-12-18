package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security to authenticate user
 * 
 * @author Akshay
 * 
 */
@Service
public class SecurityService {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	private static final Logger logger = LoggerFactory
			.getLogger(SecurityService.class);


	/**
	 * Auto login method to generate authentication token
	 * 
	 * @param username
	 * @param password
	 */
	public void autologin(String username,
			String password) {
		UserDetails userDetails = null;
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
		try {
			userDetails = userDetailsService.loadUserByUsername(username);
			usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					userDetails, password, userDetails.getAuthorities());

			authenticationManager
					.authenticate(usernamePasswordAuthenticationToken);

			if (usernamePasswordAuthenticationToken.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(
						usernamePasswordAuthenticationToken);
				logger.debug("Auto login successful: " + username);
			}
		} catch (UsernameNotFoundException e) {
			// TODO Auto-generated catch block
			logger.debug("User name not found: " + e.getMessage());
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			logger.debug("Authentication Failed: " + e.getMessage());
		}

	}
}