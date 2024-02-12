package com.yg.mydrive.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller()
@RequestMapping("/")
public class IndexController {

    @RequestMapping("index")
    public String index() {
        return "index";
    }
}
