package pp.coinwash.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pp.coinwash.user.domain.dto.OwnerResponseDto;
import pp.coinwash.user.domain.dto.OwnerSignUpDto;
import pp.coinwash.user.domain.dto.OwnerUpdateDto;
import pp.coinwash.user.domain.entity.Owner;
import pp.coinwash.user.domain.repository.OwnerRepository;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

	@Mock
	private OwnerRepository ownerRepository;

	@InjectMocks
	private OwnerService ownerService;

	private Owner owner;
	private OwnerUpdateDto updateDto;
	private OwnerSignUpDto signUpDto;

	@BeforeEach
	void setUp() {

		signUpDto = OwnerSignUpDto.builder()
			.id("qwe123")
			.password("1234")
			.name("이순신")
			.phone("010-2222-3333")
			.build();

		updateDto = OwnerUpdateDto.builder()
			.phone("010-1111-2222")
			.build();

		owner = Owner.builder()
			.ownerId(2)
			.loginId("qwer1234")
			.password("1234")
			.name("세종대왕")
			.phone("010-2222-5555")
			.build();

	}

	@DisplayName("점주 회원가입")
	@Test
	void ownerSignUp() {
		//given
		when(ownerRepository.existsByLoginIdAndDeletedAtIsNull(signUpDto.id())).thenReturn(false);

		//when
		ownerService.ownerSignUp(signUpDto);

		//then
		verify(ownerRepository, times(1)).existsByLoginIdAndDeletedAtIsNull(signUpDto.id());
		verify(ownerRepository, times(1)).save(argThat(owner ->
			owner.getLoginId().equals(signUpDto.id()) &&
			owner.getPhone().equals(signUpDto.phone()) &&
			owner.getName().equals(signUpDto.name())
		));

	}

	@DisplayName("점주 정보 조회")
	@Test
	void getOwner() {
		//given
		when(ownerRepository.findByOwnerIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(owner));
		//when
		OwnerResponseDto result = ownerService.getOwner(1L);
		//then
		assertNotNull(result);
		verify(ownerRepository, times(1)).findByOwnerIdAndDeletedAtIsNull(1L);
		assertEquals(OwnerResponseDto.from(owner), result);

	}

	@DisplayName("점주 정보 수정")
	@Test
	void updateOwner() {
		//given
		when(ownerRepository.findByOwnerIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(owner));
		//when
		ownerService.updateOwner(1, updateDto);
		//then
		verify(ownerRepository, times(1)).findByOwnerIdAndDeletedAtIsNull(1L);

		assertEquals(updateDto.phone(), owner.getPhone());
	}

	@DisplayName("점주 탈퇴")
	@Test
	void deleteOwner() {
		//given
		when(ownerRepository.findByOwnerIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(owner));
		//when
		ownerService.deleteOwner(1L);
		//then
		verify(ownerRepository, times(1)).findByOwnerIdAndDeletedAtIsNull(1L);

		assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
			owner.getDeletedAt().truncatedTo(ChronoUnit.SECONDS));
	}
}