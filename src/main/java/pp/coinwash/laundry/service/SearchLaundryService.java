package pp.coinwash.laundry.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.domain.repository.LaundryRepository;

@Service
@RequiredArgsConstructor
public class SearchLaundryService {

	private final LaundryRepository laundryRepository;

	public List<LaundryResponseDto> findLaundriesNearBy(double longitude, double latitude, double distance) {

		return laundryRepository.findLaundriesNearBy(longitude, latitude, distance)
			.stream().map(laundry -> LaundryResponseDto.fromWithDistance(laundry, latitude, longitude)).collect(Collectors.toList());
	}

}
