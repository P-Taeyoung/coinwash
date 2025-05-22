package pp.coinwash.laundry.domain.dto;

import lombok.Builder;
import pp.coinwash.laundry.domain.entity.Laundry;

@Builder
public record LaundryResponseDto(
	long laundryId,
	long ownerId,
	String addressName,
	double latitude,
	double longitude,
	boolean opened,
	String description
) {
	public static LaundryResponseDto from(Laundry laundry) {
		return LaundryResponseDto.builder()
			.laundryId(laundry.getLaundryId())
			.ownerId(laundry.getOwnerId())
			.addressName(laundry.getAddressName())
			.latitude(laundry.getLocation().getY())
			.longitude(laundry.getLocation().getX())
			.opened(laundry.isOpened())
			.description(laundry.getDescription())
			.build();

	}
}
