package com.damrankomran.chatbot_web_service.service.impl;

import com.damrankomran.chatbot_web_service.model.Question;
import com.damrankomran.chatbot_web_service.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;

import java.io.*;
import java.util.Objects;

@Slf4j
public class QuestionServiceImpl implements QuestionService {

    //Burada diğer algortimalarla karşılaştırabilmek için algoritmalardan dönen response indislerini tutcaz.
    private double[] rsp = new double[3]; //--> Model sayısını ve onlardan dönecek olan response bilgilerini temsil eder.
    private String filePath = createFileName(); //--> Timestamp ile dosya adı oluşturur.
    private FileWriter arff;

    @Override
    public String findResponse(Question question) throws Exception {
        log.info("findResponse");
        ClassLoader classLoader = QuestionServiceImpl.class.getClassLoader();
        convertJsonToArff(question);

        log.info("Modeller Yükleniyor");

        String J48ModelPath = "weka_models/j48.model";
        String BayesModelPath = "weka_models/nb.model";
        String IbkModelPath = "weka_models/ibk.model";

        //resource
        FilteredClassifier NBM = (FilteredClassifier) weka.core.SerializationHelper.read(Objects.requireNonNull(classLoader.getResource(BayesModelPath)).getFile());
        FilteredClassifier J48 = (FilteredClassifier) weka.core.SerializationHelper.read(Objects.requireNonNull(classLoader.getResource(J48ModelPath)).getFile());
        FilteredClassifier IBK = (FilteredClassifier) weka.core.SerializationHelper.read(Objects.requireNonNull(classLoader.getResource(IbkModelPath)).getFile());

        log.info("Modeller Yüklendi.");

        //JSON'dan arff'e dönüştürülen dosya okunuyor.
        BufferedReader breader;
        breader = new BufferedReader(new FileReader(filePath));
        Instances labeled = new Instances(breader);
        labeled.setClassIndex(labeled.numAttributes() - 1);
        // label instances
        //response'un indisini döndürür.
        rsp[0] = NBM.classifyInstance(labeled.instance(0));       //NBM algoritmasından dönen response indisini tutar
        rsp[1] = J48.classifyInstance(labeled.instance(0));      //j48 algoritmasından dönen response indisini tutar
        rsp[2] = IBK.classifyInstance(labeled.instance(0));     //IBK algoritmasından dönen response indisini tutar

        //algoritmalardan dönen index değerleri
        log.info("clsNBM: " + rsp[0]);
        log.info("clsJ48:  " + rsp[1]);
        log.info("clsIBK: " + rsp[2]);

        double[] responseCounter = responseCounter();        //En çok hangi response' çıkmış onun döndüren olan fonksiyon
        double result = bestResponse(responseCounter);      //Algoritma doğruluğu ve response adetine göre en iyi response'u döndüren fonksiyon

        labeled.instance(0).setClassValue(result);
        log.info("Result --> " + result);
        log.info("cevap -->" + labeled.instance(0));
        log.info("Döndürülecek olan rsp degeri ->" + labeled.instance(0).toString(1));

        question.setQuestion(labeled.instance(0).toString());
        breader.close();
        deleteTestArff();
        return labeled.instance(0).toString(1);
    }

    @Override
    public void convertJsonToArff(Question question) {
        log.info("convertJsonToArff");
        try {
            log.info("file name --> {}", filePath);
            arff = new FileWriter(filePath);
            log.info("question: {}", question.getQuestion());
            arff.append("@relation question\n");
            arff.append("@attribute question string\n");
            arff.append(
                    "@attribute class { rsp1, rsp2, rsp3, rsp4, rsp5, rsp6, rsp7, rsp8, rsp9, rsp10, rsp11, " +
                            "rsp12, rsp13 ,rsp14, rsp15, rsp16, rsp17, rsp18, rsp19, rsp20, rsp21, rsp22, rsp23, " +
                            "rsp24, rsp25, rsp26, rsp27, rsp28 ,rsp29, rsp30, rsp31, rsp32, rsp33, rsp34, rsp35, " +
                            "rsp36, rsp37, rsp38, rsp39, rsp40, rsp41, rsp42, rsp43, rsp44, rsp45, rsp46, rsp47, " +
                            "rsp48, rsp49, rsp50, rsp51, rsp52, rsp53, rsp54, rsp55, rsp56, rsp57, rsp58, rsp59, " +
                            "rsp60, rsp61, rsp62, rsp63, rsp64, rsp65, rsp66, rsp67, rsp68, rsp69, rsp70, rsp71, " +
                            "rsp72, rsp73, rsp74, rsp75, rsp76, rsp77, rsp78, rsp79, rsp80, rsp81, rsp82, rsp83, " +
                            "rsp84, rsp85, rsp86, rsp87, rsp88, rsp89, rsp90, rsp91, rsp92, rsp93, rsp94, rsp95, " +
                            "rsp96, rsp97, rsp98, rsp99, rsp100 ,rsp101, rsp103, rsp104, rsp105, rsp106, rsp107, " +
                            "rsp108, rsp109, rsp110, rsp111, rsp112, rsp113, rsp115, rsp116, rsp117, rsp118, rsp119, " +
                            "rsp120, rsp121, rsp122, rsp123, rsp124, rsp125, rsp126, rsp127, rsp128, rsp129, rsp130, " +
                            "rsp131, rsp132, rsp133}\n");
            arff.append("@data\n");
            arff.append("'").append(question.getQuestion()).append("'").append(",");
            arff.append("?");
            arff.flush();
            arff.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTestArff() {
        File file = new File(filePath);
        if (file.delete()) {
            log.info(filePath + " deleted!");
        } else {
            log.error("Delete operation is failed.!!!");
        }
    }

    @Override
    public double[] responseCounter() {
        double[] responseCounter = new double[rsp.length];
        for (int i = 0; i < rsp.length; i++) {
            for (int j = i + 1; j < rsp.length; j++) {
                if (rsp[i] == rsp[j]) {
                    responseCounter[i]++;
                }
            }
        }
        return responseCounter;
    }

    @Override
    public double bestResponse(double[] responseCounter) {
        double maxCount = responseCounter[0];        //en fazla olan response'u tutan deger
        int index = 0;                              // i = 0 --> NaiveBayes  i = 1 --> J48  i = 2 --> IBK
        for (int i = 0; i < responseCounter.length; i++) {
            if (maxCount < responseCounter[i]) {
                maxCount = responseCounter[i];
                index = i;
            }
        }
        return rsp[index];
    }

    @Override
    public String createFileName() {
        long timeStamp = System.currentTimeMillis();
        return timeStamp + ".arff";
    }
}
