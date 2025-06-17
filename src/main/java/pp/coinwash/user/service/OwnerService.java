package pp.coinwash.user.service;

import static pp.coinwash.common.exception.ErrorCode.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
import pp.coinwash.security.JwtProvider;
import pp.coinwash.security.dto.UserAuthDto;
import pp.coinwash.user.domain.dto.OwnerResponseDto;
import pp.coinwash.user.domain.dto.OwnerSignUpDto;
import pp.coinwash.user.domain.dto.OwnerUpdateDto;
import pp.coinwash.user.domain.dto.UserSignInDto;
import pp.coinwash.user.domain.entity.Owner;
import pp.coinwash.user.domain.repository.OwnerRepository;

@Service
@RequiredArgsConstructor
public class OwnerService {

	private final OwnerRepository ownerRepository;
	private final JwtProvider jwtProvider;
	private final PasswordEncoder passwordEncoder;

	public void ownerSignUp(OwnerSignUpDto dto) {
		existsId(dto.id());
		ownerRepository.save(Owner.of(dto, passwordEncoder));
	}

	public String ownerSignIn(UserSignInDto dto) {
		return jwtProvider.generateToken(UserAuthDto.fromOwner(validateId(dto)));
	}

	public OwnerResponseDto getOwner(long ownerId) {
		return OwnerResponseDto.from(validateOwner(ownerId));
	}

	@Transactional
	public void updateOwner(long ownerId, OwnerUpdateDto dto) {
		validateOwner(ownerId).update(dto);
	}

	@Transactional
	public void deleteOwner(long ownerId) {
		validateOwner(ownerId).delete();
	}

	private Owner validateId(UserSignInDto dto) {
		Owner owner = ownerRepository.findByLoginIdAndDeletedAtIsNull(dto.signInId())
			.orElseThrow(() -> new CustomException(INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(dto.password(), owner.getPassword())) {
			throw new CustomException(INVALID_CREDENTIALS);
		}

		return owner;
	}

	private void existsId(String Id) {
		if (ownerRepository.existsByLoginIdAndDeletedAtIsNull(Id)) {
			throw new CustomException(ALREADY_EXISTS_ID);
		}
	}

	private Owner validateOwner(long customerId) {
		return ownerRepository.findByOwnerIdAndDeletedAtIsNull(customerId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
	}
}
