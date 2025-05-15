package pp.coinwash.user.domain.dto;

import lombok.Builder;

@Builder
public record CustomerUpdateDto(
	String phone,
	String address,
	Double latitude,
	Double longitude
) {}
