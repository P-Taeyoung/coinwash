package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DryingCourse {
	DRYING_A_COURSE(10, 300),
	DRYING_B_COURSE(15, 400),
	DRYING_C_COURSE(20, 500);

	final int courseTime;
	final int fee;
}
