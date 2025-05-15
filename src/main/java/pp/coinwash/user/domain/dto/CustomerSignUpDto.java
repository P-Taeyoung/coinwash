package pp.coinwash.user.domain.dto;

import lombok.Builder;

@Builder
public record CustomerSignUpDto(
	String id,
	String password,
	String name,
	String phone,
	String address,
	Double latitude,
	Double longitude
) {
}
