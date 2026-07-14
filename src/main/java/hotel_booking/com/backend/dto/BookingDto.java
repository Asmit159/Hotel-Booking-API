package hotel_booking.com.backend.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class BookingDto {
    @NotNull(message = "Room ID is mandatory")
    private Long roomId;

    @NotBlank(message = "Guest name cannot be empty")
    private String guestName;

    @NotNull(message = "Check-in date is mandatory")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is mandatory")
    @FutureOrPresent(message = "Check-out date cannot be in the past")
    private LocalDate checkOutDate;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
