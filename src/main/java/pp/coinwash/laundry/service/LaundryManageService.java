package pp.coinwash.laundry.service;

import static pp.coinwash.common.exception.ErrorCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
import pp.coinwash.laundry.domain.dto.LaundryRegisterDto;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryUpdateDto;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.laundry.domain.repository.LaundryRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LaundryManageService {

	private final LaundryRepository laundryRepository;

	public void registerLaundry(LaundryRegisterDto dto, long ownerId) {
		isAbleToRegister(dto);
		laundryRepository.save(Laundry.of(dto, ownerId));
	}

	public List<LaundryResponseDto> getLaundriesByOwnerId(long ownerId) {
		return laundryRepository.findByOwnerIdAndDeletedAtIsNull(ownerId).stream().map(LaundryResponseDto::from).collect(
			Collectors.toList());
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
			.orElseThrow(() -> new CustomException(NO_AUTHORIZED_LAUNDRY_FOUND));
	}

	private void isAbleToRegister(LaundryRegisterDto dto) {
		if(laundryRepository.existsWithinDistance(dto.longitude(), dto.latitude(), 500)) {
			throw new CustomException(LAUNDRY_ALREADY_EXISTS_NEARBY);
		}
	}
}
