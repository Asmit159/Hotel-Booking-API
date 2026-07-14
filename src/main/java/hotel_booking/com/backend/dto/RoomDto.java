package hotel_booking.com.backend.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class RoomDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roomId;
    private String roomType;
    private String roomNumber;
    private BigDecimal pricePerNight;

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }


}

