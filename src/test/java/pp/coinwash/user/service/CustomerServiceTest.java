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
import org.springframework.security.crypto.password.PasswordEncoder;

import pp.coinwash.security.JwtProvider;
import pp.coinwash.security.dto.UserAuthDto;
import pp.coinwash.user.domain.dto.CustomerResponseDto;
import pp.coinwash.user.domain.dto.CustomerSignInDto;
import pp.coinwash.user.domain.dto.CustomerSignUpDto;
import pp.coinwash.user.domain.dto.CustomerUpdateDto;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private CustomerService customerService;


	private CustomerSignInDto signInDto;
	private CustomerSignUpDto signUpDto;
	private CustomerUpdateDto updateDto;
	private Customer customer;

	@BeforeEach
	void setUp() {

		signInDto = CustomerSignInDto.builder()
			.customerSignInId("qwe123")
			.password("1234")
			.build();

		signUpDto = CustomerSignUpDto.builder()
			.id("id")
			.name("홍길동")
			.password("1234")
			.phone("010-1234-5678")
			.address("동대문구")
			.latitude(123.12)
			.longitude(124.12)
			.build();

		customer = Customer.builder()
			.customerId(1L)
			.loginId("loginId")
			.name("이순신")
			.phone("010-1111-2222")
			.address("중랑구")
			.latitude(123.12)
			.longitude(124.12)
			.points(0)
			.build();

		updateDto = CustomerUpdateDto.builder()
			.phone("010-1234-4321")
			.address("종로구")
			.latitude(123.12)
			.longitude(124.12)
			.build();

	}

	@DisplayName("로그인")
	@Test
	void signIn() {
		//given
		when(customerRepository.findByLoginIdAndDeletedAtIsNull(signInDto.customerSignInId()))
			.thenReturn(Optional.of(customer));
		when(passwordEncoder.matches(signInDto.password(), customer.getPassword()))
			.thenReturn(true);

		//when
		customerService.signIn(signInDto);

		//then
		verify(jwtProvider, times(1)).generateToken(UserAuthDto.fromCustomer(customer));
	}

	@DisplayName("회원가입 성공")
	@Test
	void signUp() {
		//given
		when(customerRepository.existsByLoginIdAndDeletedAtIsNull(signUpDto.id())).thenReturn(false);

		//when
		customerService.signUp(signUpDto);

		//then
		verify(customerRepository, times(1)).existsByLoginIdAndDeletedAtIsNull(signUpDto.id());
		verify(customerRepository).save(argThat(customer ->
			customer.getLoginId().equals(signUpDto.id()) &&
				customer.getName().equals(signUpDto.name()) &&
				customer.getPhone().equals(signUpDto.phone()) &&
				customer.getAddress().equals(signUpDto.address()) &&
				customer.getLatitude().equals(signUpDto.latitude()) &&
				customer.getLongitude().equals(signUpDto.longitude())
		));
	}

	@DisplayName("회원 정보 조회")
	@Test
	void getCustomer() {
		//given
		when(customerRepository.findByCustomerIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(customer));

		//when
		CustomerResponseDto result = customerService.getCustomer(1L);

		//then
		assertNotNull(result);
		verify(customerRepository, times(1)).findByCustomerIdAndDeletedAtIsNull(1L);
		assertEquals(CustomerResponseDto.from(customer), result);
	}

	@DisplayName("회원정보 수정")
	@Test
	void updateCustomer() {
		//given
		when(customerRepository.findByCustomerIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(customer));

		// when
		customerService.updateCustomer(1L, updateDto);

		// then
		verify(customerRepository).findByCustomerIdAndDeletedAtIsNull(1L);


		assertEquals(updateDto.phone(), customer.getPhone());
		assertEquals(updateDto.address(), customer.getAddress());
		assertEquals(updateDto.latitude(), customer.getLatitude());
		assertEquals(updateDto.longitude(), customer.getLongitude());
	}

	@DisplayName("회원 탈퇴")
	@Test
	void deleteCustomer() {
		//given
		when(customerRepository.findByCustomerIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(customer));

		//when
		customerService.deleteCustomer(1L);

		//then
		verify(customerRepository, times(1)).findByCustomerIdAndDeletedAtIsNull(1L);

		// 실제 삭제일자 데이터를 비교하기 위해서는 나노초는 빼고 비교해야 함.
		assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
			customer.getDeletedAt().truncatedTo(ChronoUnit.SECONDS));
	}
}