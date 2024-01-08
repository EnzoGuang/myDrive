package com.yg.mydrive;

import com.yg.mydrive.entity.Files;
import com.yg.mydrive.mapper.FileMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
class MydriveApplicationTests {
    @Autowired
    private FileMapper fileMaper;

    @Test
    void contextLoads() {
    }

    @Test
    void testFindFileByUserIdAndFolderId() {
        assertNotNull(fileMaper);
        System.out.println(fileMaper);
        List<Files> files = fileMaper.findFileByUserIdAndFolderId(1, 1);
        for (Files file: files) {
            System.out.println(file);
        }
    }

}
