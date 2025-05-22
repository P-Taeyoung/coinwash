package pp.coinwash.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PagedResponseDto<T>(
	List<T> data,
	PaginationDto pagination
) {
	public static <T> PagedResponseDto<T> from(Page<T> page) {
		return new PagedResponseDto<>(page.getContent(), PaginationDto.from(page));
	}
}
