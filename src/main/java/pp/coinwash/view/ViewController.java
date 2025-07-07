package pp.coinwash.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

	@GetMapping("/")
	public String index() {
		return "index";
	}

	// 인증 관련
	@GetMapping("/auth/signin")
	public String login() {
		return "auth/signin";
	}

	@GetMapping("/customer/signup")
	public String customerSignup() {
		return "customer/signup";
	}

	@GetMapping("/customer/point")
	public String ownerSignup() {
		return "customer/point";
	}

	// 고객 페이지
	@GetMapping("/customer/laundries")
	public String customerDashboard() {
		return "customer/laundries";
	}

	@GetMapping("/customer/machines")
	public String customerLaundries() {
		return "customer/machines";
	}

	@GetMapping("/customer/history")
	public String customerMachines() {
		return "customer/history";
	}

	@GetMapping("/owner/laundries")
	public String customerPoint() {
		return "owner/laundries";
	}

	@GetMapping("/owner/machines")
	public String customerHistory() {
		return "owner/machines";
	}

	@GetMapping("/owner/signup")
	public String ownerDashboard() {
		return "owner/signup";
	}

	@GetMapping("/customer/profile")
	public String customerProfile() {
		return "customer/profile";
	}

	@GetMapping("/owner/profile")
	public String ownerProfile() {
		return "owner/profile";
	}
}
