package pp.coinwash.notification.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.notification.domain.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
