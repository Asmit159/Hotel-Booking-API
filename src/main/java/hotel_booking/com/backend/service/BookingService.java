package hotel_booking.com.backend.service;
import hotel_booking.com.backend.dto.BookingDto;
import hotel_booking.com.backend.entity.Booking;
import hotel_booking.com.backend.exception.RoomUnavailableException;
import hotel_booking.com.backend.repository.BookingRepository;
import hotel_booking.com.backend.repository.RoomRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;


    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(timeout=5)
    public BookingDto createBooking(BookingDto bookingDto) {
        if (bookingDto.getCheckOutDate().isBefore(bookingDto.getCheckInDate()) ||
                bookingDto.getCheckOutDate().isEqual(bookingDto.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be strictly after the check-in date.");
        }
        roomRepository.findByIdWithLock(bookingDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        long overlapCount = bookingRepository.countOverlappingBookings(
                bookingDto.getRoomId(),
                bookingDto.getCheckInDate(),
                bookingDto.getCheckOutDate()
        );


        if (overlapCount > 0) {
            throw new RoomUnavailableException("Room is not available for these dates.");
        }

        Booking booking = new Booking();
        booking.setRoomId(bookingDto.getRoomId());
        booking.setGuestName(bookingDto.getGuestName());
        booking.setCheckInDate(bookingDto.getCheckInDate());
        booking.setCheckOutDate(bookingDto.getCheckOutDate());


        Booking savedBooking = bookingRepository.save(booking);

        BookingDto responseDto = new BookingDto();
        responseDto.setRoomId(savedBooking.getRoomId());
        responseDto.setGuestName(savedBooking.getGuestName());
        responseDto.setCheckInDate(savedBooking.getCheckInDate());
        responseDto.setCheckOutDate(savedBooking.getCheckOutDate());

        return responseDto;
        }

    public Page<BookingDto> getAllBookings(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);

        return bookingPage.map(booking -> {
            BookingDto dto = new BookingDto();
            dto.setRoomId(booking.getRoomId());
            dto.setGuestName(booking.getGuestName());
            dto.setCheckInDate(booking.getCheckInDate());
            dto.setCheckOutDate(booking.getCheckOutDate());
            return dto;
        });
    }
    }

