package vn.taxi.trip.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.taxi.trip.repository.CancelReasonRepository;
import vn.taxi.trip.web.dto.CancelReasonResponse;

import java.util.List;

@RestController
@RequestMapping("/api/cancel-reasons")
public class CancelReasonController {

    private final CancelReasonRepository cancelReasonRepository;

    public CancelReasonController(CancelReasonRepository cancelReasonRepository) {
        this.cancelReasonRepository = cancelReasonRepository;
    }

    @GetMapping
    public List<CancelReasonResponse> list() {
        return cancelReasonRepository.findAll().stream().map(CancelReasonResponse::from).toList();
    }
}
