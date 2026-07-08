package hotel_booking.com.backend.repository;

import hotel_booking.com.backend.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
            "(SELECT b.roomId FROM Booking b WHERE b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)")
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);
}
