package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DryingCourse {
	DRYING_A_COURSE(20),
	DRYING_B_COURSE(30),
	DRYING_C_COURSE(40);

	final int courseTime;
}
