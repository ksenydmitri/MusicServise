package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import music.service.service.VisitCounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visits")
@Tag(name = "Visits Controller", description = "API для учёта посещений")
public class VisitsController {

    private final VisitCounterService visitCounterService;

    public VisitsController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @PostMapping
    @Operation(summary = "Увеличить счётчик посещений", description = "Увеличивает количество посещений для указанного URL.")
    public ResponseEntity<Void> incrementVisitCount(
            @Parameter(description = "URL для подсчёта посещений", example = "/logs")
            @RequestParam String url) {
        visitCounterService.incrementVisitCount(url);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Получить количество посещений", description = "Возвращает количество посещений для указанного URL.")
    public ResponseEntity<Integer> getVisitCount(
            @Parameter(description = "URL для подсчёта посещений", example = "/logs")
            @RequestParam String url) {
        int count = visitCounterService.getVisitCount(url);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total")
    @Operation(summary = "Получить общее количество посещений", description = "Возвращает общее количество посещений по всем URL.")
    public ResponseEntity<Integer> getTotalVisitCount() {
        int totalCount = visitCounterService.getTotalVisitCount();
        return ResponseEntity.ok(totalCount);
    }
}
