package com.example.bakeryshop.services;


import com.example.bakeryshop.repisitory.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class UserService {


    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;;

    }

   }