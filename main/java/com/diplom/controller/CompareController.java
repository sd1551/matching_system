package com.diplom.controller;

import com.diplom.dto.ComparisonDto;
import com.diplom.dto.ComparisonResultDto;
import com.diplom.dto.DocumentComparisonDto;
import com.diplom.service.AuthService;
import com.diplom.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/compare")
@RequiredArgsConstructor
public class CompareController {

    private final ComparisonService comparisonService;
    private final AuthService authService;

    @GetMapping
    public String comparePage() {
        return "compare";
    }

    @GetMapping("/vacancies")
    @ResponseBody
    public ResponseEntity<List<ComparisonDto>> getVacanciesWithDocuments() {
        return ResponseEntity.ok(comparisonService.getVacanciesWithDocuments());
    }

    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<ComparisonResultDto> startComparison(
            @RequestParam("vacancyId") UUID vacancyId,
            @CookieValue("token") String token) {
        UUID adminId = authService.getUserFromToken(token).getId();
        return ResponseEntity.ok(comparisonService.createComparison(vacancyId, adminId));
    }

    @GetMapping("/result/{id}")
    @ResponseBody
    public ResponseEntity<ComparisonResultDto> getComparisonResult(@PathVariable UUID id) {
        return ResponseEntity.ok(comparisonService.getComparisonResult(id));
    }

    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<Void> updateRecommendations(
            @RequestParam("comparisonId") UUID comparisonId,
            @RequestBody List<DocumentComparisonDto> updates) {
        comparisonService.updateRecommendations(comparisonId, updates);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-one")
    @ResponseBody
    public ResponseEntity<Void> sendRecommendation(
            @RequestParam("comparisonId") UUID comparisonId,
            @RequestParam("documentId") UUID documentId,
            @RequestBody Map<String, String> request,
            @CookieValue("token") String token) {
        UUID adminId = authService.getUserFromToken(token).getId();
        comparisonService.sendRecommendation(comparisonId, documentId, request.get("recommendation"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-all")
    @ResponseBody
    public ResponseEntity<Void> sendAllRecommendations(
            @RequestParam("comparisonId") UUID comparisonId,
            @CookieValue("token") String token) {
        UUID adminId = authService.getUserFromToken(token).getId();
        comparisonService.sendAllRecommendations(comparisonId);
        return ResponseEntity.ok().build();
    }
}