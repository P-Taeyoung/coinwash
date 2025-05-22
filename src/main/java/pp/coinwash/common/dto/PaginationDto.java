package pp.coinwash.common.dto;

import org.springframework.data.domain.Page;

public record PaginationDto(
	int currentPage,
	int limit,
	long totalItems,
	int totalPages,
	boolean hasNext,
	boolean hasPrevious
) {
	public static PaginationDto from(Page<?> page) {
		return new PaginationDto(
			page.getNumber() + 1, // 페이지 번호는 0부터 시작하므로 +1
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext(),
			page.hasPrevious()
		);
	}
}
