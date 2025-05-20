package pp.coinwash.security.dto;

import lombok.Builder;

@Builder
public record UserAuthDto (
	Long userId,
	String userName,
	UserRole role
){
}
