package pp.coinwash.user.domain.dto;

import lombok.Builder;
import pp.coinwash.user.domain.entity.Customer;

@Builder
public record CustomerResponseDto(
	String id,
	String name,
	String phone,
	int points,
	String address,
	Double latitude,
	Double longitude) {

	public static CustomerResponseDto from(Customer customer) {
		return CustomerResponseDto.builder()
			.id(customer.getLoginId())
			.name(customer.getName())
			.phone(customer.getPhone())
			.points(customer.getPoints())
			.address(customer.getAddress())
			.latitude(customer.getLatitude())
			.longitude(customer.getLongitude())
			.build();
	}
}
