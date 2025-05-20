package pp.coinwash.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.user.domain.dto.OwnerResponseDto;
import pp.coinwash.user.domain.dto.OwnerSignUpDto;
import pp.coinwash.user.domain.dto.OwnerUpdateDto;
import pp.coinwash.user.domain.entity.Owner;
import pp.coinwash.user.domain.repository.OwnerRepository;

@Service
@RequiredArgsConstructor
public class OwnerService {

	private final OwnerRepository ownerRepository;

	public void ownerSignUp(OwnerSignUpDto dto) {
		validateId(dto.id());
		ownerRepository.save(Owner.of(dto));
	}

	public OwnerResponseDto getOwner(long ownerId) {
		return OwnerResponseDto.from(validateCustomer(ownerId));
	}

	@Transactional
	public void updateOwner(long ownerId, OwnerUpdateDto dto) {
		validateCustomer(ownerId).update(dto);
	}

	@Transactional
	public void deleteOwner(long ownerId) {
		validateCustomer(ownerId).delete();
	}

	private void validateId(String Id) {
		if (ownerRepository.existsByLoginIdAndDeletedAtIsNull(Id)) {
			throw new RuntimeException("이미 존재하는 아이디입니다.");
		}
	}

	private Owner validateCustomer(long customerId) {
		return ownerRepository.findByOwnerIdAndDeletedAtIsNull(customerId)
			.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
	}
}
