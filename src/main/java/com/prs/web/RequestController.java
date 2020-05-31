package com.prs.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prs.business.JsonResponse;
import com.prs.business.Request;
import com.prs.db.RequestRepository;
@CrossOrigin
@RestController
@RequestMapping("/requests")
public class RequestController {

	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<Request> requests = requestRepo.findAll();
		if (requests.size() == 0) {
			jr = JsonResponse.getErrorInstance("No requests found.");
		} else {
			jr = JsonResponse.getInstance(requests);
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {

		JsonResponse jr = null;
		Optional<Request> request = requestRepo.findById(id);
		if (request.isPresent()) {
			jr = JsonResponse.getInstance(request.get());
		} else {
			jr = JsonResponse.getErrorInstance("No requests found for id: " + id);
		}

		return jr;
	}

	// Request review- return list of requests-show in review status and not
	// assigned to logged in user

	@GetMapping("/list-review/{id}")
	public JsonResponse listReview(@PathVariable int id) {
		System.out.println("listreview for id: "+id);
		JsonResponse jr = null;
		List<Request> requests = requestRepo.findAllByStatusAndUserIdNot("Review", id);

		try {

			jr = JsonResponse.getInstance(requests);
		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
			e.printStackTrace();
		}

		return jr;
	}

	@PostMapping("/")
	public JsonResponse createRequest(@RequestBody Request r) {
		JsonResponse jr = null;

		r.setStatus("New");
		r.setSubmittedDate(LocalDateTime.now());

		try {
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		}

		catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}

		catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;

	}

	@PutMapping("/")
	public JsonResponse updateRequest(@RequestBody Request r) {
		JsonResponse jr = null;

		try {
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;

	}

	// submit request for review
	@PutMapping("/submit-review")
	public JsonResponse submitRequestToReview(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			if (r.getTotal() <= 50) {
				r.setStatus("Approved");
				r = requestRepo.save(r);
				jr = JsonResponse.getInstance(r);
			} else {
				r.setStatus("Review");

				r.setSubmittedDate(LocalDateTime.now());
				r = requestRepo.save(r);
				jr = JsonResponse.getInstance(r);

			}
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error submitting request: " + e.getMessage());
			e.printStackTrace();

		}

		return jr;

	}
	// OR THIS?
	// if () {
	// r.setStatus("Approved");
	// }
	// else {
	// r.setStatus("Review");
	// }
	// r.setSubmittedDate(LocalDateTime.now());
	// Optional<Request> request = requestRepo.setStatusToReview(r.getStatus());

	// approve request
	@PutMapping("/approve")
	public JsonResponse approve(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			r.setStatus("Approved");
			jr = JsonResponse.getInstance(requestRepo.save(r));

		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
			e.printStackTrace();
		}
		return jr;
	}

	// Reject request
	@PutMapping("/reject")
	public JsonResponse reject(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			r.setStatus("Rejected");
			jr = JsonResponse.getInstance(requestRepo.save(r));

		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
			e.printStackTrace();
		}
		return jr;
	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteRequest(@PathVariable int id) {
		JsonResponse jr = null;

		try {
			requestRepo.deleteById(id);
			jr = JsonResponse.getInstance("Request id: " + id + "successfully deleted.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;

	}

}
