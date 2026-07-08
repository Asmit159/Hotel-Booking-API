package hotel_booking.com.backend.config;

import hotel_booking.com.backend.entity.Role;
import hotel_booking.com.backend.entity.Room;
import hotel_booking.com.backend.entity.User;
import hotel_booking.com.backend.repository.RoomRepository;
import hotel_booking.com.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoomRepository roomRepository, UserRepository userRepository) {

        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        if (roomRepository.count() == 0) {
            List<Room> roomsToSave = new ArrayList<>();

            for (int i = 1; i <= 30; i++) {
                Room room = new Room();
                room.setRoomNumber("S-" + i);
                room.setRoomType("STANDARD");
                room.setPricePerNight(new BigDecimal("1500.00"));
                roomsToSave.add(room);
            }

            for (int i = 1; i <= 20; i++) {
                Room room = new Room();
                room.setRoomNumber("D-" + i);
                room.setRoomType("DELUXE");
                room.setPricePerNight(new BigDecimal("3500"));
                roomsToSave.add(room);
            }

            for (int i = 1; i <= 10; i++) {
                Room room = new Room();
                room.setRoomNumber("SUITE-" + i);
                room.setRoomType("SUITE");
                room.setPricePerNight(new BigDecimal("10500"));
                roomsToSave.add(room);
            }


            roomRepository.saveAll(roomsToSave);

            System.out.println("==============================================");
            System.out.println("DATABASE SEEDED: 60 Rooms successfully created.");
            System.out.println("==============================================");
        } else {
            System.out.println("Database already contains data. Seeder skipped.");
        }

        //SEED ADMIN USER
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            // Hash the password before saving!
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            System.out.println("==============================================");
            System.out.println("SECURITY SEEDED: Admin account created.");
            System.out.println("==============================================");
        }
    }

}