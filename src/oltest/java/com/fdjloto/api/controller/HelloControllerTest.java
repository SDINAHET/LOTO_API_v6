// package com.fdjloto.api.controller;  controlleur

// public class HelloControllerTest {

// }

// package com.fdjloto.api.controller;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(HelloController.class)
// class HelloControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Test
//     void hello_should_return_200_and_body() throws Exception {
//         mockMvc.perform(get("/api/hello"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("hello"));
//     }
// }

package com.fdjloto.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HelloControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
    }

    @Test
    void hello_should_return_200_and_body() throws Exception {
        mockMvc.perform(get("/api/hello"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("Loto API is running")));
    }
}
