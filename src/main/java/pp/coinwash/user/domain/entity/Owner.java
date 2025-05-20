package pp.coinwash.user.domain.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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
import pp.coinwash.user.domain.dto.OwnerUpdateDto;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Owner extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long ownerId;

	private String loginId;
	private String password;
	private String name;
	private String phone;

	private LocalDateTime deletedAt;

	public static Owner of(OwnerSignUpDto dto) {
		return Owner.builder()
			.loginId(dto.id())
			.password(BCrypt.hashpw(dto.password(),BCrypt.gensalt()))
			.name(dto.name())
			.phone(dto.phone())
			.build();
	}

	public void update(OwnerUpdateDto dto) {
		this.phone = dto.phone();
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}
}
