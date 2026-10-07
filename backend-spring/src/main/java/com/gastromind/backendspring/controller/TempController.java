package com.gastromind.backendspring.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp")
public class TempController {

    @GetMapping("/bcrypt")
    public String generateHash(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        return "Password: " + password + "\nBCrypt Hash: " + hash;
    }
}
