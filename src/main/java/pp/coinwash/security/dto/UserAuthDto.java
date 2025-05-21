package pp.coinwash.security.dto;

import lombok.Builder;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.entity.Owner;

@Builder
public record UserAuthDto (
	Long userId,
	String userName,
	UserRole role
){
	public static UserAuthDto fromCustomer(Customer customer) {
		return UserAuthDto.builder()
			.userId(customer.getCustomerId())
			.userName(customer.getName())
			.role(UserRole.CUSTOMER)
			.build();
	}

	public static UserAuthDto fromOwner(Owner owner) {
		return UserAuthDto.builder()
			.userId(owner.getOwnerId())
			.userName(owner.getName())
			.role(UserRole.OWNER)
			.build();
	}
}
