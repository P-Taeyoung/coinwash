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
	String description,
	Double distance  // 거리 필드 추가
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

	public static LaundryResponseDto fromWithDistance(Laundry laundry, double userLat, double userLon) {
		double distance = calculateDistance(userLat, userLon,
			laundry.getLocation().getY(),
			laundry.getLocation().getX());

		return LaundryResponseDto.builder()
			.laundryId(laundry.getLaundryId())
			.ownerId(laundry.getOwnerId())
			.addressName(laundry.getAddressName())
			.latitude(laundry.getLocation().getY())
			.longitude(laundry.getLocation().getX())
			.opened(laundry.isOpened())
			.description(laundry.getDescription())
			.distance(distance)
			.build();
	}

	// 거리 계산 메서드 (Haversine 공식)
	private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		final double R = 6371; // 지구 반지름 (km)
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
				Math.sin(dLon/2) * Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return R * c * 1000; // 미터로 변환
	}
}
