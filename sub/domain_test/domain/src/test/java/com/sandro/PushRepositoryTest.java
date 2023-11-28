package com.sandro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PushRepositoryTest {
    @Autowired
    PushRepository pushRepository;

    @Test
    void test() throws Exception {
        Push push = new FcmPush(null, "title");

        pushRepository.saveAndFlush(push);

        List<Push> all = pushRepository.findAll();
        System.out.println("all = " + all);
    }
}