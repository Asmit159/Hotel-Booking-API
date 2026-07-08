package hotel_booking.com.backend.repository;

import hotel_booking.com.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.roomId = :roomId AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    long countOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
