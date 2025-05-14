package pp.coinwash.user.domain.dto;

public record CustomerSignUpDto(
	String id,
	String password,
	String name,
	String phone,
	String address,
	Long latitude,
	Long longitude
) {
}
