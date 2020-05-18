package net.yisu.example.demo;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Basic {

	private static final Logger logger = LoggerFactory.getLogger(Basic.class);

	@RequestMapping(value = "${v1API}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> indexGetWithoutSlash() {
		logger.info("the endpoint GET / without the slash was reached");

		JSONObject output = new JSONObject();
		output.put("name", "hello there.");
		return ResponseEntity.status(HttpStatus.OK).body( output.toString() );

	}

	@RequestMapping(value = "${v1API}/", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> indexGet() {
		logger.info("the endpoint GET / was reached");

		JSONObject output = new JSONObject();
		output.put("name", "hi there.");
		return ResponseEntity.status(HttpStatus.OK).body( output.toString() );

	}

}
