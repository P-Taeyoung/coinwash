package pp.coinwash.user.domain.entity;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.user.domain.dto.OwnerSignUpDto;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Owner extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long ownerId;

	private String id;
	private String password;
	private String name;
	private String phone;

	public static Owner of(OwnerSignUpDto dto) {
		return Owner.builder()
			.id(dto.id())
			.password(BCrypt.hashpw(dto.password(),BCrypt.gensalt()))
			.name(dto.name())
			.phone(dto.phone())
			.build();
	}
}
