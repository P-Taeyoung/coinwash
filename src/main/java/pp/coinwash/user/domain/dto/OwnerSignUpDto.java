package pp.coinwash.user.domain.dto;

public record OwnerSignUpDto(
	String id,
	String password,
	String name,
	String phone
) {
}
