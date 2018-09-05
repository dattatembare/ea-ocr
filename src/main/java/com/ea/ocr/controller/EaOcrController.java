package com.ea.ocr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ea.ocr.data.GenerateData;
import com.ea.ocr.data.GenerateFiles;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/oa")
@Api(value = "oaOperations", description = "Operations pertaining to ea-ocr endpoints")
public class EaOcrController {
	private static final Logger log = LoggerFactory.getLogger(EaOcrController.class);
	private static final String RESOURCES_PATH = "./src/main/resources/";
	/**
	 * OCR START
	 */
	
	@Autowired
	GenerateData generateData;
	@Autowired
	GenerateFiles generateFiles;
	
	@ApiOperation(value = "PDF available for processing")
	@RequestMapping(value = "/generateJsonNcsv", method = RequestMethod.POST)
	public String generateJsonNcsv(@RequestParam("pdfFilePath") String pdfFilePath, @RequestParam("outputFilePath") String outputFilePath,
			@RequestParam("state") String state) {
		String jsonFile = RESOURCES_PATH+state.toLowerCase()+".json";
		log.info("Processing PDFs on path {} for {}", pdfFilePath, jsonFile);
		generateFiles.execute(pdfFilePath, outputFilePath, jsonFile);
		return "Success!";
	}
	
	@ApiOperation(value = "PDF available for processing")
	@RequestMapping(value = "/generateJson", method = RequestMethod.POST)
	public String generateJson(@RequestParam("pdfFilePath") String pdfFilePath, @RequestParam("outputFilePath") String outputFilePath,
			@RequestParam("state") String state) {
		String jsonFile = RESOURCES_PATH+state.toLowerCase()+".json";
		log.info("Processing PDFs on path {} for {}", pdfFilePath, jsonFile);
		generateData.generateJsonFile(pdfFilePath, outputFilePath, jsonFile);
		return "Success!";
	}
	
	@ApiOperation(value = "PDF available for processing")
	@RequestMapping(value = "/generateJsonWithThread", method = RequestMethod.POST)
	public String generateJsonWithThread(@RequestParam("pdfFilePath") String pdfFilePath, @RequestParam("outputFilePath") String outputFilePath,
			@RequestParam("state") String state) {
		String jsonFile = RESOURCES_PATH+state.toLowerCase()+".json";
		log.info("Processing PDFs on path {} for {}", pdfFilePath, jsonFile);
		generateData.generateJsonFileWithThreads(pdfFilePath, outputFilePath, jsonFile);
		return "Success!";
	}
	
	/**
	 * OCR END
	 */

	/*@ApiOperation(value = "WorfklowStages for a particular workflowId")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Workflow Stages data fetch"),
			@ApiResponse(code = 204, message = "No Content for that workflowid"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	@RequestMapping(value = "/workflowStatus", method = RequestMethod.GET)
	public ResponseEntity<List<WorkflowStageResource>> worflowStatus(
			@RequestParam("parentWorkflowId") long parentWorkflowId) {
		List<WorkflowStageResource> workflowStageResourceList = null;
		try {
			workflowStageResourceList = null; //dao.getWorkflowStatusTracking(parentWorkflowId);
			
			 * for (Iterator<WorkflowStageResource> iterator =
			 * workflowStageResourceList.iterator(); iterator.hasNext();) {
			 * WorkflowStageResource workflowStageResource = iterator.next();
			 * System.out.println("FileNames: "+workflowStageResource.
			 * getIngestFilename()); }
			 
			if (workflowStageResourceList.isEmpty()) {
				return new ResponseEntity(HttpStatus.NO_CONTENT);
			}

		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return new ResponseEntity<List<WorkflowStageResource>>(workflowStageResourceList, HttpStatus.OK);
	}*/

}
