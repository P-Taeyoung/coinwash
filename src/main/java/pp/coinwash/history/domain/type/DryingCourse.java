package pp.coinwash.history.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DryingCourse {
	DRYING_A_COURSE(10, 3000),
	DRYING_B_COURSE(15, 4000),
	DRYING_C_COURSE(20, 5000);

	final int courseTime;
	final int fee;
}
