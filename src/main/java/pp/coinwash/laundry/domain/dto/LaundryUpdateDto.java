package pp.coinwash.laundry.domain.dto;

import lombok.Builder;

@Builder
public record LaundryUpdateDto (
	String description
){
}
