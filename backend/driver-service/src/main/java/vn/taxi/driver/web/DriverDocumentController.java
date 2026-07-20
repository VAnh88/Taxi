package vn.taxi.driver.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.taxi.driver.domain.Driver;
import vn.taxi.driver.domain.DocumentType;
import vn.taxi.driver.domain.DriverDocument;
import vn.taxi.driver.repository.DriverDocumentRepository;
import vn.taxi.driver.service.DriverService;
import vn.taxi.driver.web.dto.CreateDriverDocumentRequest;
import vn.taxi.driver.web.dto.DriverDocumentResponse;

import java.util.List;
import java.util.UUID;

/** Hồ sơ giấy tờ tài xế (CCCD/bằng lái/cà vẹt/bảo hiểm) — tham chiếu khi admin duyệt verification. */
@RestController
@RequestMapping("/api/drivers/{driverId}/documents")
public class DriverDocumentController {

    private final DriverDocumentRepository documentRepository;
    private final DriverService driverService;

    public DriverDocumentController(DriverDocumentRepository documentRepository, DriverService driverService) {
        this.documentRepository = documentRepository;
        this.driverService = driverService;
    }

    @GetMapping
    public List<DriverDocumentResponse> list(@PathVariable UUID driverId) {
        return documentRepository.findByDriverId(driverId).stream().map(DriverDocumentResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<DriverDocumentResponse> create(@PathVariable UUID driverId,
                                                           @Valid @RequestBody CreateDriverDocumentRequest request) {
        Driver driver = driverService.getById(driverId);
        DriverDocument doc = new DriverDocument(driver, DocumentType.valueOf(request.docType().toUpperCase()), request.fileUrl());
        doc = documentRepository.save(doc);
        return ResponseEntity.status(HttpStatus.CREATED).body(DriverDocumentResponse.from(doc));
    }
}
