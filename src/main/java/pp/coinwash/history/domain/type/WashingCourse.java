package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WashingCourse {
	WASHING_A_COURSE(10, 4000),
	WASHING_B_COURSE(15, 5000),
	WASHING_C_COURSE(20, 6000);

	final int courseTime;
	final int fee;
}
