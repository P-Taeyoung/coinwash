package pp.coinwash.laundry.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryRegisterDto;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryUpdateDto;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.laundry.domain.repository.LaundryRepository;

@Service
@RequiredArgsConstructor
public class LaundryManageService {

	private final LaundryRepository laundryRepository;

	public void registerLaundry(LaundryRegisterDto dto, long ownerId) {
		isAbleToRegister(dto);
		laundryRepository.save(Laundry.of(dto, ownerId));
	}

	public PagedResponseDto<LaundryResponseDto> getLaundriesByOwnerId(long ownerId, Pageable pageable) {
		return PagedResponseDto.from(laundryRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageable).map(LaundryResponseDto::from));
	}

	@Transactional
	public void updateLaundry(long laundryId, long ownerId, LaundryUpdateDto dto) {
		validateLaundry(laundryId, ownerId).update(dto);
	}

	@Transactional
	public void deleteLaundry(long laundryId, long ownerId) {
		validateLaundry(laundryId, ownerId).delete();
	}

	@Transactional
	public void changeLaundryStatus(long laundryId, long ownerId) {
		validateLaundry(laundryId, ownerId).changeLaundryStatus();
	}

	private Laundry validateLaundry (long laundryId, long ownerId) {
		return laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId)
			.orElseThrow(() -> new RuntimeException("해당하는 코인세탁방 정보를 찾을 수 없습니다."));
	}

	private void isAbleToRegister(LaundryRegisterDto dto) {
		if(laundryRepository.existsWithinDistance(dto.latitude(), dto.longitude(), 500)) {
			throw new RuntimeException("500m 내 이미 코인 세탁방이 존재합니다.");
		}
	}
}
