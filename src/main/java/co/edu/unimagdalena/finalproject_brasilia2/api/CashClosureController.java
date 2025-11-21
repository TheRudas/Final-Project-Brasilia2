package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.CashClosureDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.CashClosureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Cash Closure Controller
 * Manages daily cash register closures for clerks
 *
 * Security:
 * - Close cash: CLERK, ADMIN
 * - View closures: CLERK (own), DISPATCHER, ADMIN (all)
 *
 * @author AFGamero
 * @since 2025-11-21
 */
@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashClosureController {

    private final CashClosureService cashClosureService;

    /**
     * Close cash register for the day
     * POST /api/cash/close
     *
     * Clerk must provide:
     * - Total cash collected
     * - Number of tickets sold
     * - Number of parcels registered
     * - Total baggage fees collected
     */
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping("/close")
    public ResponseEntity<CashClosureResponse> close(@Valid @RequestBody CashClosureCreateRequest request) {
        CashClosureResponse response = cashClosureService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get cash closure by ID
     * GET /api/cash/closures/{id}
     */
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/closures/{id}")
    public ResponseEntity<CashClosureResponse> get(@PathVariable Long id) {
        CashClosureResponse response = cashClosureService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get cash closure by clerk and date
     * GET /api/cash/closures/clerk/{clerkId}/date/{date}
     */
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/closures/clerk/{clerkId}/date/{date}")
    public ResponseEntity<CashClosureResponse> getByClerkAndDate(
            @PathVariable Long clerkId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CashClosureResponse response = cashClosureService.getByClerkAndDate(clerkId, date);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all cash closures by clerk
     * GET /api/cash/closures/clerk/{clerkId}
     */
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/closures/clerk/{clerkId}")
    public ResponseEntity<List<CashClosureResponse>> getByClerk(@PathVariable Long clerkId) {
        List<CashClosureResponse> closures = cashClosureService.getByClerk(clerkId);
        return ResponseEntity.ok(closures);
    }

    /**
     * Get cash closures by date range
     * GET /api/cash/closures/range?startDate=2025-11-01&endDate=2025-11-30
     */
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/closures/range")
    public ResponseEntity<List<CashClosureResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CashClosureResponse> closures = cashClosureService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(closures);
    }
}