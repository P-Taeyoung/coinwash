package pp.coinwash.user.domain.dto;

public record CustomerUpdateDto(
	String phone,
	String address,
	Long latitude,
	Long longitude
) {}
