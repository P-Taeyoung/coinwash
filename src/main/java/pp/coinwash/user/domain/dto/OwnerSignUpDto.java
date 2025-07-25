package pp.coinwash.user.domain.dto;

import lombok.Builder;

@Builder
public record OwnerSignUpDto(
	String id,
	String password,
	String name,
	String phone
) {
}
