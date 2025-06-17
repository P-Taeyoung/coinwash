package pp.coinwash.laundry.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryRegisterDto;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryUpdateDto;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.laundry.domain.repository.LaundryRepository;

@ExtendWith(MockitoExtension.class)
class LaundryManageServiceTest {

	@Mock
	private LaundryRepository laundryRepository;

	@InjectMocks
	private LaundryManageService laundryManageService;

	private Laundry laundry;
	private Laundry laundry2;
	private LaundryRegisterDto registerDto;
	private LaundryUpdateDto updateDto;
	private LaundryResponseDto responseDto;


	@BeforeEach
	void setUp() {
		GeometryFactory geometryFactory = new GeometryFactory();

		laundry = Laundry.builder()
			.laundryId(1)
			.addressName("동대문구 휘경동")
			.location(geometryFactory.createPoint(new Coordinate(127.058338, 37.589288)))
			.description("가나다라마바사")
			.opened(false)
			.build();

		laundry2 = Laundry.builder()
			.laundryId(2)
			.addressName("동대문구 회기동")
			.location(geometryFactory.createPoint(new Coordinate(127.058338, 37.589288)))
			.description("나냐너녀노뇨")
			.opened(true)
			.build();

		registerDto = LaundryRegisterDto.builder()
			.addressName("동대문구 회기동")
			.latitude(37.600231)
			.longitude(127.054443)
			.description("아자차카타파하")
			.build();

		updateDto = LaundryUpdateDto.builder()
			.description("아야어여우유")
			.build();

		responseDto = LaundryResponseDto.builder()
			.laundryId(1)
			.ownerId(1)
			.addressName("동대문구 중랑구")
			.latitude(37.595040)
			.longitude(127.078684)
			.opened(false)
			.description("가갸거겨고교")
			.build();
	}

	@DisplayName("세탁방 등록")
	@Test
	void registerLaundry() {
		//given
		long ownerId = 1;
		when(laundryRepository.existsWithinDistance(
			registerDto.longitude(), registerDto.latitude(), 500))
			.thenReturn(false);

		//when
		laundryManageService.registerLaundry(registerDto, ownerId);

		//then
		ArgumentCaptor<Laundry> laundryCaptor = ArgumentCaptor.forClass(Laundry.class);
		verify(laundryRepository).existsWithinDistance( registerDto.longitude(), registerDto.latitude(), 500);
		verify(laundryRepository, times(1)).save(laundryCaptor.capture());

		Laundry savedLaundry = laundryCaptor.getValue();
		assertThat(savedLaundry.getAddressName()).isEqualTo(registerDto.addressName());
		assertThat(savedLaundry.getDescription()).isEqualTo(registerDto.description());
		assertThat(savedLaundry.isOpened()).isFalse();

		// Point 객체의 좌표값 직접 비교
		Point location = savedLaundry.getLocation();
		assertThat(location.getX()).isCloseTo(registerDto.longitude(), Offset.offset(0.000001));
		assertThat(location.getY()).isCloseTo(registerDto.latitude(), Offset.offset(0.000001));

	}

	@DisplayName("점주 세탁방 조회")
	@Test
	void getLaundriesByOwnerId() {
		//given
		long ownerId = 1;
		Pageable pageable = PageRequest.of(0, 10);
		List<Laundry> laundries = List.of(laundry);
		Page<Laundry> laundryPage = new PageImpl<>(laundries, pageable, laundries.size());
		when(laundryRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageable))
			.thenReturn(laundryPage);

		//when
		PagedResponseDto<LaundryResponseDto> result = laundryManageService.getLaundriesByOwnerId(ownerId, pageable);

		//then
		verify(laundryRepository, times(1)).findByOwnerIdAndDeletedAtIsNull(ownerId, pageable);
		assertEquals(PagedResponseDto.from(laundryPage.map(LaundryResponseDto::from)), result);
		assertEquals(LaundryResponseDto.from(laundry), result.data().get(0));
	}

	@DisplayName("세탁방 정보 수정")
	@Test
	void updateLaundry() {
		//given
		long ownerId = 1;
		long laundryId = 1;
		when(laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId))
			.thenReturn(Optional.ofNullable(laundry));

		//when
		laundryManageService.updateLaundry(laundryId, ownerId, updateDto);

		//then
		verify(laundryRepository, times(1))
			.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId);

		assertEquals(updateDto.description(), laundry.getDescription());
	}

	@DisplayName("세탁방 정보 삭제")
	@Test
	void deleteLaundry() {
		//given
		long ownerId = 1;
		long laundryId = 1;
		when(laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId))
			.thenReturn(Optional.ofNullable(laundry));

		//when
		laundryManageService.deleteLaundry(laundryId, ownerId);

		//then
		verify(laundryRepository, times(1))
			.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId);

		assertNotNull(laundry.getDeletedAt());
	}

	@DisplayName("세탁방 운영 현황 변경")
	@Test
	void changeLaundryStatus() {
		//given
		long ownerId = 1;
		long laundryId = 1;
		when(laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId))
			.thenReturn(Optional.ofNullable(laundry));

		//when
		laundryManageService.changeLaundryStatus(laundryId, ownerId);

		//then
		verify(laundryRepository, times(1))
			.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId);

		assertTrue(laundry.isOpened());
	}
}