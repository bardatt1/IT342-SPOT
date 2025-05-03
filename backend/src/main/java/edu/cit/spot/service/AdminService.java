package edu.cit.spot.service;

import edu.cit.spot.dto.admin.AdminDto;
import edu.cit.spot.dto.admin.CreateAdminRequest;
import edu.cit.spot.dto.admin.CreateSystemAdminRequest;
import edu.cit.spot.dto.admin.AdminUpdateRequest;

import java.util.List;

public interface AdminService {
    
    AdminDto createAdmin(CreateAdminRequest request);
    
    AdminDto createSystemAdmin(CreateSystemAdminRequest request);
    
    AdminDto getAdminById(Long id);
    
    AdminDto getAdminByEmail(String email);
    
    AdminDto updateAdmin(Long id, AdminUpdateRequest request);
    
    AdminDto promoteToSystemAdmin(Long id);
    
    AdminDto demoteFromSystemAdmin(Long id);
    
    List<AdminDto> getAllAdmins();
    
    List<AdminDto> getAllSystemAdmins();
    
    boolean deleteAdmin(Long id);
}
