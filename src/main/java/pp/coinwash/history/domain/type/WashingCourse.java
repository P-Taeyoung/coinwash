package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WashingCourse {
	WASHING_A_COURSE(10, 400),
	WASHING_B_COURSE(15, 500),
	WASHING_C_COURSE(20, 600);

	final int courseTime;
	final int fee;
}
