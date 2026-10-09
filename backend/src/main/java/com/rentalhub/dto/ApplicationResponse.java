package com.rentalhub.dto;

import com.rentalhub.entity.Application;

public record ApplicationResponse(Long id, UserInfo user, PropertyInfo property, String status) {
    public record UserInfo(Long id, String name, String email, String role) {}
    public record PropertyInfo(Long id, String title, String location, Double price, String description, String ownerName, String ownerEmail, String ownerPhone) {}

    public static ApplicationResponse from(Application a) {
        var u = a.getUser();
        var p = a.getProperty();
        return new ApplicationResponse(
                a.getId(),
                new UserInfo(u.getId(), u.getName(), u.getEmail(), u.getRole()),
                new PropertyInfo(
                        p.getId(), p.getTitle(), p.getLocation(), p.getPrice(), p.getDescription(),
                        "APPROVED".equals(a.getStatus()) ? p.getOwnerName() : null,
                        "APPROVED".equals(a.getStatus()) ? p.getOwnerEmail() : null,
                        "APPROVED".equals(a.getStatus()) ? p.getOwnerPhone() : null
                ),
                a.getStatus()
        );
    }
}
