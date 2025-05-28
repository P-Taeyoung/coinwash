package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WashingCourse {
	WASHING_A_COURSE(30),
	WASHING_B_COURSE(45),
	WASHING_C_COURSE(60);

	final int courseTime;
}
