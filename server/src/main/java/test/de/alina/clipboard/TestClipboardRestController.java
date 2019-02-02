package test.de.alina.clipboard;

import com.google.gson.Gson;
import de.alina.clipboard.Application;
import de.alina.clipboard.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class)
public class TestClipboardRestController {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGetID() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/get-id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testGetQRcodeInvalid() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/qr-image.png").param("id", "test")
                .accept(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().is(401));
    }

    @Test
    public void testQRcodeValid() throws Exception {
        String userid = getValidUUID();
        mvc.perform(MockMvcRequestBuilders.get("/qr-image.png").param("id", userid)
                .accept(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void testSendAndGetData() throws Exception {
        String userid = getValidUUID();
        String testdata = "hello world!";
        mvc.perform(MockMvcRequestBuilders.post("/send-data")
                .header("id",userid)
                .header("data", testdata))
                .andExpect(status().isOk());
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/get-data")
                .param("id", userid))
                .andExpect(status().isOk())
                .andReturn();
        String contentBody = result.getResponse().getContentAsString();
        Gson gson = new Gson();
        User user = gson.fromJson(contentBody, User.class);
        assertEquals(testdata, user.stringData);
    }

    private String getValidUUID() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/get-id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String contentString = result.getResponse().getContentAsString();
        Gson gson = new Gson();
        User user = gson.fromJson(contentString, User.class);
        return user.id.toString();
    }
}
