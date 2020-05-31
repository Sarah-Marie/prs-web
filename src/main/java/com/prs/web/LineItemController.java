package com.prs.web;

//import java.time.LocalDateTime;
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
import com.prs.business.LineItem;
import com.prs.business.Request;
import com.prs.db.LineItemRepository;
import com.prs.db.RequestRepository;
@CrossOrigin
@RestController
@RequestMapping("/line-items")
public class LineItemController {

	@Autowired
	private LineItemRepository lineitemRepo;
	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<LineItem> lineitems = lineitemRepo.findAll();
		if (lineitems.size() == 0) {
			jr = JsonResponse.getErrorInstance("No line items found.");
		} else {
			jr = JsonResponse.getInstance(lineitems);
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {

		JsonResponse jr = null;
		Optional<LineItem> lineitem = lineitemRepo.findById(id);
		if (lineitem.isPresent()) {
			jr = JsonResponse.getInstance(lineitem.get());
		} else {
			jr = JsonResponse.getErrorInstance("No line items found for id: " + id);
		}

		return jr;
	}

	
	// Add line items
	@PostMapping("/")
	public JsonResponse addLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;

		try {
			li = lineitemRepo.save(li);
			jr = JsonResponse.getInstance(li);
			recalculateTotal(li.getRequest());
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}
		catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/")
	public JsonResponse updateLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;

		try {
			li = lineitemRepo.save(li);
			jr = JsonResponse.getInstance(li);
			recalculateTotal(li.getRequest());
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}
		catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;

	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteLineItem(@PathVariable int id) {
		JsonResponse jr = null;

		try {
			LineItem li = lineitemRepo.findById(id).get();
			lineitemRepo.deleteById(id);
			jr = JsonResponse.getInstance("Line item id: " + id + " deleted successfully.");
			Request r = li.getRequest();
			recalculateTotal(r);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}
		catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;

	}

	// List line items for purchase request and return list<LineItem>
	@GetMapping("/lines-for-pr/{id}")
	public JsonResponse listLineItemsRequest(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			List<LineItem> lines = lineitemRepo.findAllByRequestId(id);
			jr = JsonResponse.getInstance(lines);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}
		catch (Exception e) {
			jr = JsonResponse.getInstance(e);
			e.printStackTrace();
		}
		return jr;
	}

	// recalculate method

	private void recalculateTotal(Request r) {

		List<LineItem> lines = lineitemRepo.findAllByRequestId(r.getId());

		double total = 0.0;
		for (LineItem line : lines) {
			total += line.getQuantity() * line.getProduct().getPrice();
		}
		// save that total in the requestRepo
		r.setTotal(total);
		try {
			requestRepo.save(r);
		} catch (Exception e) {
			throw e;
		}

	}
}
