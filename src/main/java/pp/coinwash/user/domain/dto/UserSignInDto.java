package pp.coinwash.user.domain.dto;

import lombok.Builder;

@Builder
public record UserSignInDto(
	String signInId,
	String password
){
}
