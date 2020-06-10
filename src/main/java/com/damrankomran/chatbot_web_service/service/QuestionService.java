package com.damrankomran.chatbot_web_service.service;

import com.damrankomran.chatbot_web_service.model.Question;

public interface QuestionService {

    String findResponse(Question question) throws Exception;

    void convertJsonToArff(Question question);

    void deleteTestArff();

    double[] responseCounter();

    double bestResponse(double[] responseCounter);

    String createFileName();
}
