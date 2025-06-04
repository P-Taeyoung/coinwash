package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WashingCourse {
	WASHING_A_COURSE(30, 400),
	WASHING_B_COURSE(45, 500),
	WASHING_C_COURSE(60, 600);

	final int courseTime;
	final int fee;
}
