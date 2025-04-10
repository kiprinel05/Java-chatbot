package org.example.admin;

import org.example.admin.Interfaces.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Admin loginRequest) {
        Optional<Admin> admin = adminRepository.findByUsername(loginRequest.getUsername());

        if (admin.isPresent() && admin.get().getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.ok("Autentificare reușită! Acces permis la Admin Panel.");
        } else {
            return ResponseEntity.status(401).body("Eroare: Username sau parolă incorecte.");
        }
    }
}
