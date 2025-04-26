package edu.cit.spot.service.impl;

import edu.cit.spot.dto.admin.AdminDto;
import edu.cit.spot.dto.admin.AdminUpdateRequest;
import edu.cit.spot.dto.admin.CreateAdminRequest;
import edu.cit.spot.entity.Admin;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.AdminRepository;
import edu.cit.spot.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminDto createAdmin(CreateAdminRequest request) {
        // Check if email is already in use
        if (adminRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use: " + request.email());
        }
        
        // Create new admin
        Admin admin = new Admin();
        admin.setFirstName(request.firstName());
        admin.setMiddleName(request.middleName());
        admin.setLastName(request.lastName());
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        
        Admin savedAdmin = adminRepository.save(admin);
        
        return AdminDto.fromEntity(savedAdmin);
    }

    @Override
    public AdminDto getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));
        
        return AdminDto.fromEntity(admin);
    }

    @Override
    public AdminDto getAdminByEmail(String email) {
        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "email", email));
        
        return AdminDto.fromEntity(admin);
    }

    @Override
    @Transactional
    public AdminDto updateAdmin(Long id, AdminUpdateRequest request) {
        Admin admin = adminRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));
        
        // Update email if it's changed and not in use by another admin
        if (request.email() != null && !request.email().equals(admin.getEmail())) {
            if (adminRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email is already in use: " + request.email());
            }
            admin.setEmail(request.email());
        }
        
        // Update other fields if provided
        if (request.firstName() != null) {
            admin.setFirstName(request.firstName());
        }
        if (request.middleName() != null) {
            admin.setMiddleName(request.middleName());
        }
        if (request.lastName() != null) {
            admin.setLastName(request.lastName());
        }
        if (request.password() != null) {
            admin.setPassword(passwordEncoder.encode(request.password()));
        }
        
        Admin updatedAdmin = adminRepository.save(admin);
        
        return AdminDto.fromEntity(updatedAdmin);
    }

    @Override
    public List<AdminDto> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        
        return admins.stream()
            .map(AdminDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));
        
        adminRepository.delete(admin);
        return true;
    }
}
