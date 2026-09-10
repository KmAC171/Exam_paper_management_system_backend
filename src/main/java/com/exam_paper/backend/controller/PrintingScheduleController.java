package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.printing.*;
import com.exam_paper.backend.service.PrintingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/printing")
@RequiredArgsConstructor
public class PrintingScheduleController {

    private final PrintingScheduleService printingScheduleService;

    @GetMapping("/slots/available")
    public ResponseEntity<List<SlotAvailabilityDTO>> getAvailableSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String location
    ) {
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(printingScheduleService.getAvailableSlots(effectiveDate, location));
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<PrintingScheduleResponseDTO>> getAllSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId
    ) {
        return ResponseEntity.ok(printingScheduleService.getAllSchedules(fromDate, toDate, status, departmentId));
    }

    @GetMapping("/my-schedules")
    public ResponseEntity<List<PrintingScheduleResponseDTO>> getMySchedules(Authentication authentication) {
        return ResponseEntity.ok(printingScheduleService.getMySchedules(authentication.getName()));
    }

    @GetMapping("/packet/{packetId}")
    public ResponseEntity<?> getActiveScheduleForPacket(@PathVariable Long packetId) {
        return printingScheduleService.getActiveScheduleForPacket(packetId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of("message", "No active printing schedule found for packet")));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPrintingStats() {
        return ResponseEntity.ok(printingScheduleService.getPrintingStats());
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookSlot(
            @RequestBody BookSlotRequestDTO request,
            Authentication authentication
    ) {
        try {
            PrintingScheduleResponseDTO response = printingScheduleService.bookSlot(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/schedules/{id}/reschedule")
    public ResponseEntity<?> rescheduleSlot(
            @PathVariable Long id,
            @RequestBody RescheduleSlotRequestDTO request,
            Authentication authentication
    ) {
        try {
            PrintingScheduleResponseDTO response = printingScheduleService.rescheduleSlot(id, authentication.getName(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/schedules/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdatePrintingStatusDTO request,
            Authentication authentication
    ) {
        try {
            PrintingScheduleResponseDTO response = printingScheduleService.updateStatus(id, request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<?> cancelSlot(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            printingScheduleService.cancelSlot(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Printing appointment successfully cancelled."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
