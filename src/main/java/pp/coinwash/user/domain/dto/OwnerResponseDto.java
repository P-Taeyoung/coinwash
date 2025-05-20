package pp.coinwash.user.domain.dto;

import lombok.Builder;
import pp.coinwash.user.domain.entity.Owner;

@Builder
public record OwnerResponseDto(
	String name,
	String phone
) {
	public static OwnerResponseDto from(Owner owner) {
		return OwnerResponseDto.builder()
			.name(owner.getName())
			.phone(owner.getPhone())
			.build();
	}
}
