package com.diplom.controller;

import com.diplom.dto.VacancyRequest;
import com.diplom.dto.VacancyWithDocumentsDto;
import com.diplom.entity.UserEntity;
import com.diplom.entity.VacancyEntity;
import com.diplom.service.AuthService;
import com.diplom.service.VacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<VacancyEntity> createVacancy(
            @RequestBody VacancyRequest request,
            @CookieValue("token") String token) {
        UserEntity user = authService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        VacancyEntity vacancy = new VacancyEntity();
        vacancy.setTitle(request.getTitle());
        vacancy.setDescription(request.getDescription());
        vacancy.setCreatedBy(user);
        vacancy.setHardSkills(request.getHardSkills() != null ? request.getHardSkills() : new HashSet<>());
        vacancy.setSoftSkills(request.getSoftSkills() != null ? request.getSoftSkills() : new HashSet<>());

        try {
            VacancyEntity saved = vacancyService.createVacancy(vacancy, user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VacancyWithDocumentsDto>> getAllVacancies(
            @CookieValue(value = "token", required = false) String token) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        UserEntity user = authService.getUserFromToken(token);
        List<VacancyWithDocumentsDto> vacancies = vacancyService.getAllVacancies(user);
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/my")
    public ResponseEntity<List<VacancyWithDocumentsDto>> getUserVacancies(
            @CookieValue("token") String token) {
        UserEntity user = authService.getUserFromToken(token);
        return ResponseEntity.ok(vacancyService.getUserVacancies(user.getId()));
    }

    @PostMapping("/{vacancyId}/documents/{documentId}")
    public ResponseEntity<Void> attachDocumentToVacancy(
            @PathVariable("vacancyId") UUID vacancyId,
            @PathVariable("documentId") UUID documentId,
            @CookieValue("token") String token) {
        UserEntity user = authService.getUserFromToken(token);
        try {
            vacancyService.attachDocument(vacancyId, documentId, user.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{vacancyId}/documents/{documentId}")
    public ResponseEntity<Void> detachDocumentFromVacancy(
            @PathVariable("vacancyId") UUID vacancyId,
            @PathVariable("documentId") UUID documentId,
            @CookieValue("token") String token) {
        UserEntity user = authService.getUserFromToken(token);
        try {
            vacancyService.detachDocument(vacancyId, documentId, user.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(
            @PathVariable("id") UUID id,
            @CookieValue("token") String token) {
        UserEntity user = authService.getUserFromToken(token);
        try {
            vacancyService.deleteVacancy(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/hard-skills")
    public ResponseEntity<List<String>> searchHardSkills(
            @RequestParam(value = "query", defaultValue = "") String query) throws IOException {
        return ResponseEntity.ok(vacancyService.searchHardSkills(query));
    }

    @GetMapping("/soft-skills")
    public ResponseEntity<List<String>> searchSoftSkills(
            @RequestParam(value = "query", defaultValue = "") String query) throws IOException {
        return ResponseEntity.ok(vacancyService.searchSoftSkills(query));
    }

    @PostMapping("/hard-skills")
    public ResponseEntity<Void> addHardSkill(
            @RequestParam("skill") String skill) throws IOException {
        vacancyService.addHardSkill(skill);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/soft-skills")
    public ResponseEntity<Void> addSoftSkill(
            @RequestParam("skill") String skill) throws IOException {
        vacancyService.addSoftSkill(skill);
        return ResponseEntity.ok().build();
    }
}
