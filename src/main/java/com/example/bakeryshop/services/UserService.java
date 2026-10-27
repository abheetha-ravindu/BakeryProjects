package com.example.bakeryshop.services;

import com.example.bakeryshop.model.AdminUser;
import com.example.bakeryshop.model.Customer;
import com.example.bakeryshop.model.User;
import com.example.bakeryshop.repository.AdminUserRepository;
import com.example.bakeryshop.repository.DeliveryRepository;
import com.example.bakeryshop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final DeliveryRepository deliveryRepository;

    public UserService(UserRepository userRepository, AdminUserRepository adminUserRepository,
                       DeliveryRepository deliveryRepository) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.deliveryRepository = deliveryRepository;
    }

    /** Checks the admin_user table (username or email + password). */
    public Optional<AdminUser> authenticateAdmin(String usernameOrEmail, String password) {
        AdminUser admin = adminUserRepository.findByUsername(usernameOrEmail);
        if (admin == null) {
            admin = adminUserRepository.findByEmail(usernameOrEmail);
        }
        if (admin != null && admin.getPassword() != null && admin.getPassword().equals(password)) {
            return Optional.of(admin);
        }
        return Optional.empty();
    }

    public AdminUser findAdminById(Long id) {
        return adminUserRepository.findById(id).orElse(null);
    }

    public List<Customer> findAllCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Customer)
                .map(u -> (Customer) u)
                .toList();
    }

    public Customer register(Customer customer) {
        return userRepository.save(customer);
    }

    /** Looks the user up by username first, then by email, and checks the password. */
    public Optional<User> authenticate(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** Updates only the editable fields (name, phone, address, password); email stays as it is. */
    public void updateProfile(Long userId, Customer form) {
        User existing = userRepository.findById(userId).orElse(null);
        if (existing instanceof Customer customer) {
            customer.setUsername(form.getUsername());
            customer.setPhoneNumber(form.getPhoneNumber());
            customer.setAddress(form.getAddress());
            customer.setPassword(form.getPassword());
            userRepository.save(customer);
        }
    }

    @Transactional
    public void deleteById(Long id) {
        deliveryRepository.deleteByCustomerId(id); // user ge deliveries issella ain karanawa
        userRepository.deleteById(id);
    }
}
