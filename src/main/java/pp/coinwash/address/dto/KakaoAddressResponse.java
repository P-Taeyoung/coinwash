package pp.coinwash.address.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAddressResponse {
    private Meta meta;
    private List<Document> documents;
    private SameName sameName;

    @Data
    public static class Meta {
      @JsonProperty("total_count")
      private int totalCount;
      @JsonProperty("pageable_count")
      private int pageableCount;
      @JsonProperty("is_end")
      private boolean isEnd;
      @JsonProperty("same_name")
      private SameName sameName;
    }

    @Data
    public static class SameName {
      @JsonProperty("region")
      private List<String> region;
      @JsonProperty("keyword")
      private String keyword;
      @JsonProperty("selected_region")
      private String selectedRegion;
    }

  @Data
  public static class Document {
    @JsonProperty("address_name")
    private String addressName;
    @JsonProperty("road_address_name")
    private String roadAddressName;
    @JsonProperty("x")
    private String x;  // 경도(Longitude)
    @JsonProperty("y")
    private String y;  // 위도(Latitude)
  }
}
