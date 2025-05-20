package pp.coinwash.security.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

	private UserAuthDto userAuthDto;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		Collection<GrantedAuthority> authorities = new ArrayList<>();

		authorities.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return userAuthDto.role().name();
			}
		});

		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return userAuthDto.userName();
	}

	public Long getUserId() {
		return userAuthDto.userId();
	}
}
