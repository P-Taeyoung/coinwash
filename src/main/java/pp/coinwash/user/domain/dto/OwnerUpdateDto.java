package pp.coinwash.user.domain.dto;

import lombok.Builder;

@Builder
public record OwnerUpdateDto(
	String phone
) {
}
