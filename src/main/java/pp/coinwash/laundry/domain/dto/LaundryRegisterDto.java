package pp.coinwash.laundry.domain.dto;

import lombok.Builder;

@Builder
public record LaundryRegisterDto(
	String addressName,
	double latitude,
	double longitude,
	String description
) {
}
