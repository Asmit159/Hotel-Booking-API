package hotel_booking.com.backend.service;

import hotel_booking.com.backend.dto.RoomDto;
import hotel_booking.com.backend.entity.Room;
import hotel_booking.com.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    // Constructor Injection
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @CacheEvict(value = "availableRooms", allEntries = true)
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = new Room();
        room.setRoomNumber(roomDto.getRoomNumber());
        room.setRoomType(roomDto.getRoomType());
        room.setPricePerNight(roomDto.getPricePerNight());

        Room savedRoom = roomRepository.save(room);

        RoomDto responseDto = new RoomDto();
        responseDto.setRoomId(savedRoom.getId());
        responseDto.setRoomNumber(savedRoom.getRoomNumber());
        responseDto.setRoomType(savedRoom.getRoomType());
        responseDto.setPricePerNight(savedRoom.getPricePerNight());

        return responseDto;
    }

    @Cacheable(value = "availableRooms", key = "#checkInDate.toString() + '-' + #checkOutDate.toString()")
    public List<RoomDto> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = roomRepository.findAvailableRooms(checkInDate, checkOutDate);

        return availableRooms.stream().map(room -> {
            RoomDto dto = new RoomDto();
            dto.setRoomId(room.getId());
            dto.setRoomNumber(room.getRoomNumber());
            dto.setRoomType(room.getRoomType());
            dto.setPricePerNight(room.getPricePerNight());
            return dto;
        }).collect(Collectors.toList());
    }
}