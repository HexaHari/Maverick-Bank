package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.AdminReqDto;
import com.bank.MaverickBank.dto.request.EmployeeUpdateDto;
import com.bank.MaverickBank.dto.request.UserAddDto;
import com.bank.MaverickBank.dto.request.UserUpdateDto;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.dto.response.UserFilterDto;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.service.AdminService;
import com.bank.MaverickBank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    @PostMapping("/api/admin/add")
    public void addAdmin(@Valid @RequestBody AdminReqDto adminReqDto){
        userService.getUserObject(adminReqDto.username(), adminReqDto.password(), Role.ADMIN);
    }

    @PutMapping("/api/admin/employee/{employeeId}")
    public void updateEmployee(@PathVariable Long employeeId, @RequestBody @Valid EmployeeUpdateDto dto) {
        adminService.updateEmployee(employeeId, dto);
    }

    @PutMapping("/api/admin/user/{userId}")
    public void updateUser(@PathVariable Long userId, @RequestBody @Valid UserUpdateDto dto, Principal principal) {
        adminService.updateUser(userId, dto, principal);
    }

    @GetMapping("/api/admin/employee/stat")
    public ChartDto getEmployeeBranchStats() {
        return adminService.getEmployeeBranchStats();
    }

    @GetMapping("/api/admin/account/stat")
    public ChartDto getAccountBranchStats() {
        return adminService.getAccountBranchStats();
    }

    @PutMapping("/api/admin/user/{userId}/deactivate")
    public void deactivateUser(@PathVariable Long userId, Principal principal) {
        adminService.deactivateUser(userId, principal);
    }

    // AdminController
    @GetMapping("/api/admin/user/filter")
    public UserFilterDto dynamicFilterUser(@RequestParam(name = "userId", required = false) Long userId,
                                           @RequestParam(name = "username", required = false) String username,
                                           @RequestParam(name = "role", required = false) Role role,
                                           @RequestParam(name = "isActive", required = false) Boolean isActive,
                                           @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                           @RequestParam(name = "createdTill", required = false) LocalDate createdTill,
                                           @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                           @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        return adminService.dynamicFilterUser(userId, username, role, isActive, createdFrom, createdTill, page, size);
    }

    @PostMapping("/api/admin/user/add")
    public void addUser(@Valid @RequestBody UserAddDto userAddDto){
        userService.getUserObject(userAddDto.username(), userAddDto.password(), userAddDto.role());
    }

}
