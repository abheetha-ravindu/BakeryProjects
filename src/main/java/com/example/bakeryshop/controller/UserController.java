package com.example.bakeryshop.controller;

import com.example.bakeryshop.model.AdminUser;
import com.example.bakeryshop.model.Customer;
import com.example.bakeryshop.model.User;
import com.example.bakeryshop.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new Customer());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Customer customer) {
        userService.register(customer);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password,
                            HttpSession session, Model model) {

        Optional<AdminUser> admin = userService.authenticateAdmin(username, password);
        if (admin.isPresent()) {
            session.removeAttribute("loggedInUserId");
            session.setAttribute("loggedInAdminId", admin.get().getId());
            return "redirect:/admin/profile";
        }


        Optional<User> user = userService.authenticate(username, password);

        if (user.isPresent()) {
            session.removeAttribute("loggedInAdminId");
            session.setAttribute("loggedInUserId", user.get().getId());
            return "redirect:/main";
        }

        model.addAttribute("error", "Username/Email හෝ Password වැරදියි!");
        return "login";
    }

    @GetMapping("/profile")
    public String showMyProfile(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdminId") != null) {
            return "redirect:/admin/profile";
        }
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", userService.findById(userId));
        return "profileupdate";
    }

    @PostMapping("/profile/update")
    public String updateMyProfile(@ModelAttribute Customer customer, HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId != null) {
            userService.updateProfile(userId, customer);
        }
        return "redirect:/main";
    }

    @GetMapping("/profile/delete")
    public String deleteMyProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId != null) {
            userService.deleteById(userId);
            session.invalidate();
        }
        return "redirect:/register";
    }

    @GetMapping("/admin/users")
    public String showUserList(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdminId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("users", userService.findAllCustomers());
        return "adminuserlist";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("loggedInAdminId") == null) {
            return "redirect:/login";
        }
        userService.deleteById(id);
        return "redirect:/admin/profile?tab=users";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
