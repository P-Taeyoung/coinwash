package pp.coinwash.address.dto;

import static pp.coinwash.address.dto.KakaoAddressResponse.*;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
  private String address;
  private String roadAddress;
  private double longitude;
  private double latitude;

  public static AddressDto from(Document document) {
    return AddressDto.builder()
        .address(document.getAddressName())
        .roadAddress(document.getRoadAddressName())
        .longitude(Double.parseDouble(document.getX()))
        .latitude(Double.parseDouble(document.getY()))
        .build();
  }

  public static List<AddressDto> fromList(List<Document> documents) {
    return documents.stream().map(AddressDto::from).collect(Collectors.toList());
  }

}
