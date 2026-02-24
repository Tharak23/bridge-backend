package com.skillbridge.Bridge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.Bridge.dto.ServiceProviderRequest;
import com.skillbridge.Bridge.entity.ServiceProvider;
import com.skillbridge.Bridge.entity.User;
import com.skillbridge.Bridge.repository.ServiceProviderRepository;
import com.skillbridge.Bridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceProviderOnboardService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @Transactional
    public ServiceProvider submitServiceProvider(String clerkUserId, ServiceProviderRequest req) {
        User user = userRepository.findByClerkUserId(clerkUserId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .clerkUserId(clerkUserId)
                        .role("service_provider")
                        .name(req.getName())
                        .phone(req.getPhone())
                        .build()));

        user.setName(req.getName());
        user.setPhone(req.getPhone());
        userRepository.save(user);

        ServiceProvider sp = serviceProviderRepository.findByUserClerkUserId(clerkUserId)
                .orElse(ServiceProvider.builder().user(user).build());

        sp.setProfessionalType(req.getProfessionalType());
        sp.setDateOfBirth(req.getDateOfBirth());
        sp.setPhotoUrl(req.getPhotoUrl());
        sp.setGender(req.getGender());
        sp.setServicesOffered(req.getServicesOffered());
        sp.setExperienceYears(req.getExperienceYears());
        sp.setServiceArea(req.getServiceArea());
        sp.setBankAccountNumber(req.getBankAccountNumber());
        sp.setUpiId(req.getUpiId());
        sp.setWorkingHoursJson(toJson(req.getWorkingHours()));
        sp.setDaysAvailableJson(toJson(req.getDaysAvailable()));
        sp.setTravelRadiusKm(req.getTravelRadiusKm());
        if (Boolean.TRUE.equals(req.getTermsAccepted())) {
            sp.setTermsAcceptedAt(req.getTermsAcceptedAt() != null ? req.getTermsAcceptedAt() : Instant.now());
        }
        sp.setStatus("submitted");
        return serviceProviderRepository.save(sp);
    }

    public ServiceProvider getByClerkUserId(String clerkUserId) {
        return serviceProviderRepository.findByUserClerkUserId(clerkUserId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfileByClerkUserId(String clerkUserId) {
        ServiceProvider sp = serviceProviderRepository.findByUserClerkUserId(clerkUserId).orElse(null);
        if (sp == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", sp.getId().toString());
        map.put("status", sp.getStatus());
        map.put("professionalType", sp.getProfessionalType());
        map.put("dateOfBirth", sp.getDateOfBirth() != null ? sp.getDateOfBirth().toString() : null);
        map.put("photoUrl", sp.getPhotoUrl());
        map.put("gender", sp.getGender());
        map.put("servicesOffered", sp.getServicesOffered());
        map.put("experienceYears", sp.getExperienceYears());
        map.put("serviceArea", sp.getServiceArea());
        map.put("bankAccountNumber", sp.getBankAccountNumber());
        map.put("upiId", sp.getUpiId());
        map.put("workingHoursJson", sp.getWorkingHoursJson());
        map.put("daysAvailableJson", sp.getDaysAvailableJson());
        map.put("travelRadiusKm", sp.getTravelRadiusKm());
        if (sp.getUser() != null) {
            map.put("name", sp.getUser().getName());
            map.put("phone", sp.getUser().getPhone());
        }
        return map;
    }

    @Transactional
    public Map<String, Object> updateProfileByClerkUserId(String clerkUserId, Map<String, Object> updates) {
        ServiceProvider sp = serviceProviderRepository.findByUserClerkUserId(clerkUserId).orElse(null);
        if (sp == null) return null;
        User user = sp.getUser();
        if (user != null) {
            if (updates.containsKey("name") && updates.get("name") != null) user.setName((String) updates.get("name"));
            if (updates.containsKey("phone") && updates.get("phone") != null) user.setPhone((String) updates.get("phone"));
            userRepository.save(user);
        }
        if (updates.containsKey("professionalType")) sp.setProfessionalType((String) updates.get("professionalType"));
        if (updates.containsKey("dateOfBirth") && updates.get("dateOfBirth") != null) {
            Object dob = updates.get("dateOfBirth");
            sp.setDateOfBirth(java.time.LocalDate.parse(dob.toString()));
        }
        if (updates.containsKey("photoUrl")) sp.setPhotoUrl((String) updates.get("photoUrl"));
        if (updates.containsKey("gender")) sp.setGender((String) updates.get("gender"));
        if (updates.containsKey("servicesOffered")) sp.setServicesOffered((String) updates.get("servicesOffered"));
        if (updates.containsKey("experienceYears")) sp.setExperienceYears(updates.get("experienceYears") != null ? ((Number) updates.get("experienceYears")).intValue() : null);
        if (updates.containsKey("serviceArea")) sp.setServiceArea((String) updates.get("serviceArea"));
        if (updates.containsKey("bankAccountNumber")) sp.setBankAccountNumber((String) updates.get("bankAccountNumber"));
        if (updates.containsKey("upiId")) sp.setUpiId((String) updates.get("upiId"));
        if (updates.containsKey("travelRadiusKm")) sp.setTravelRadiusKm(updates.get("travelRadiusKm") != null ? ((Number) updates.get("travelRadiusKm")).intValue() : null);
        if (updates.containsKey("workingHours")) sp.setWorkingHoursJson(toJson(updates.get("workingHours")));
        if (updates.containsKey("daysAvailable")) sp.setDaysAvailableJson(toJson(updates.get("daysAvailable")));
        sp = serviceProviderRepository.save(sp);
        Map<String, Object> map = new HashMap<>();
        map.put("id", sp.getId().toString());
        map.put("status", sp.getStatus());
        map.put("professionalType", sp.getProfessionalType());
        map.put("dateOfBirth", sp.getDateOfBirth() != null ? sp.getDateOfBirth().toString() : null);
        map.put("photoUrl", sp.getPhotoUrl());
        map.put("gender", sp.getGender());
        map.put("servicesOffered", sp.getServicesOffered());
        map.put("experienceYears", sp.getExperienceYears());
        map.put("serviceArea", sp.getServiceArea());
        map.put("bankAccountNumber", sp.getBankAccountNumber());
        map.put("upiId", sp.getUpiId());
        map.put("workingHoursJson", sp.getWorkingHoursJson());
        map.put("daysAvailableJson", sp.getDaysAvailableJson());
        map.put("travelRadiusKm", sp.getTravelRadiusKm());
        if (sp.getUser() != null) {
            map.put("name", sp.getUser().getName());
            map.put("phone", sp.getUser().getPhone());
        }
        return map;
    }

    private static String toJson(Object value) {
        if (value == null) return "[]";
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
