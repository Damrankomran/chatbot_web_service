package com.damrankomran.chatbot_web_service.controller;

import com.damrankomran.chatbot_web_service.model.Question;
import com.damrankomran.chatbot_web_service.service.QuestionService;
import com.damrankomran.chatbot_web_service.service.impl.QuestionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final QuestionService questionService = new QuestionServiceImpl();

    @RequestMapping(value = "/model", method = RequestMethod.POST)
    public ResponseEntity<?> findResponse(@RequestBody Question question){
        log.info("findResponse");
        try{
            String response = questionService.findResponse(question);
            log.info("response: "+response);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
