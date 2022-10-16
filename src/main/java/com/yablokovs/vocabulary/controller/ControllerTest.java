package com.yablokovs.vocabulary.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/*
 * Controller Testing Two Parallel Requests But Bean Remains The Same - only Thread is changing :)
 * */
@Slf4j
@RestController
public class ControllerTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    ListableBeanFactory listableBeanFactory;

    private int counter = 0;

    @SneakyThrows
    @PutMapping("/request")
    public ResponseEntity<String> test(@RequestBody RequestString requestString) {

        for (int i = 0; i < 3; i++) {
            Thread.sleep(5000);
            log.info("request with word: {} and counter: {}", requestString.getRequest(), ++counter);

            log.info("Thread.currentThread().getName(): {}", Thread.currentThread().getName());

            log.info("bean: {}", context.getBean(ControllerTest.class));

            for (String s : listableBeanFactory.getBeanNamesForType(ControllerTest.class)) {
                log.info("bean name: {}", s);
            }
        }

        return new ResponseEntity<>("result", HttpStatus.OK);
    }

    private static class RequestString {

        public String request;

        public String getRequest() {
            return request;
        }

        public void setRequest(String request) {
            this.request = request;
        }
    }

/*
2022-09-19 21:42:52.133  INFO 71273 --- [nio-8080-exec-2] c.y.v.controller.TestController          : Thread.currentThread().getName(): http-nio-8080-exec-2
2022-09-19 21:42:52.133  INFO 71273 --- [nio-8080-exec-2] c.y.v.controller.TestController          : bean: com.yablokovs.vocabulary.controller.TestController@608d1083
2022-09-19 21:42:52.133  INFO 71273 --- [nio-8080-exec-2] c.y.v.controller.TestController          : bean name: testController

2022-09-19 21:42:57.003  INFO 71273 --- [nio-8080-exec-3] c.y.v.controller.TestController          : request with word: name
2022-09-19 21:42:57.005  INFO 71273 --- [nio-8080-exec-3] c.y.v.controller.TestController          : Thread.currentThread().getName(): http-nio-8080-exec-3
2022-09-19 21:42:57.005  INFO 71273 --- [nio-8080-exec-3] c.y.v.controller.TestController          : bean: com.yablokovs.vocabulary.controller.TestController@608d1083
2022-09-19 21:42:57.005  INFO 71273 --- [nio-8080-exec-3] c.y.v.controller.TestController          : bean name: testController
*/

}
